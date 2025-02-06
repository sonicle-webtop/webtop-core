/*
 * Copyright (C) 2025 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2025 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.app;

import com.sonicle.commons.LangUtils;
import com.sonicle.commons.PathUtils;
import com.sonicle.webtop.core.app.jaxrs.NOOPJaxrsApplicationAndResourcePackagesAnnotationScanner;
import com.sonicle.webtop.core.app.jaxrs.CustomizerSpecFilter;
import com.sonicle.webtop.core.app.jaxrs.CustomizedAcceptHeaderOpenApiResource;
import com.sonicle.webtop.core.app.jaxrs.CustomizedOpenApiResource;
import com.sonicle.webtop.core.app.servlet.RestApi;
import com.sonicle.webtop.core.app.util.ClassHelper;
import com.sonicle.webtop.core.sdk.AuthException;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.servlet.ServletConfig;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.web.jaxrs.ShiroFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 * https://github.com/swagger-api/swagger-core/wiki/Swagger-2.X---Integration-and-configuration#jax-rs-application
 */
public class ServiceApiJaxRsApplication extends ResourceConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceApiJaxRsApplication.class);
	
	public ServiceApiJaxRsApplication(@Context ServletConfig servletConfig) {
		super();
		
		WebTopApp wta = WebTopApp.getInstance();
		if (wta == null) throw new WTRuntimeException("WTA is null");
		
		register(ShiroFeature.class); // Add Shiro integration
		//register(new ServiceModelProcessor());
		register(org.glassfish.jersey.jackson.JacksonFeature.class);
		//register(MultiPartFeature.class); // if needed
		
		registerServiceApiEndpoints(wta, servletConfig);
	}
	
	private void registerServiceApiEndpoints(WebTopApp wta, ServletConfig servletConfig) {
		ServiceManager svcMgr = wta.getServiceManager();
		boolean publishSpec = WebTopProps.getOpenApiPublishSpec(wta.getProperties());
		
		String serviceId = servletConfig.getInitParameter(RestApi.INIT_PARAM_WEBTOP_SERVICE_ID);
		if (StringUtils.isBlank(serviceId)) throw new WTRuntimeException("Invalid servlet init parameter [{}]", RestApi.INIT_PARAM_WEBTOP_SERVICE_ID);
		
		ServiceDescriptor desc = svcMgr.getDescriptor(serviceId);
		if (desc == null) throw new WTRuntimeException("Service descriptor not found [{}]", serviceId);
		
		if (desc.hasOpenApiDefinitions()) {
			// Register endpoint for returning the active (actually the last one) API spec file
			ServiceDescriptor.OpenApiDefinition specDefinition = desc.getOpenApiDefinitions().get(desc.getOpenApiDefinitions().size()-1);
			if (publishSpec) {
				registerSpecExposingResources(servletConfig, desc, specDefinition);
			} else {
				LOGGER.debug("[{}] JAX-RS Spec resource is disabled: '{}' will NOT be published!", servletConfig.getServletName(), specDefinition.oasFile);
			}
			
			// Register implementation endpoints
			for (ServiceDescriptor.OpenApiDefinition apiDefinition : desc.getOpenApiDefinitions()) {
				// Register API implementation classes
				for (Class clazz : apiDefinition.resourceClasses) {
					javax.ws.rs.Path pathAnnotation = (javax.ws.rs.Path)ClassHelper.getClassAnnotation(clazz.getSuperclass(), javax.ws.rs.Path.class);
					String resourcePath = "/" + PathUtils.concatPathParts(apiDefinition.context, pathAnnotation.value());
					LOGGER.debug("[{}] Registering JAX-RS resource at '{}' [{}]", servletConfig.getServletName(), resourcePath, clazz.toString());
					registerResources(Resource.builder(clazz).path(resourcePath).build());
				}
			}
		}
	}
	
	private void registerSpecExposingResources(final ServletConfig servletConfig, final ServiceDescriptor descriptor, final ServiceDescriptor.OpenApiDefinition apiDefinition) {
		try {
			LOGGER.debug("[{}] Parsing OpenAPI Spec '{}'", servletConfig.getServletName(), apiDefinition.oasFile);
			String rawOpenApi = readOpenAPISpec(apiDefinition.oasFile);
			SwaggerParseResult result = new OpenAPIParser().readContents(rawOpenApi, null, null);
			OpenAPI openAPI = result.getOpenAPI();
			if (openAPI != null) {
				openAPI.info(descriptor.buildOpenApiInfo(apiDefinition));
				SwaggerConfiguration oaConfig = new SwaggerConfiguration()
					.openAPI(openAPI)
					// Internally, both ServicedOpenApiResource and ServicedAcceptHeaderOpenApiResource
					// implementations will create a default scanner to complete/integrate passed Spec here, 
					// by adding auto-discovered endpoints and paths.
					// Since that we only want do leverage only on the passed Spec, we sets a NOOP scanner class
					// that simply disarm any automatic lookup.
					// (see buildScanner method in io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContext)
					.scannerClass(NOOPJaxrsApplicationAndResourcePackagesAnnotationScanner.class.getName())
					// Set our filter Class in order to customize OpenAPI Spec adding server informations
					.filterClass(CustomizerSpecFilter.class.getName())
					.prettyPrint(false);
				
				LOGGER.debug("[{}] Registering JAX-RS Spec resource at '/openapi.[json|yaml]'", servletConfig.getServletName());
				CustomizedOpenApiResource oar = new CustomizedOpenApiResource(); // "*/openapi.json", "*/openapi.yaml"
				oar.setOpenApiConfiguration(oaConfig);
				register(oar);
				
				//String soarPath = "/" + PathUtils.concatPathParts(apiDefinition.context, extractJaxRsResourcePathAnnotation(oar.getClass().getSuperclass()));
				//Resource soar = Resource.builder(oar).path(soarPath).build();
				//registerResources(soar);
				
				CustomizedAcceptHeaderOpenApiResource ahoar = new CustomizedAcceptHeaderOpenApiResource(); // "*/openapi" with Accept header
				ahoar.setOpenApiConfiguration(oaConfig);
				//String resourcePath2 = "/" + PathUtils.concatPathParts(apiDefinition.context, extractJaxRsResourcePathAnnotation(ahoar.getClass().getSuperclass()));
				register(ahoar);
			}
		} catch (Exception ex) {
			LOGGER.error("[{}] Unable to configure JAX-RS Spec resource", ex, servletConfig.getServletName());
		}
	}
	
	private String extractJaxRsResourcePathAnnotation(final Class clazz) {
		javax.ws.rs.Path pathAnnotation = (javax.ws.rs.Path)ClassHelper.getClassAnnotation(clazz, javax.ws.rs.Path.class);
		return pathAnnotation != null ? pathAnnotation.value() : null;
	}
	
	public String readOpenAPISpec(final String name) throws IOException {
		InputStream is = null;
		try {
			is = LangUtils.findClassLoader(getClass()).getResourceAsStream(name);
			if (is == null) throw new IOException("InputStream is null");
			return IOUtils.toString(is, StandardCharsets.UTF_8);
		} finally {
			IOUtils.closeQuietly(is);
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
