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
 * The user data for insertion.
 **/
@ApiModel(description = "The user data for insertion.")
@JsonTypeName("UserAdd")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-04-16T12:49:23.936+02:00[Europe/Berlin]")
public class ApiUserAdd   {
  private @Valid String userId;
  private @Valid String password;
  private @Valid Boolean enabled;
  private @Valid String displayName;
  private @Valid String firstName;
  private @Valid String lastName;
  private @Valid List<String> assignedGroups = null;
  private @Valid List<String> assignedRoles = null;
  private @Valid List<String> permissions = null;
  private @Valid List<String> allowedServiceIds = null;

  /**
   * User ID, actually its name.
   **/
  public ApiUserAdd userId(String userId) {
    this.userId = userId;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "User ID, actually its name.")
  @JsonProperty("userId")
  @NotNull
  public String getUserId() {
    return userId;
  }

  @JsonProperty("userId")
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * Secret string for logging the user in.
   **/
  public ApiUserAdd password(String password) {
    this.password = password;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Secret string for logging the user in.")
  @JsonProperty("password")
  @NotNull
  public String getPassword() {
    return password;
  }

  @JsonProperty("password")
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Represent the availability status
   **/
  public ApiUserAdd enabled(Boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Represent the availability status")
  @JsonProperty("enabled")
  @NotNull
  public Boolean getEnabled() {
    return enabled;
  }

  @JsonProperty("enabled")
  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Descriptive name for display purposes
   **/
  public ApiUserAdd displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Descriptive name for display purposes")
  @JsonProperty("displayName")
  @NotNull
  public String getDisplayName() {
    return displayName;
  }

  @JsonProperty("displayName")
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * User&#39;s first name
   **/
  public ApiUserAdd firstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  
  @ApiModelProperty(value = "User's first name")
  @JsonProperty("firstName")
  public String getFirstName() {
    return firstName;
  }

  @JsonProperty("firstName")
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  /**
   * User&#39;s last name
   **/
  public ApiUserAdd lastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

  
  @ApiModelProperty(value = "User's last name")
  @JsonProperty("lastName")
  public String getLastName() {
    return lastName;
  }

  @JsonProperty("lastName")
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  /**
   * One or more references to Groups to which the User belongs. They will be interpreted as IDs or SIDs according to a parameter.
   **/
  public ApiUserAdd assignedGroups(List<String> assignedGroups) {
    this.assignedGroups = assignedGroups;
    return this;
  }

  
  @ApiModelProperty(value = "One or more references to Groups to which the User belongs. They will be interpreted as IDs or SIDs according to a parameter.")
  @JsonProperty("assignedGroups")
  public List<String> getAssignedGroups() {
    return assignedGroups;
  }

  @JsonProperty("assignedGroups")
  public void setAssignedGroups(List<String> assignedGroups) {
    this.assignedGroups = assignedGroups;
  }

  public ApiUserAdd addAssignedGroupsItem(String assignedGroupsItem) {
    if (this.assignedGroups == null) {
      this.assignedGroups = new ArrayList<>();
    }

    this.assignedGroups.add(assignedGroupsItem);
    return this;
  }

  public ApiUserAdd removeAssignedGroupsItem(String assignedGroupsItem) {
    if (assignedGroupsItem != null && this.assignedGroups != null) {
      this.assignedGroups.remove(assignedGroupsItem);
    }

    return this;
  }
  /**
   * One or more references to Roles assigned to the User. They will be interpreted as IDs or SIDs according to a parameter.
   **/
  public ApiUserAdd assignedRoles(List<String> assignedRoles) {
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

  public ApiUserAdd addAssignedRolesItem(String assignedRolesItem) {
    if (this.assignedRoles == null) {
      this.assignedRoles = new ArrayList<>();
    }

    this.assignedRoles.add(assignedRolesItem);
    return this;
  }

  public ApiUserAdd removeAssignedRolesItem(String assignedRolesItem) {
    if (assignedRolesItem != null && this.assignedRoles != null) {
      this.assignedRoles.remove(assignedRolesItem);
    }

    return this;
  }
  /**
   * One or more permissions describing a right granted to the User. They are in the form of permission string.
   **/
  public ApiUserAdd permissions(List<String> permissions) {
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

  public ApiUserAdd addPermissionsItem(String permissionsItem) {
    if (this.permissions == null) {
      this.permissions = new ArrayList<>();
    }

    this.permissions.add(permissionsItem);
    return this;
  }

  public ApiUserAdd removePermissionsItem(String permissionsItem) {
    if (permissionsItem != null && this.permissions != null) {
      this.permissions.remove(permissionsItem);
    }

    return this;
  }
  /**
   * One or more references to service IDs to which the User has access.
   **/
  public ApiUserAdd allowedServiceIds(List<String> allowedServiceIds) {
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

  public ApiUserAdd addAllowedServiceIdsItem(String allowedServiceIdsItem) {
    if (this.allowedServiceIds == null) {
      this.allowedServiceIds = new ArrayList<>();
    }

    this.allowedServiceIds.add(allowedServiceIdsItem);
    return this;
  }

  public ApiUserAdd removeAllowedServiceIdsItem(String allowedServiceIdsItem) {
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
    ApiUserAdd userAdd = (ApiUserAdd) o;
    return Objects.equals(this.userId, userAdd.userId) &&
        Objects.equals(this.password, userAdd.password) &&
        Objects.equals(this.enabled, userAdd.enabled) &&
        Objects.equals(this.displayName, userAdd.displayName) &&
        Objects.equals(this.firstName, userAdd.firstName) &&
        Objects.equals(this.lastName, userAdd.lastName) &&
        Objects.equals(this.assignedGroups, userAdd.assignedGroups) &&
        Objects.equals(this.assignedRoles, userAdd.assignedRoles) &&
        Objects.equals(this.permissions, userAdd.permissions) &&
        Objects.equals(this.allowedServiceIds, userAdd.allowedServiceIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, password, enabled, displayName, firstName, lastName, assignedGroups, assignedRoles, permissions, allowedServiceIds);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiUserAdd {\n");
    
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    password: ").append(toIndentedString(password)).append("\n");
    sb.append("    enabled: ").append(toIndentedString(enabled)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    firstName: ").append(toIndentedString(firstName)).append("\n");
    sb.append("    lastName: ").append(toIndentedString(lastName)).append("\n");
    sb.append("    assignedGroups: ").append(toIndentedString(assignedGroups)).append("\n");
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

