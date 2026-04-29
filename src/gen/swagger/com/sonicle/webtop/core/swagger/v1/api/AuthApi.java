package com.sonicle.webtop.core.swagger.v1.api;

import com.sonicle.webtop.core.swagger.v1.model.ApiAuthLoginRequest;
import com.sonicle.webtop.core.swagger.v1.model.ApiAuthLogoutRequest;
import com.sonicle.webtop.core.swagger.v1.model.ApiAuthRefreshRequest;
import com.sonicle.webtop.core.swagger.v1.model.ApiAuthSessionInfo;
import com.sonicle.webtop.core.swagger.v1.model.ApiAuthTokenPair;
import com.sonicle.webtop.core.swagger.v1.model.ApiError;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import java.io.InputStream;
import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

@Path("/auth")
@Api(description = "the auth API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-04-29T10:39:20.102+02:00[Europe/Berlin]")
public abstract class AuthApi extends com.sonicle.webtop.core.sdk.BaseRestApiResource {

    @GET
    @Path("/sessions")
    @Produces({ "application/json" })
    @ApiOperation(value = "Lists active sessions for the current user", notes = "Returns one entry per active refresh token belonging to the authenticated user.", response = ApiAuthSessionInfo.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "auth-bearer")
         }, tags={ "auth" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Active sessions", response = ApiAuthSessionInfo.class, responseContainer = "List"),
        @ApiResponse(code = 401, message = "Missing or invalid access token", response = ApiError.class)
    })
    public Response authListSessions() {
        return Response.ok().entity("magic!").build();
    }

    @POST
    @Path("/login")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Authenticates a user and issues a token pair", notes = "Validates user credentials and returns an access token + refresh token pair. Anonymous endpoint (no authentication required). On invalid credentials responds with 401 and WWW-Authenticate: Bearer error=\"invalid_grant\".", response = ApiAuthTokenPair.class, tags={ "auth" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Login successful", response = ApiAuthTokenPair.class),
        @ApiResponse(code = 400, message = "Invalid request", response = ApiError.class),
        @ApiResponse(code = 401, message = "Invalid credentials", response = ApiError.class),
        @ApiResponse(code = 423, message = "Account or domain disabled", response = ApiError.class),
        @ApiResponse(code = 429, message = "Too many login attempts", response = ApiError.class),
        @ApiResponse(code = 503, message = "System under maintenance", response = ApiError.class)
    })
    public Response authLogin(@Valid @NotNull ApiAuthLoginRequest body) {
        return Response.ok().entity("magic!").build();
    }

    @POST
    @Path("/logout")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Revokes the current session (refresh token + its access tokens)", notes = "Idempotent. If a refreshToken is supplied in the body, that session is revoked along with all access tokens descended from it; otherwise only the chain of the access token used for this call is revoked.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-bearer")
         }, tags={ "auth" })
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "Logged out", response = Void.class),
        @ApiResponse(code = 401, message = "Missing or invalid access token", response = ApiError.class)
    })
    public Response authLogout(@Valid ApiAuthLogoutRequest body) {
        return Response.ok().entity("magic!").build();
    }

    @POST
    @Path("/logout-all")
    @Produces({ "application/json" })
    @ApiOperation(value = "Revokes every session (all devices) for the current user", notes = "Revokes all refresh and access tokens belonging to the authenticated user across every device.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-bearer")
         }, tags={ "auth" })
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "All sessions revoked", response = Void.class),
        @ApiResponse(code = 401, message = "Missing or invalid access token", response = ApiError.class)
    })
    public Response authLogoutAll() {
        return Response.ok().entity("magic!").build();
    }

    @POST
    @Path("/refresh")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Rotates a refresh token for a new token pair", notes = "Atomically validates and revokes the supplied refresh token, then mints a new access + refresh token pair. If the supplied refresh token has already been revoked, the entire chain is revoked (theft signal) and 401 is returned with WWW-Authenticate: Bearer error=\"invalid_grant\".", response = ApiAuthTokenPair.class, tags={ "auth" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Token pair rotated", response = ApiAuthTokenPair.class),
        @ApiResponse(code = 400, message = "Invalid request", response = ApiError.class),
        @ApiResponse(code = 401, message = "Invalid, expired or revoked refresh token", response = ApiError.class),
        @ApiResponse(code = 429, message = "Too many refresh attempts", response = ApiError.class),
        @ApiResponse(code = 503, message = "System under maintenance", response = ApiError.class)
    })
    public Response authRefresh(@Valid @NotNull ApiAuthRefreshRequest body) {
        return Response.ok().entity("magic!").build();
    }

    @DELETE
    @Path("/sessions/{session_id}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Revokes a specific session by its ID", notes = "Revokes the refresh token identified by sessionId and all access tokens descended from it. The sessionId is the opaque value returned by GET /auth/sessions.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-bearer")
         }, tags={ "auth" })
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "Session revoked", response = Void.class),
        @ApiResponse(code = 401, message = "Missing or invalid access token", response = ApiError.class),
        @ApiResponse(code = 404, message = "Session not found or not owned by the calling user", response = ApiError.class)
    })
    public Response authRevokeSession(@PathParam("session_id") @ApiParam("The opaque session ID returned by GET /auth/sessions") String sessionId) {
        return Response.ok().entity("magic!").build();
    }
}
