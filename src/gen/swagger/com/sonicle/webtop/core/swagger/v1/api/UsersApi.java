package com.sonicle.webtop.core.swagger.v1.api;

import com.sonicle.webtop.core.swagger.v1.model.ApiError;
import com.sonicle.webtop.core.swagger.v1.model.ApiLegacyUser;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import java.io.InputStream;
import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

@Path("/users")
@Api(description = "the users API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-04-16T12:49:23.936+02:00[Europe/Berlin]")
public abstract class UsersApi extends com.sonicle.webtop.core.sdk.BaseRestApiResource {

    @GET
    @Produces({ "application/json" })
    @ApiOperation(value = "List all enabled users", notes = "", response = ApiLegacyUser.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "auth-bearer")
         }, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiLegacyUser.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response getUsers(@QueryParam("targetProfileId")   String targetProfileId) {
        return Response.ok().entity("magic!").build();
    }
}
