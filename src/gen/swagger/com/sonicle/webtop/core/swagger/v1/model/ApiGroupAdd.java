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
 * The group data for insertion.
 **/
@ApiModel(description = "The group data for insertion.")
@JsonTypeName("GroupAdd")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-04-16T12:49:23.936+02:00[Europe/Berlin]")
public class ApiGroupAdd   {
  private @Valid String groupId;
  private @Valid String description;
  private @Valid List<String> assignedUsers = null;
  private @Valid List<String> assignedRoles = null;
  private @Valid List<String> permissions = null;
  private @Valid List<String> allowedServiceIds = null;

  /**
   * Group ID, actually its name.
   **/
  public ApiGroupAdd groupId(String groupId) {
    this.groupId = groupId;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Group ID, actually its name.")
  @JsonProperty("groupId")
  @NotNull
  public String getGroupId() {
    return groupId;
  }

  @JsonProperty("groupId")
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  /**
   * Descriptive info about this Group.
   **/
  public ApiGroupAdd description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Descriptive info about this Group.")
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
   * One or more references to Users belonging to this Group. They will be interpreted as IDs or SIDs according to a parameter.
   **/
  public ApiGroupAdd assignedUsers(List<String> assignedUsers) {
    this.assignedUsers = assignedUsers;
    return this;
  }

  
  @ApiModelProperty(value = "One or more references to Users belonging to this Group. They will be interpreted as IDs or SIDs according to a parameter.")
  @JsonProperty("assignedUsers")
  public List<String> getAssignedUsers() {
    return assignedUsers;
  }

  @JsonProperty("assignedUsers")
  public void setAssignedUsers(List<String> assignedUsers) {
    this.assignedUsers = assignedUsers;
  }

  public ApiGroupAdd addAssignedUsersItem(String assignedUsersItem) {
    if (this.assignedUsers == null) {
      this.assignedUsers = new ArrayList<>();
    }

    this.assignedUsers.add(assignedUsersItem);
    return this;
  }

  public ApiGroupAdd removeAssignedUsersItem(String assignedUsersItem) {
    if (assignedUsersItem != null && this.assignedUsers != null) {
      this.assignedUsers.remove(assignedUsersItem);
    }

    return this;
  }
  /**
   * One or more references to Roles assigned to the User. They will be interpreted as IDs or SIDs according to a parameter.
   **/
  public ApiGroupAdd assignedRoles(List<String> assignedRoles) {
    this.assignedRoles = assignedRoles;
    return this;
  }

  
  @ApiModelProperty(value = "One or more references to Roles assigned to the User. They will be interpreted as IDs or SIDs according to a parameter.")
  @JsonProperty("assignedRoles")
  public List<String> getAssignedRoles() {
    return assignedRoles;
  }

  @JsonProperty("assignedRoles")
  public void setAssignedRoles(List<String> assignedRoles) {
    this.assignedRoles = assignedRoles;
  }

  public ApiGroupAdd addAssignedRolesItem(String assignedRolesItem) {
    if (this.assignedRoles == null) {
      this.assignedRoles = new ArrayList<>();
    }

    this.assignedRoles.add(assignedRolesItem);
    return this;
  }

  public ApiGroupAdd removeAssignedRolesItem(String assignedRolesItem) {
    if (assignedRolesItem != null && this.assignedRoles != null) {
      this.assignedRoles.remove(assignedRolesItem);
    }

    return this;
  }
  /**
   * One or more permissions describing a right granted to the User. They are in the form of permission string.
   **/
  public ApiGroupAdd permissions(List<String> permissions) {
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

  public ApiGroupAdd addPermissionsItem(String permissionsItem) {
    if (this.permissions == null) {
      this.permissions = new ArrayList<>();
    }

    this.permissions.add(permissionsItem);
    return this;
  }

  public ApiGroupAdd removePermissionsItem(String permissionsItem) {
    if (permissionsItem != null && this.permissions != null) {
      this.permissions.remove(permissionsItem);
    }

    return this;
  }
  /**
   * One or more references to service IDs to which the User has access.
   **/
  public ApiGroupAdd allowedServiceIds(List<String> allowedServiceIds) {
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

  public ApiGroupAdd addAllowedServiceIdsItem(String allowedServiceIdsItem) {
    if (this.allowedServiceIds == null) {
      this.allowedServiceIds = new ArrayList<>();
    }

    this.allowedServiceIds.add(allowedServiceIdsItem);
    return this;
  }

  public ApiGroupAdd removeAllowedServiceIdsItem(String allowedServiceIdsItem) {
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
    ApiGroupAdd groupAdd = (ApiGroupAdd) o;
    return Objects.equals(this.groupId, groupAdd.groupId) &&
        Objects.equals(this.description, groupAdd.description) &&
        Objects.equals(this.assignedUsers, groupAdd.assignedUsers) &&
        Objects.equals(this.assignedRoles, groupAdd.assignedRoles) &&
        Objects.equals(this.permissions, groupAdd.permissions) &&
        Objects.equals(this.allowedServiceIds, groupAdd.allowedServiceIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(groupId, description, assignedUsers, assignedRoles, permissions, allowedServiceIds);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiGroupAdd {\n");
    
    sb.append("    groupId: ").append(toIndentedString(groupId)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    assignedUsers: ").append(toIndentedString(assignedUsers)).append("\n");
    sb.append("    assignedRoles: ").append(toIndentedString(assignedRoles)).append("\n");
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

