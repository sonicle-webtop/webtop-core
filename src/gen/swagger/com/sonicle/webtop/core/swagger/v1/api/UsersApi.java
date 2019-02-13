package com.sonicle.webtop.core.swagger.v1.api;

import com.sonicle.webtop.core.swagger.v1.model.User;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

@Path("/users")
@Api(description = "the users API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2019-02-13T11:59:17.339+01:00")
public abstract class UsersApi extends com.sonicle.webtop.core.sdk.BaseRestApiResource {

    @GET
    @Produces({ "application/json" })
    @ApiOperation(value = "List all enabled users", notes = "", response = User.class, responseContainer = "List", authorizations = {
        @Authorization(value = "Basic authentication")
    }, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = User.class, responseContainer = "List") })
    public Response getUsers(@QueryParam("targetProfileId")    String targetProfileId) {
        return Response.ok().entity("magic!").build();
    }
}
