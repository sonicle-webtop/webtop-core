/*
 * Copyright (C) 2021 Sonicle S.r.l.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY SONICLE, SONICLE DISCLAIMS THE
 * WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle[at]sonicle[dot]com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2021 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.app;

import com.sonicle.commons.PathUtils;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.webtop.core.app.servlet.RestApi;
import com.sonicle.webtop.core.app.util.ClassHelper;
import com.sonicle.webtop.core.sdk.AuthException;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import io.swagger.v3.core.filter.AbstractSpecFilter;
import io.swagger.v3.jaxrs2.integration.resources.AcceptHeaderOpenApiResource;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.web.jaxrs.ShiroFeature;
import org.glassfish.jersey.internal.util.collection.StringKeyIgnoreCaseMultivaluedMap;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 * https://github.com/swagger-api/swagger-core/wiki/Swagger-2.X---Integration-and-configuration#jax-rs-application
 */
public class ServiceApiJaxRsResourceApplication extends ResourceConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceApiJaxRsResourceApplication.class);
	
	public ServiceApiJaxRsResourceApplication(@Context ServletConfig servletConfig) {
		super();
		
		WebTopApp wta = WebTopApp.getInstance();
		if (wta == null) throw new WTRuntimeException("WTA is null");
		
		register(ShiroFeature.class); // Add Shiro integration
		//register(new ServiceModelProcessor());
		register(new AuthExceptionMapper()); // Needed for legacy encpoints (in new ones is not applicable due to explicit throws in resource)
		register(new WTExceptionMapper()); // Needed for legacy encpoints (in new ones is not applicable due to explicit throws in resource)
		register(org.glassfish.jersey.jackson.JacksonFeature.class);
		//register(MultiPartFeature.class); // if needed
		
		//TODO: deprecate legacy endpoints!!!
		registerLegacyApiEndpoints(wta, servletConfig);
		registerServiceApiEndpoints(wta, servletConfig);
	}
	
	@Deprecated
	private void registerLegacyApiEndpoints(WebTopApp wta, ServletConfig servletConfig) {
		ServiceManager svcMgr = wta.getServiceManager();
		
		String serviceId = servletConfig.getInitParameter(RestApi.INIT_PARAM_WEBTOP_SERVICE_ID);
		if (StringUtils.isBlank(serviceId)) throw new WTRuntimeException("Invalid servlet init parameter [" + RestApi.INIT_PARAM_WEBTOP_SERVICE_ID + "]");
		
		ServiceDescriptor desc = svcMgr.getDescriptor(serviceId);
		if (desc == null) throw new WTRuntimeException("Service descriptor not found [{0}]", serviceId);
		
		if (desc.hasRestApiEndpoints()) {
			// Take only the first. We do not support many endpoints anymore!!!
			ServiceDescriptor.ApiEndpointClass endpointClass = desc.getRestApiEndpoints().get(0);
			LOGGER.debug("[{}] Registering JaxRs resource (legacy) [{}] -> [{}]", servletConfig.getServletName(), endpointClass.clazz.getName(), "/");
			registerResources(Resource.builder(endpointClass.clazz).path("/").build());
		}
	}
	
	private void registerServiceApiEndpoints(WebTopApp wta, ServletConfig servletConfig) {
		ServiceManager svcMgr = wta.getServiceManager();
		
		String serviceId = servletConfig.getInitParameter(RestApi.INIT_PARAM_WEBTOP_SERVICE_ID);
		if (StringUtils.isBlank(serviceId)) throw new WTRuntimeException("Invalid servlet init parameter [{}]", RestApi.INIT_PARAM_WEBTOP_SERVICE_ID);
		
		ServiceDescriptor desc = svcMgr.getDescriptor(serviceId);
		if (desc == null) throw new WTRuntimeException("Service descriptor not found [{}]", serviceId);
		
		if (desc.hasOpenApiDefinitions()) {
			for (ServiceDescriptor.OpenApiDefinition apiDefinition : desc.getOpenApiDefinitions()) {
				// Register API spec listing resources
				OpenAPI oa = new OpenAPI();			
				oa.info(desc.buildOpenApiInfo(apiDefinition));	
				SwaggerConfiguration oaConfig = new SwaggerConfiguration()
					.openAPI(oa)
					// Sets our filter Class in order to customize OpenAPI spec adding server informations
					.filterClass(DynamicServerSpecFilter.class.getName())
					.prettyPrint(false)
					.resourcePackages(Stream.of(apiDefinition.implPackage).collect(Collectors.toSet()));
				
				ServiceOpenApiResource oar = new ServiceOpenApiResource(); // "*/openapi.json", "*/openapi.yaml"
				oar.setOpenApiConfiguration(oaConfig);
				register(oar);
				ServiceAcceptHeaderOpenApiResource ahoar = new ServiceAcceptHeaderOpenApiResource(); // "*/openapi" with Accept header
				oar.setOpenApiConfiguration(oaConfig);
				register(ahoar);
				
				// Register API resources
				for (Class clazz : apiDefinition.resourceClasses) {
					javax.ws.rs.Path pathAnnotation = (javax.ws.rs.Path)ClassHelper.getClassAnnotation(clazz.getSuperclass(), javax.ws.rs.Path.class);
					String resourcePath = "/" + PathUtils.concatPathParts(apiDefinition.context, pathAnnotation.value());
					LOGGER.debug("[{}] Registering JaxRs resource [{}] -> [{}]", servletConfig.getServletName(), clazz.toString(), resourcePath);
					registerResources(Resource.builder(clazz).path(resourcePath).build());
				}
			}
		}
	}
	
	public static class DynamicServerSpecFilter extends AbstractSpecFilter {

		@Override
		public Optional<OpenAPI> filterOpenAPI(OpenAPI openAPI, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
			List<String> xRealReqUrlHeader = headers.get(WrappedHttpHeaders.X_REAL_REQUEST_URL);
			if (xRealReqUrlHeader != null && !xRealReqUrlHeader.isEmpty()) {
				if (!StringUtils.isBlank(openAPI.getInfo().getVersion())) {
					String serverUrl = xRealReqUrlHeader.get(0) + "/" + openAPI.getInfo().getVersion();
					openAPI.addServersItem(new Server().url(serverUrl));
				}
				return Optional.of(openAPI);
			} else {
				return super.filterOpenAPI(openAPI, params, cookies, headers);
			}
		}
	}
	
	public static class ServiceOpenApiResource extends OpenApiResource {
		@Context
		HttpServletRequest request;

		@Override
		public Response getOpenApi(HttpHeaders headers, UriInfo uriInfo, String type) throws Exception {
			// In order to allow the above filter (DynamicServerSpecFilter) to 
			// build the server URL using the external URL, we need to subclass 
			// this OpenApiResource replace the HttpHeaders class 
			// with a wrapped instance that injects some new headers.
			
			if (request != null) {
				final String requestURL = StringUtils.substringBeforeLast(ServletUtils.getRequestURLString(request), "/");
				return super.getOpenApi(new WrappedHttpHeaders(headers, requestURL), uriInfo, type);
			} else {
				return super.getOpenApi(headers, uriInfo, type);
			}
		}
	}
	
	public static class ServiceAcceptHeaderOpenApiResource extends AcceptHeaderOpenApiResource {
		@Context
		HttpServletRequest request;

		@Override
		protected Response getOpenApi(HttpHeaders headers, ServletConfig config, Application app, UriInfo uriInfo, String type) throws Exception {
			// In order to allow the above filter (DynamicServerSpecFilter) to 
			// build the server URL using the external URL, we need to subclass 
			// this AcceptHeaderOpenApiResource replace the HttpHeaders class 
			// with a wrapped instance that injects some new headers.
			
			if (request != null) {
				// https://github.com/dotnet/aspnetcore/issues/23263
				final String requestURL = StringUtils.substringBeforeLast(ServletUtils.getRequestURLString(request), "/");
				return super.getOpenApi(new WrappedHttpHeaders(headers, requestURL), config, app, uriInfo, type);
			} else {
				return super.getOpenApi(headers, config, app, uriInfo, type);
			}
		}
	}
	
	public static class WrappedHttpHeaders implements HttpHeaders {
		public static final String X_REAL_REQUEST_URL = "x-real-request-url";
		private final HttpHeaders httpHeaders;
		private final String xRealUrlReqHeaderValue;
		
		public WrappedHttpHeaders(HttpHeaders httpHeaders, String xRealUrlReqHeaderValue) {
			this.httpHeaders = httpHeaders;
			this.xRealUrlReqHeaderValue = xRealUrlReqHeaderValue;
		}
		
		@Override
		public List<String> getRequestHeader(String string) {
			if (X_REAL_REQUEST_URL.equalsIgnoreCase(X_REAL_REQUEST_URL)) {
				return Arrays.asList(xRealUrlReqHeaderValue);
			} else {
				return httpHeaders.getRequestHeader(string);
			}
		}

		@Override
		public String getHeaderString(String string) {
			return httpHeaders.getHeaderString(string);
		}

		@Override
		public MultivaluedMap<String, String> getRequestHeaders() {
			MultivaluedMap<String, String> origMap = httpHeaders.getRequestHeaders();
			MultivaluedMap<String, String> map = new StringKeyIgnoreCaseMultivaluedMap<>();
			for (Map.Entry<String, List<String>> entry : origMap.entrySet()) {
				map.put(entry.getKey(), entry.getValue());
			}
			map.add(X_REAL_REQUEST_URL, xRealUrlReqHeaderValue);
			return map;
		}

		@Override
		public List<MediaType> getAcceptableMediaTypes() {
			return httpHeaders.getAcceptableMediaTypes();
		}

		@Override
		public List<Locale> getAcceptableLanguages() {
			return httpHeaders.getAcceptableLanguages();
		}

		@Override
		public MediaType getMediaType() {
			return httpHeaders.getMediaType();
		}

		@Override
		public Locale getLanguage() {
			return httpHeaders.getLanguage();
		}

		@Override
		public Map<String, Cookie> getCookies() {
			return httpHeaders.getCookies();
		}

		@Override
		public Date getDate() {
			return httpHeaders.getDate();
		}

		@Override
		public int getLength() {
			return httpHeaders.getLength();
		}
	}
	
	private static class AuthExceptionMapper implements ExceptionMapper<AuthException> {
		@Override
		public Response toResponse(AuthException e) {
			return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
		}
	}
	
	private static class WTExceptionMapper implements ExceptionMapper<WTException> {
		@Override
		public Response toResponse(WTException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}
}
