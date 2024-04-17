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
 * The group data for listing purposes.
 **/
@ApiModel(description = "The group data for listing purposes.")
@JsonTypeName("Group")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-04-16T12:49:23.936+02:00[Europe/Berlin]")
public class ApiGroup   {
  private @Valid String groupId;
  private @Valid String groupSid;
  private @Valid Boolean builtIn;
  private @Valid String description;
  private @Valid List<String> assignedUsers = null;
  private @Valid List<String> assignedRoles = null;
  private @Valid List<String> permissions = null;
  private @Valid List<String> allowedServiceIds = null;

  /**
   * Group ID, actually its name.
   **/
  public ApiGroup groupId(String groupId) {
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
   * Group SID (internal Subject IDentifier), auto-generated GUID.
   **/
  public ApiGroup groupSid(String groupSid) {
    this.groupSid = groupSid;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Group SID (internal Subject IDentifier), auto-generated GUID.")
  @JsonProperty("groupSid")
  @NotNull
  public String getGroupSid() {
    return groupSid;
  }

  @JsonProperty("groupSid")
  public void setGroupSid(String groupSid) {
    this.groupSid = groupSid;
  }

  /**
   * Specifies if it is an auto-generated Group.
   **/
  public ApiGroup builtIn(Boolean builtIn) {
    this.builtIn = builtIn;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Specifies if it is an auto-generated Group.")
  @JsonProperty("builtIn")
  @NotNull
  public Boolean getBuiltIn() {
    return builtIn;
  }

  @JsonProperty("builtIn")
  public void setBuiltIn(Boolean builtIn) {
    this.builtIn = builtIn;
  }

  /**
   * Descriptive info about this Group.
   **/
  public ApiGroup description(String description) {
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
  public ApiGroup assignedUsers(List<String> assignedUsers) {
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

  public ApiGroup addAssignedUsersItem(String assignedUsersItem) {
    if (this.assignedUsers == null) {
      this.assignedUsers = new ArrayList<>();
    }

    this.assignedUsers.add(assignedUsersItem);
    return this;
  }

  public ApiGroup removeAssignedUsersItem(String assignedUsersItem) {
    if (assignedUsersItem != null && this.assignedUsers != null) {
      this.assignedUsers.remove(assignedUsersItem);
    }

    return this;
  }
  /**
   * One or more references to Roles assigned to the User. They will be interpreted as IDs or SIDs according to a parameter.
   **/
  public ApiGroup assignedRoles(List<String> assignedRoles) {
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

  public ApiGroup addAssignedRolesItem(String assignedRolesItem) {
    if (this.assignedRoles == null) {
      this.assignedRoles = new ArrayList<>();
    }

    this.assignedRoles.add(assignedRolesItem);
    return this;
  }

  public ApiGroup removeAssignedRolesItem(String assignedRolesItem) {
    if (assignedRolesItem != null && this.assignedRoles != null) {
      this.assignedRoles.remove(assignedRolesItem);
    }

    return this;
  }
  /**
   * One or more permissions describing a right granted to the User. They are in the form of permission string.
   **/
  public ApiGroup permissions(List<String> permissions) {
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

  public ApiGroup addPermissionsItem(String permissionsItem) {
    if (this.permissions == null) {
      this.permissions = new ArrayList<>();
    }

    this.permissions.add(permissionsItem);
    return this;
  }

  public ApiGroup removePermissionsItem(String permissionsItem) {
    if (permissionsItem != null && this.permissions != null) {
      this.permissions.remove(permissionsItem);
    }

    return this;
  }
  /**
   * One or more references to service IDs to which the User has access.
   **/
  public ApiGroup allowedServiceIds(List<String> allowedServiceIds) {
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

  public ApiGroup addAllowedServiceIdsItem(String allowedServiceIdsItem) {
    if (this.allowedServiceIds == null) {
      this.allowedServiceIds = new ArrayList<>();
    }

    this.allowedServiceIds.add(allowedServiceIdsItem);
    return this;
  }

  public ApiGroup removeAllowedServiceIdsItem(String allowedServiceIdsItem) {
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
    ApiGroup group = (ApiGroup) o;
    return Objects.equals(this.groupId, group.groupId) &&
        Objects.equals(this.groupSid, group.groupSid) &&
        Objects.equals(this.builtIn, group.builtIn) &&
        Objects.equals(this.description, group.description) &&
        Objects.equals(this.assignedUsers, group.assignedUsers) &&
        Objects.equals(this.assignedRoles, group.assignedRoles) &&
        Objects.equals(this.permissions, group.permissions) &&
        Objects.equals(this.allowedServiceIds, group.allowedServiceIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(groupId, groupSid, builtIn, description, assignedUsers, assignedRoles, permissions, allowedServiceIds);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiGroup {\n");
    
    sb.append("    groupId: ").append(toIndentedString(groupId)).append("\n");
    sb.append("    groupSid: ").append(toIndentedString(groupSid)).append("\n");
    sb.append("    builtIn: ").append(toIndentedString(builtIn)).append("\n");
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

