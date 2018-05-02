/*
 * WebTop Services is a Web Application framework developed by Sonicle S.r.l.
 * Copyright (C) 2014 Sonicle S.r.l.
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
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle@sonicle.com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2014 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.app;

import com.sonicle.webtop.core.app.util.ClassHelper;
import com.sonicle.commons.PathUtils;
import com.sonicle.webtop.core.sdk.AuthException;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import com.sonicle.webtop.core.servlet.RestApi;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.ServletConfig;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;
import org.secnod.shiro.jaxrs.ShiroExceptionMapper;
import org.secnod.shiro.jersey.SubjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class JaxRsServiceApplication extends ResourceConfig {
	private static final Logger logger = LoggerFactory.getLogger(JaxRsServiceApplication.class);
	
	public JaxRsServiceApplication(@Context ServletConfig servletConfig) {
		super();
		
		register(new SubjectFactory());
		//register(new ServiceModelProcessor());
		register(new ShiroExceptionMapper());
		register(new AuthExceptionMapper()); // Needed for legacy encpoints (in new ones is not applicable due to explicit throws in resource)
		register(new WTExceptionMapper()); // Needed for legacy encpoints (in new ones is not applicable due to explicit throws in resource)
		register(org.glassfish.jersey.jackson.JacksonFeature.class);
		//register(MultiPartFeature.class); // if needed
		
		WebTopApp wta = WebTopApp.getInstance();
		if (wta == null) throw new WTRuntimeException("WTA is null");
		configureLegacyApiEndpoints(wta, servletConfig);
		configureServiceApis(wta, servletConfig);
		packages("io.swagger.v3.jaxrs2.integration.resources");
	}
	
	private void configureLegacyApiEndpoints(WebTopApp wta, ServletConfig servletConfig) {
		ServiceManager svcMgr = wta.getServiceManager();
		
		String serviceId = servletConfig.getInitParameter(RestApi.INIT_PARAM_WEBTOP_SERVICE_ID);
		if (StringUtils.isBlank(serviceId)) throw new WTRuntimeException("Invalid servlet init parameter [" + RestApi.INIT_PARAM_WEBTOP_SERVICE_ID + "]");
		
		ServiceDescriptor desc = svcMgr.getDescriptor(serviceId);
		if (desc == null) throw new WTRuntimeException("Service descriptor not found [{0}]", serviceId);
		
		if (desc.hasRestApiEndpoints()) {
			// Take only the first. We do not support many endpoints anymore!!!
			ServiceDescriptor.ApiEndpointClass endpointClass = desc.getRestApiEndpoints().get(0);
			logger.debug("[{}] Registering JaxRs resource (legacy) [{}] -> [{}]", servletConfig.getServletName(), endpointClass.toString(), "/");
			registerResources(Resource.builder(endpointClass.clazz).path("/").build());
		}
	}
	
	private void configureServiceApis(WebTopApp wta, ServletConfig servletConfig) {
		ServiceManager svcMgr = wta.getServiceManager();
		
		String serviceId = servletConfig.getInitParameter(RestApi.INIT_PARAM_WEBTOP_SERVICE_ID);
		if (StringUtils.isBlank(serviceId)) throw new WTRuntimeException("Invalid servlet init parameter [" + RestApi.INIT_PARAM_WEBTOP_SERVICE_ID + "]");
		
		ServiceDescriptor desc = svcMgr.getDescriptor(serviceId);
		if (desc == null) throw new WTRuntimeException("Service descriptor not found [{0}]", serviceId);
		
		if (desc.hasOpenApiDefinitions()) {
			for (ServiceDescriptor.OpenApiDefinition apiDefinition : desc.getOpenApiDefinitions()) {
				// Register resources
				for (Class clazz : apiDefinition.resourceClasses) {
					javax.ws.rs.Path pathAnnotation = (javax.ws.rs.Path)ClassHelper.getClassAnnotation(clazz.getSuperclass(), javax.ws.rs.Path.class);
					String resourcePath = "/" + PathUtils.concatPathParts(apiDefinition.context, pathAnnotation.value());
					logger.debug("[{}] Registering JaxRs resource [{}] -> [{}]", servletConfig.getServletName(), clazz.toString(), resourcePath);
					registerResources(Resource.builder(clazz).path(resourcePath).build());
				}
				
				// Configure OpenApi listing
				OpenAPI oa = new OpenAPI();
				oa.info(desc.buildOpenApiInfo(apiDefinition));
				
				SwaggerConfiguration oaConfig = new SwaggerConfiguration()
						.openAPI(oa)
						.prettyPrint(true)
						.resourcePackages(Stream.of(apiDefinition.implPackage).collect(Collectors.toSet()));
				
				try {
					new JaxrsOpenApiContextBuilder()
							.servletConfig(servletConfig)
							.application(this)
							.ctxId(apiDefinition.context)
							.openApiConfiguration(oaConfig)
							.buildContext(true);
				} catch (OpenApiConfigurationException ex) {
					logger.error("Unable to init swagger", ex);
				}
			}
		}
	}
	
	private static class AuthExceptionMapper implements ExceptionMapper<AuthException> {
		@Override
		public Response toResponse(AuthException e) {
			return Response.status(Status.FORBIDDEN).entity(e.getMessage()).build();
		}
	}
	
	private static class WTExceptionMapper implements ExceptionMapper<WTException> {
		@Override
		public Response toResponse(WTException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}
}
