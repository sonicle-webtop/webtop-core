package com.sonicle.webtop.core.swagger.v1.api;

import com.sonicle.webtop.core.swagger.v1.model.ApiError;
import com.sonicle.webtop.core.swagger.v1.model.ApiPlatformService;
import com.sonicle.webtop.core.swagger.v1.model.ApiRecipient;
import com.sonicle.webtop.core.swagger.v1.model.ApiTag;
import com.sonicle.webtop.core.swagger.v1.model.ApiTagBase;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import java.io.InputStream;
import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

@Path("/me")
@Api(description = "the me API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-03-16T17:01:06.128+01:00[Europe/Berlin]")
public abstract class MeApi extends com.sonicle.webtop.core.sdk.BaseRestApiResource {

    @POST
    @Path("/tags")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "", notes = "Adds new Tag.", response = ApiTag.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created", response = ApiTag.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response addTag(@Valid ApiTagBase body) {
        return Response.ok().entity("magic!").build();
    }

    @DELETE
    @Path("/tags/{tag_id}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete tag", notes = "Deletes the specified Tag given its ID.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 404, message = "Not Found", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response deleteTag(@PathParam("tag_id") String tagId) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/platform/services")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get platform services", notes = "", response = ApiPlatformService.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "me" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiPlatformService.class, responseContainer = "List"),
        @ApiResponse(code = 403, message = "Forbidden", response = Void.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = Void.class)
    })
    public Response getPlatformServices() {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/recipients-providers/sources")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get recipients-provider sources", notes = "Returns the recipients-provider IDs avaiable.", response = String.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "me" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = String.class, responseContainer = "List"),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response getRecipientsProviderSources() {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/tags/{tag_id}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a tag", notes = "Gets the specified Tag given its ID.", response = ApiTag.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "me" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiTag.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 404, message = "Not Found", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response getTag(@PathParam("tag_id") String tagId) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/recipients")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get recipients-provider recipients", notes = "Returns a list of recipients belonging the ", response = ApiRecipient.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "me" })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created", response = ApiRecipient.class, responseContainer = "List"),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response listRecipients(@QueryParam("address_type") @NotNull  @ApiParam("The Recipient&#39;s field type to return as address")  String addressType,@QueryParam("query")  @ApiParam("The query text to match")  String query,@QueryParam("max_results")  @ApiParam("The max number of results to return")  Integer maxResults,@QueryParam("list_options") @Min(0L) @Max(512L)  @ApiParam("One of the following values, or a sum of multiple values: 1 - BuiltInProvidersLast, 2 - IncludeBuiltInProviderAuto, 8 - IncludeBuiltInProviderWebTop.")  Long listOptions) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/recipients-providers/recipients")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get recipients-provider recipients", notes = "Returns a list of recipients belonging to the specified set of sources.", response = ApiRecipient.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "me" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiRecipient.class, responseContainer = "List"),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response listRecipientsOfSources(@QueryParam("source_providers") @NotNull  @ApiParam("A collection of provider IDs in which look for")  String sourceProviders,@QueryParam("address_type") @NotNull  @ApiParam("The Recipient&#39;s field type to return as address")  String addressType,@QueryParam("query")  @ApiParam("The query text to match")  String query,@QueryParam("max_results")  @ApiParam("The max number of results to return")  Integer maxResults) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/tags")
    @Produces({ "application/json" })
    @ApiOperation(value = "List tags", notes = "Returns a list of Tags.", response = ApiTag.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "me" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiTag.class, responseContainer = "List"),
        @ApiResponse(code = 403, message = "Forbidden", response = Void.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = Void.class)
    })
    public Response listTags(@QueryParam("visibility")   String visibility) {
        return Response.ok().entity("magic!").build();
    }

    @PUT
    @Path("/tags/{tag_id}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update tag", notes = "Updates the specified Tag given its ID.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 404, message = "Not Found", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response updateTag(@PathParam("tag_id") String tagId,@Valid ApiTagBase body) {
        return Response.ok().entity("magic!").build();
    }
}
