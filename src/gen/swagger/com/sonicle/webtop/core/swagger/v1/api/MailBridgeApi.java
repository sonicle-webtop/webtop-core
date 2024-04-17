package com.sonicle.webtop.core.swagger.v1.api;

import com.sonicle.webtop.core.swagger.v1.model.ApiError;
import com.sonicle.webtop.core.swagger.v1.model.ApiFetcher;
import com.sonicle.webtop.core.swagger.v1.model.ApiRelay;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import java.io.InputStream;
import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

@Path("/mail-bridge")
@Api(description = "the mail-bridge API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-04-16T12:49:23.936+02:00[Europe/Berlin]")
public abstract class MailBridgeApi extends com.sonicle.webtop.core.sdk.BaseRestApiResource {

    @GET
    @Path("/fetchers")
    @Produces({ "application/json" })
    @ApiOperation(value = "List MailBridge Fetchers for a Domain", notes = "Lists Domain's MailBridge Fetchers.", response = ApiFetcher.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "auth-bearer")
         }, tags={ "mail-bridge-fetchers" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = ApiFetcher.class, responseContainer = "List"),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response mailBridgeListFetchers(@QueryParam("domain_id") @NotNull  @ApiParam("The domain ID")  String domainId) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/relays")
    @Produces({ "application/json" })
    @ApiOperation(value = "List MailBridge Relays for a Domain", notes = "Lists Domain's MailBridge Relays.", response = ApiRelay.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "auth-bearer")
         }, tags={ "mail-bridge-relays" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = ApiRelay.class, responseContainer = "List"),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response mailBridgeListRelays(@QueryParam("domain_id") @NotNull  @ApiParam("The domain ID")  String domainId) {
        return Response.ok().entity("magic!").build();
    }

    @POST
    @Path("/fetchers/{webtop_profile_id}/auth-state")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update auth state of fetcher", notes = "Updates auth state of fetcher.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-bearer")
         }, tags={ "mail-bridge-fetchers" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 404, message = "Not Found", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response mailBridgeUpdateFetcherAuthState(@PathParam("webtop_profile_id") String webtopProfileId,@QueryParam("state")  @ApiParam("auth state value (UN,OK,ER)")  String state,@Valid String body) {
        return Response.ok().entity("magic!").build();
    }

    @POST
    @Path("/relays/{webtop_profile_id}/auth-state")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update auth state of relay", notes = "Updates auth state of relay.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-bearer")
         }, tags={ "mail-bridge-relays" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 404, message = "Not Found", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response mailBridgeUpdateRelayAuthState(@PathParam("webtop_profile_id") String webtopProfileId,@QueryParam("state")  @ApiParam("auth state value (UN,OK,ER)")  String state,@Valid String body) {
        return Response.ok().entity("magic!").build();
    }
}
