package com.sonicle.webtop.core.swagger.v1.api;

import com.sonicle.webtop.core.swagger.v1.model.PrincipalInfo;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

@Path("/principals")
@Api(description = "the principals API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2019-01-30T14:32:26.335+01:00")
public abstract class PrincipalsApi extends com.sonicle.webtop.core.sdk.BaseRestApiResource {

    @GET
    @Path("/{profileUsername}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Returns principal info", notes = "", response = PrincipalInfo.class, authorizations = {
        @Authorization(value = "Basic authentication")
    }, tags={ "principals" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = PrincipalInfo.class),
        @ApiResponse(code = 400, message = "Invalid parameter", response = Void.class),
        @ApiResponse(code = 404, message = "Principal not found", response = Void.class) })
    public Response getPrincipalInfo(@PathParam("profileUsername") @ApiParam("Full profile username (user@domain.tld)") String profileUsername) {
        return Response.ok().entity("magic!").build();
    }
}
