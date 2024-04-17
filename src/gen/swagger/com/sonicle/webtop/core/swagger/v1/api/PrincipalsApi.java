package com.sonicle.webtop.core.swagger.v1.api;

import com.sonicle.webtop.core.swagger.v1.model.ApiError;
import com.sonicle.webtop.core.swagger.v1.model.ApiPrincipalInfo;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import java.io.InputStream;
import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

@Path("/principals/{profileUsername}")
@Api(description = "the principals API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-04-16T12:49:23.936+02:00[Europe/Berlin]")
public abstract class PrincipalsApi extends com.sonicle.webtop.core.sdk.BaseRestApiResource {

    @GET
    @Produces({ "application/json" })
    @ApiOperation(value = "Returns principal info", notes = "", response = ApiPrincipalInfo.class, authorizations = {
        
        @Authorization(value = "auth-bearer")
         }, tags={ "principals" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiPrincipalInfo.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = ApiError.class),
        @ApiResponse(code = 401, message = "Invalid credentials", response = ApiError.class),
        @ApiResponse(code = 404, message = "Principal not found", response = Void.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = Void.class)
    })
    public Response getPrincipalInfo(@PathParam("profileUsername") @ApiParam("Full profile username (user@domain.tld)") String profileUsername,@QueryParam("permRefs")  @ApiParam("Permissions to evaluate")  List<String> permRefs) {
        return Response.ok().entity("magic!").build();
    }
}
