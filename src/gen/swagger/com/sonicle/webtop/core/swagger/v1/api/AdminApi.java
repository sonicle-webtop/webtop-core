package com.sonicle.webtop.core.swagger.v1.api;

import com.sonicle.webtop.core.swagger.v1.model.ApiAddDomain201Response;
import com.sonicle.webtop.core.swagger.v1.model.ApiAddGroup201Response;
import com.sonicle.webtop.core.swagger.v1.model.ApiAddResource201Response;
import com.sonicle.webtop.core.swagger.v1.model.ApiAddRole201Response;
import com.sonicle.webtop.core.swagger.v1.model.ApiAddUser201Response;
import com.sonicle.webtop.core.swagger.v1.model.ApiApiKey;
import com.sonicle.webtop.core.swagger.v1.model.ApiApiKeyBase;
import com.sonicle.webtop.core.swagger.v1.model.ApiApiKeyGenerated;
import com.sonicle.webtop.core.swagger.v1.model.ApiDomain;
import com.sonicle.webtop.core.swagger.v1.model.ApiDomainBase;
import com.sonicle.webtop.core.swagger.v1.model.ApiDomainEntry;
import com.sonicle.webtop.core.swagger.v1.model.ApiError;
import com.sonicle.webtop.core.swagger.v1.model.ApiGroup;
import com.sonicle.webtop.core.swagger.v1.model.ApiGroupAdd;
import com.sonicle.webtop.core.swagger.v1.model.ApiGroupBase;
import com.sonicle.webtop.core.swagger.v1.model.ApiLicense;
import com.sonicle.webtop.core.swagger.v1.model.ApiLicenseAdd;
import com.sonicle.webtop.core.swagger.v1.model.ApiLicenseOfflineReqInfo;
import com.sonicle.webtop.core.swagger.v1.model.ApiResource;
import com.sonicle.webtop.core.swagger.v1.model.ApiResourceAdd;
import com.sonicle.webtop.core.swagger.v1.model.ApiResourceBase;
import com.sonicle.webtop.core.swagger.v1.model.ApiResultExceptions;
import com.sonicle.webtop.core.swagger.v1.model.ApiRole;
import com.sonicle.webtop.core.swagger.v1.model.ApiRoleAdd;
import com.sonicle.webtop.core.swagger.v1.model.ApiRoleBase;
import com.sonicle.webtop.core.swagger.v1.model.ApiSettingEntry;
import com.sonicle.webtop.core.swagger.v1.model.ApiUser;
import com.sonicle.webtop.core.swagger.v1.model.ApiUserAdd;
import com.sonicle.webtop.core.swagger.v1.model.ApiUserBase;
import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import java.io.InputStream;
import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

@Path("/admin")
@Api(description = "the admin API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-02-27T16:20:59.935+01:00[Europe/Berlin]")
public abstract class AdminApi extends com.sonicle.webtop.core.sdk.BaseRestApiResource {

    @POST
    @Path("/licenses/{product_code}/activation")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Activates a product's license for a domain", notes = "When no activated string is attached to body request, an online activation will be performed, otherwise the string will be used as result of manual (offline) activation process.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-licenses" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 404, message = "Not Found", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response activateLicense(@PathParam("product_code") @ApiParam("The product code") String productCode,@QueryParam("domain_id") @NotNull  @ApiParam("The domain ID")  String domainId,@Valid String body) {
        return Response.ok().entity("magic!").build();
    }

    @POST
    @Path("/domains")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add domain", notes = "Adds new Domain.", response = ApiAddDomain201Response.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-domains" })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created", response = ApiAddDomain201Response.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response addDomain(@Valid ApiDomain body) {
        return Response.ok().entity("magic!").build();
    }

    @POST
    @Path("/groups")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Adds a domain group", notes = "Adds new Domain group.", response = ApiAddGroup201Response.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-groups" })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created", response = ApiAddGroup201Response.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response addGroup(@QueryParam("domain_id") @NotNull  @ApiParam("The domain ID")  String domainId,@QueryParam("update_options") @Min(0L) @DefaultValue("30")  @ApiParam("One of the following values, or a sum of multiple values: 1 - SubjectsAsSID, 2 - UpdateUserAssociations, 4 - UpdateRoleAssociations, 8 - UpdatePermissions, 16 - UpdateServicePermissions.")  Long updateOptions,@Valid ApiGroupAdd body) {
        return Response.ok().entity("magic!").build();
    }

    @POST
    @Path("/licenses/{product_code}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Adds a product's license for a domain", notes = "", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-licenses" })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response addLicense(@PathParam("product_code") @ApiParam("The product code") String productCode,@QueryParam("domain_id") @NotNull  @ApiParam("The domain ID")  String domainId,@QueryParam("auto_activate") @DefaultValue("true")  @ApiParam("Specifies whether to try auto-activation")  Boolean autoActivate,@Valid ApiLicenseAdd body) {
        return Response.ok().entity("magic!").build();
    }

    @POST
    @Path("/resources")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Adds a resource for a domain", notes = "Adds new Domain resource.", response = ApiAddResource201Response.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-resources" })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created", response = ApiAddResource201Response.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response addResource(@QueryParam("domain_id") @NotNull  @ApiParam("The domain ID")  String domainId,@QueryParam("update_options") @Min(0L) @DefaultValue("2")  @ApiParam("One of the following values, or a sum of multiple values: 1 - SubjectsAsSID, 2 - UpdatePermissions.")  Long updateOptions,@Valid ApiResourceAdd body) {
        return Response.ok().entity("magic!").build();
    }

    @POST
    @Path("/roles")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Adds a role for a domain", notes = "Adds new Domain role.", response = ApiAddRole201Response.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-roles" })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created", response = ApiAddRole201Response.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response addRole(@QueryParam("domain_id") @NotNull  @ApiParam("The domain ID")  String domainId,@QueryParam("update_options") @Min(0L) @DefaultValue("24")  @ApiParam("One of the following values, or a sum of multiple values: 1 - SubjectsAsSID, 2 - UpdateSubjectAssociations, 8 - UpdatePermissions, 16 - UpdateServicePermissions.")  Long updateOptions,@Valid ApiRoleAdd body) {
        return Response.ok().entity("magic!").build();
    }

    @POST
    @Path("/users")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add domain user", notes = "Adds new Domain user.", response = ApiAddUser201Response.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-users" })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created", response = ApiAddUser201Response.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response addUser(@QueryParam("domain_id") @NotNull  @ApiParam("The domain ID")  String domainId,@QueryParam("update_options") @Min(0L) @DefaultValue("30")  @ApiParam("One of the following values, or a sum of multiple values: 1 - SubjectsAsSID, 2 - UpdateGroupAssociations, 4 - UpdateRoleAssociations, 8 - UpdatePermissions, 16 - UpdateServicePermissions.")  Long updateOptions,@Valid ApiUserAdd body) {
        return Response.ok().entity("magic!").build();
    }

    @POST
    @Path("/licenses/{product_code}/leases")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Activates a product's license for a domain", notes = "Assigns a lease of a license to each specified users.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-licenses" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 404, message = "Not Found", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response assignLicenseLease(@PathParam("product_code") @ApiParam("The product code") String productCode,@QueryParam("domain_id") @NotNull  @ApiParam("The domain ID")  String domainId,@Valid List<String> body) {
        return Response.ok().entity("magic!").build();
    }

    @DELETE
    @Path("/licenses/{product_code}/activation")
    @Produces({ "application/json" })
    @ApiOperation(value = "Deactivates a product's license for a domain", notes = "Deactivates a license performing an online deactivation prior to removing activation info. If offline parameter was specified, activation info are just removed without any online deactivation tentatives.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-licenses" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 404, message = "Not Found", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response deactivateLicense(@PathParam("product_code") @ApiParam("The product code") String productCode,@QueryParam("domain_id") @NotNull  @ApiParam("The domain ID")  String domainId,@QueryParam("offline") @DefaultValue("false")   Boolean offline) {
        return Response.ok().entity("magic!").build();
    }

    @DELETE
    @Path("/api-keys/{apikey_id}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Deletes an API key for a domain", notes = "Deletes the specified API key given its ID.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-apikeys" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 404, message = "Not Found", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response deleteApiKey(@PathParam("apikey_id") @ApiParam("The API Key ID") String apikeyId,@QueryParam("domain_id") @NotNull  @ApiParam("The domain ID")  String domainId) {
        return Response.ok().entity("magic!").build();
    }

    @DELETE
    @Path("/domains/{domain_id}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete domain", notes = "Deletes a Domain.", response = ApiResultExceptions.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-domains" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = ApiResultExceptions.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 404, message = "Not Found", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response deleteDomain(@PathParam("domain_id") String domainId,@QueryParam("deep")  @ApiParam("Specifies whether to remove also all User entries from Directory (if supported)")  Boolean deep) {
        return Response.ok().entity("magic!").build();
    }

    @DELETE
    @Path("/groups/{group_id}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Deletes a domain group", notes = "Deletes a Domain's group.", response = ApiResultExceptions.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-groups" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = ApiResultExceptions.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 404, message = "Not Found", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response deleteGroup(@PathParam("group_id") String groupId,@QueryParam("domain_id") @NotNull  @ApiParam("The domain ID")  String domainId) {
        return Response.ok().entity("magic!").build();
    }

    @DELETE
    @Path("/licenses/{product_code}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Removes a product's license for a domain", notes = "", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-licenses" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 404, message = "Not Found", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response deleteLicense(@PathParam("product_code") @ApiParam("The product code") String productCode,@QueryParam("domain_id") @NotNull  @ApiParam("The domain ID")  String domainId,@QueryParam("force") @DefaultValue("false")   Boolean force) {
        return Response.ok().entity("magic!").build();
    }

    @DELETE
    @Path("/resources/{resource_id}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Deletes a resource for a domain", notes = "Deletes a Domain's resource.", response = ApiResultExceptions.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-resources" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiResultExceptions.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 404, message = "Not Found", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response deleteResource(@PathParam("resource_id") String resourceId,@QueryParam("domain_id") @NotNull  @ApiParam("The domain ID")  String domainId) {
        return Response.ok().entity("magic!").build();
    }

    @DELETE
    @Path("/roles/{role_id}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Deletes a role for a domain", notes = "Deletes a Domain's role.", response = ApiResultExceptions.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-roles" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = ApiResultExceptions.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 404, message = "Not Found", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response deleteRole(@PathParam("role_id") String roleId,@QueryParam("domain_id") @NotNull  @ApiParam("The domain ID")  String domainId) {
        return Response.ok().entity("magic!").build();
    }

    @DELETE
    @Path("/settings/{service_id}/{key}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete setting entry (System/Domain)", notes = "Deletes setting value, System or Domain depending on the paramter.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-settings" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 404, message = "Entry or Domain Not Found", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response deleteSetting(@PathParam("service_id") @ApiParam("Service ID") String serviceId,@PathParam("key") @ApiParam("Setting key") String key,@QueryParam("domain_id")  @ApiParam("The domain ID")  String domainId) {
        return Response.ok().entity("magic!").build();
    }

    @DELETE
    @Path("/users/{user_id}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Deletes a domain user", notes = "Deletes a Domain's user.", response = ApiResultExceptions.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-users" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = ApiResultExceptions.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 404, message = "Not Found", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response deleteUser(@PathParam("user_id") String userId,@QueryParam("domain_id") @NotNull  @ApiParam("The domain ID")  String domainId,@QueryParam("deep")  @ApiParam("Specifies whether to remove also entry from Directory (if supported)")  Boolean deep) {
        return Response.ok().entity("magic!").build();
    }

    @POST
    @Path("/api-keys")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Generate new API key", notes = "Generates new API key for a domain.", response = ApiApiKeyGenerated.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-apikeys" })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created", response = ApiApiKeyGenerated.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response generateApiKey(@QueryParam("domain_id") @NotNull  @ApiParam("The domain ID")  String domainId,@Valid ApiApiKeyBase body) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/api-keys/{apikey_id}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Gets an API key for a domain", notes = "Gets the specified API key given its ID.", response = ApiApiKey.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-apikeys" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiApiKey.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 404, message = "Not Found", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response getApiKey(@PathParam("apikey_id") @ApiParam("The API Key ID") String apikeyId,@QueryParam("domain_id") @NotNull  @ApiParam("The domain ID")  String domainId) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/domains/{domain_id}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get domain", notes = "Get a single Domain.", response = ApiDomain.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-domains" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = ApiDomain.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response getDomain(@PathParam("domain_id") String domainId) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/groups/{group_id}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Gets a domain group", notes = "Get a single Domain's group.", response = ApiGroup.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-groups" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = ApiGroup.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response getGroup(@PathParam("group_id") String groupId,@QueryParam("domain_id") @NotNull  @ApiParam("The domain ID")  String domainId) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/licenses/{product_code}/reqinfo")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get license offline request info for a domain", notes = "Returns info for performing offline activation requests, or for deactivation if specific parameter is turned on.", response = ApiLicenseOfflineReqInfo.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-licenses" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = ApiLicenseOfflineReqInfo.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 404, message = "Not Found", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response getLicenseOfflineReqInfo(@PathParam("product_code") @ApiParam("The product code") String productCode,@QueryParam("domain_id") @NotNull  @ApiParam("The domain ID")  String domainId,@QueryParam("deactivation") @DefaultValue("false")  @ApiParam("Set to return deactivation info instead of activation")  Boolean deactivation) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/resources/{resource_id}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Gets a resource for a domain", notes = "Get a single Domain's resource.", response = ApiResource.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-resources" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiResource.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 404, message = "Not Found", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response getResource(@PathParam("resource_id") String resourceId,@QueryParam("domain_id") @NotNull  @ApiParam("The domain ID")  String domainId) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/roles/{role_id}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Gets a role for a domain", notes = "Get a single Domain's role.", response = ApiRole.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-roles" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = ApiRole.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 404, message = "Not Found", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response getRole(@PathParam("role_id") String roleId,@QueryParam("domain_id") @NotNull  @ApiParam("The domain ID")  String domainId) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/settings/{service_id}/{key}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get setting entry (System/Domain)", notes = "Gets specified setting value, System or Domain depending on the paramter.", response = ApiSettingEntry.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-settings" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = ApiSettingEntry.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 404, message = "Domain Not Found", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response getSetting(@PathParam("service_id") @ApiParam("Service ID") String serviceId,@PathParam("key") @ApiParam("Setting key") String key,@QueryParam("domain_id")  @ApiParam("The domain ID")  String domainId) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/users/{user_id}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Gets a domain user", notes = "Get a single Domain's user.", response = ApiUser.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-users" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = ApiUser.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response getUser(@PathParam("user_id") String userId,@QueryParam("domain_id") @NotNull  @ApiParam("The domain ID")  String domainId) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/api-keys")
    @Produces({ "application/json" })
    @ApiOperation(value = "List API keys", notes = "Returns a list of API keys generated for a domain.", response = ApiApiKey.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-apikeys" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiApiKey.class, responseContainer = "List"),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response listApiKeys(@QueryParam("domain_id") @NotNull  @ApiParam("The domain ID")  String domainId) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/domains")
    @Produces({ "application/json" })
    @ApiOperation(value = "List domains", notes = "Lists Domains.", response = ApiDomainEntry.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-domains" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = ApiDomainEntry.class, responseContainer = "List"),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response listDomains() {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/groups")
    @Produces({ "application/json" })
    @ApiOperation(value = "List domain groups", notes = "Lists Domain's groups.", response = ApiGroup.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-groups" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = ApiGroup.class, responseContainer = "List"),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response listGroups(@QueryParam("domain_id") @NotNull  @ApiParam("The domain ID")  String domainId) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/licenses")
    @Produces({ "application/json" })
    @ApiOperation(value = "List product licenses for a domain", notes = "Lists Domain's product licenses.", response = ApiLicense.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-licenses" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = ApiLicense.class, responseContainer = "List"),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response listLicenses(@QueryParam("domain_id") @NotNull  @ApiParam("The domain ID")  String domainId,@QueryParam("include_builtin") @DefaultValue("true")  @ApiParam("Set to false to discard built-in licenses in results")  Boolean includeBuiltin) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/resources")
    @Produces({ "application/json" })
    @ApiOperation(value = "List resources for a domain", notes = "Lists Domain's resources.", response = ApiResource.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-resources" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = ApiResource.class, responseContainer = "List"),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response listResources(@QueryParam("domain_id") @NotNull  @ApiParam("The domain ID")  String domainId) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/roles")
    @Produces({ "application/json" })
    @ApiOperation(value = "List roles for a domain", notes = "Lists Domain's roles.", response = ApiRole.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-roles" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = ApiRole.class, responseContainer = "List"),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response listRoles(@QueryParam("domain_id") @NotNull  @ApiParam("The domain ID")  String domainId) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/settings")
    @Produces({ "application/json" })
    @ApiOperation(value = "List settings (System/Domain)", notes = "Return a list of all setting entries, System or Domain depending on the paramter.", response = ApiSettingEntry.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-settings" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = ApiSettingEntry.class, responseContainer = "List"),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 404, message = "Domain Not Found", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response listSettings(@QueryParam("domain_id")  @ApiParam("The domain ID")  String domainId) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/users")
    @Produces({ "application/json" })
    @ApiOperation(value = "List domain users", notes = "Lists Domain's users.", response = ApiUser.class, responseContainer = "List", authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-users" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = ApiUser.class, responseContainer = "List"),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response listUsers(@QueryParam("domain_id") @NotNull  @ApiParam("The domain ID")  String domainId) {
        return Response.ok().entity("magic!").build();
    }

    @DELETE
    @Path("/licenses/{product_code}/leases")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Deactivates a product's license for a domain", notes = "Revokes the lease of a license from each specified users.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-licenses" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 404, message = "Not Found", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response revokeLicenseLease(@PathParam("product_code") @ApiParam("The product code") String productCode,@QueryParam("domain_id") @NotNull  @ApiParam("The domain ID")  String domainId,@Valid List<String> body) {
        return Response.ok().entity("magic!").build();
    }

    @PUT
    @Path("/password")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Sets System Administrator password", notes = "Updates system administrator password.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-password" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response setAdminPassword(@Valid String body) {
        return Response.ok().entity("magic!").build();
    }

    @PUT
    @Path("/users/{user_id}/password")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Set user password", notes = "Updates user password.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-users" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 404, message = "Not Found", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response setUserPassword(@PathParam("user_id") String userId,@QueryParam("domain_id") @NotNull  @ApiParam("The domain ID")  String domainId,@Valid String body) {
        return Response.ok().entity("magic!").build();
    }

    @PUT
    @Path("/api-keys/{apikey_id}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Updates an API key for a domain", notes = "Updates the specified API key given its ID: only few fields can be updated.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-apikeys" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 404, message = "Not Found", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response updateApiKey(@PathParam("apikey_id") @ApiParam("The API Key ID") String apikeyId,@QueryParam("domain_id") @NotNull  @ApiParam("The domain ID")  String domainId,@Valid ApiApiKeyBase body) {
        return Response.ok().entity("magic!").build();
    }

    @PUT
    @Path("/domains/{domain_id}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update domain", notes = "Updates a Domain.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-domains" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 404, message = "Not Found", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response updateDomain(@PathParam("domain_id") String domainId,@QueryParam("update_options") @Min(0L) @DefaultValue("0")  @ApiParam("One of the following values, or a sum of multiple values: 1 - UpdateDirectoryPassword, 2 - UpdateDirectoryData.")  Long updateOptions,@Valid ApiDomainBase body) {
        return Response.ok().entity("magic!").build();
    }

    @PUT
    @Path("/groups/{group_id}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Updates a domain group", notes = "Updates a Domain's group.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-groups" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 404, message = "Not Found", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response updateGroup(@PathParam("group_id") String groupId,@QueryParam("domain_id") @NotNull  @ApiParam("The domain ID")  String domainId,@QueryParam("update_options") @Min(0L) @DefaultValue("30")  @ApiParam("One of the following values, or a sum of multiple values: 1 - SubjectsAsSID, 2 - UpdateUserAssociations, 4 - UpdateRoleAssociations, 8 - UpdatePermissions, 16 - UpdateServicePermissions.")  Long updateOptions,@Valid ApiGroupBase body) {
        return Response.ok().entity("magic!").build();
    }

    @PUT
    @Path("/resources/{resource_id}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Updates a resource for a domain", notes = "Updates a Domain's resource.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-resources" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 404, message = "Not Found", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response updateResource(@PathParam("resource_id") String resourceId,@QueryParam("domain_id") @NotNull  @ApiParam("The domain ID")  String domainId,@QueryParam("update_options") @Min(0L) @DefaultValue("2")  @ApiParam("One of the following values, or a sum of multiple values: 1 - SubjectsAsSID, 2 - UpdatePermissions.")  Long updateOptions,@Valid ApiResourceBase body) {
        return Response.ok().entity("magic!").build();
    }

    @PUT
    @Path("/roles/{role_id}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Updates a role for a domain", notes = "Updates a Domain's role.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-roles" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 404, message = "Not Found", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response updateRole(@PathParam("role_id") String roleId,@QueryParam("domain_id") @NotNull  @ApiParam("The domain ID")  String domainId,@QueryParam("update_options") @Min(0L) @DefaultValue("24")  @ApiParam("One of the following values, or a sum of multiple values: 1 - SubjectsAsSID, 2 - UpdateSubjectAssociations, 8 - UpdatePermissions, 16 - UpdateServicePermissions.")  Long updateOptions,@Valid ApiRoleBase body) {
        return Response.ok().entity("magic!").build();
    }

    @PUT
    @Path("/settings/{service_id}/{key}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update setting entry (System/Domain)", notes = "Updates setting value, System or Domain depending on the paramter.", response = Void.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-settings" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 404, message = "Domain Not Found", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response updateSetting(@PathParam("service_id") @ApiParam("Service ID") String serviceId,@PathParam("key") @ApiParam("Setting key") String key,@QueryParam("domain_id")  @ApiParam("The domain ID")  String domainId,@Valid String body) {
        return Response.ok().entity("magic!").build();
    }

    @PUT
    @Path("/users/{user_id}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Updates a domain user", notes = "Updates a Domain's user.", response = ApiError.class, authorizations = {
        
        @Authorization(value = "auth-bearer"),
        
        @Authorization(value = "auth-basic")
         }, tags={ "admin-users" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = ApiError.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ApiError.class),
        @ApiResponse(code = 404, message = "Not Found", response = ApiError.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class)
    })
    public Response updateUser(@PathParam("user_id") String userId,@QueryParam("domain_id") @NotNull  @ApiParam("The domain ID")  String domainId,@QueryParam("update_options") @Min(0L) @DefaultValue("30")  @ApiParam("One of the following values, or a sum of multiple values: 1 - SubjectsAsSID, 2 - UpdateGroupAssociations, 4 - UpdateRoleAssociations, 8 - UpdatePermissions, 16 - UpdateServicePermissions.")  Long updateOptions,@Valid ApiUserBase body) {
        return Response.ok().entity("magic!").build();
    }
}
