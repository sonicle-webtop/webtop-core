package com.sonicle.webtop.core.swagger.v1.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * The role data for insertion.
 **/
@ApiModel(description = "The role data for insertion.")
@JsonTypeName("RoleAdd")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-04-16T12:49:23.936+02:00[Europe/Berlin]")
public class ApiRoleAdd   {
  private @Valid String roleId;
  private @Valid String description;
  private @Valid List<String> permissions = null;
  private @Valid List<String> allowedServiceIds = null;

  /**
   * Role ID, actually its name.
   **/
  public ApiRoleAdd roleId(String roleId) {
    this.roleId = roleId;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Role ID, actually its name.")
  @JsonProperty("roleId")
  @NotNull
  public String getRoleId() {
    return roleId;
  }

  @JsonProperty("roleId")
  public void setRoleId(String roleId) {
    this.roleId = roleId;
  }

  /**
   * Descriptive info about this Role.
   **/
  public ApiRoleAdd description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Descriptive info about this Role.")
  @JsonProperty("description")
  @NotNull
  public String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * One or more permissions describing a right granted to the User. They are in the form of permission string.
   **/
  public ApiRoleAdd permissions(List<String> permissions) {
    this.permissions = permissions;
    return this;
  }

  
  @ApiModelProperty(value = "One or more permissions describing a right granted to the User. They are in the form of permission string.")
  @JsonProperty("permissions")
  public List<String> getPermissions() {
    return permissions;
  }

  @JsonProperty("permissions")
  public void setPermissions(List<String> permissions) {
    this.permissions = permissions;
  }

  public ApiRoleAdd addPermissionsItem(String permissionsItem) {
    if (this.permissions == null) {
      this.permissions = new ArrayList<>();
    }

    this.permissions.add(permissionsItem);
    return this;
  }

  public ApiRoleAdd removePermissionsItem(String permissionsItem) {
    if (permissionsItem != null && this.permissions != null) {
      this.permissions.remove(permissionsItem);
    }

    return this;
  }
  /**
   * One or more references to service IDs to which the User has access.
   **/
  public ApiRoleAdd allowedServiceIds(List<String> allowedServiceIds) {
    this.allowedServiceIds = allowedServiceIds;
    return this;
  }

  
  @ApiModelProperty(value = "One or more references to service IDs to which the User has access.")
  @JsonProperty("allowedServiceIds")
  public List<String> getAllowedServiceIds() {
    return allowedServiceIds;
  }

  @JsonProperty("allowedServiceIds")
  public void setAllowedServiceIds(List<String> allowedServiceIds) {
    this.allowedServiceIds = allowedServiceIds;
  }

  public ApiRoleAdd addAllowedServiceIdsItem(String allowedServiceIdsItem) {
    if (this.allowedServiceIds == null) {
      this.allowedServiceIds = new ArrayList<>();
    }

    this.allowedServiceIds.add(allowedServiceIdsItem);
    return this;
  }

  public ApiRoleAdd removeAllowedServiceIdsItem(String allowedServiceIdsItem) {
    if (allowedServiceIdsItem != null && this.allowedServiceIds != null) {
      this.allowedServiceIds.remove(allowedServiceIdsItem);
    }

    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiRoleAdd roleAdd = (ApiRoleAdd) o;
    return Objects.equals(this.roleId, roleAdd.roleId) &&
        Objects.equals(this.description, roleAdd.description) &&
        Objects.equals(this.permissions, roleAdd.permissions) &&
        Objects.equals(this.allowedServiceIds, roleAdd.allowedServiceIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(roleId, description, permissions, allowedServiceIds);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiRoleAdd {\n");
    
    sb.append("    roleId: ").append(toIndentedString(roleId)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    permissions: ").append(toIndentedString(permissions)).append("\n");
    sb.append("    allowedServiceIds: ").append(toIndentedString(allowedServiceIds)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }


}

