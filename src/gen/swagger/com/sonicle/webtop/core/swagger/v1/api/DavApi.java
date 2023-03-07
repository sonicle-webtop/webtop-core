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

@Path("/dav/principals/{profileUsername}")
@Api(description = "the dav API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2023-01-19T09:40:24.427+01:00[Europe/Berlin]")
public abstract class DavApi extends com.sonicle.webtop.core.sdk.BaseRestApiResource {

    @GET
    @Produces({ "application/json" })
    @ApiOperation(value = "Returns principal info", notes = "", response = ApiPrincipalInfo.class, authorizations = {
        
        @Authorization(value = "auth-bearer")
         }, tags={ "dav-principals" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiPrincipalInfo.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = ApiError.class),
        @ApiResponse(code = 404, message = "Principal not found", response = ApiError.class)
    })
    public Response getDavPrincipalInfo(@PathParam("profileUsername") @ApiParam("Full profile username") String profileUsername) {
        return Response.ok().entity("magic!").build();
    }
}
