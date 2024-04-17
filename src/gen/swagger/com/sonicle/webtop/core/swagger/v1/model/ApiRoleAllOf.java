package com.sonicle.webtop.core.swagger.v1.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;
import javax.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;



@JsonTypeName("Role_allOf")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-04-16T12:49:23.936+02:00[Europe/Berlin]")
public class ApiRoleAllOf   {
  private @Valid String roleId;
  private @Valid String roleSid;

  /**
   * Role ID, actually its name.
   **/
  public ApiRoleAllOf roleId(String roleId) {
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
   * Role SID (internal Subject IDentifier), auto-generated GUID.
   **/
  public ApiRoleAllOf roleSid(String roleSid) {
    this.roleSid = roleSid;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Role SID (internal Subject IDentifier), auto-generated GUID.")
  @JsonProperty("roleSid")
  @NotNull
  public String getRoleSid() {
    return roleSid;
  }

  @JsonProperty("roleSid")
  public void setRoleSid(String roleSid) {
    this.roleSid = roleSid;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiRoleAllOf roleAllOf = (ApiRoleAllOf) o;
    return Objects.equals(this.roleId, roleAllOf.roleId) &&
        Objects.equals(this.roleSid, roleAllOf.roleSid);
  }

  @Override
  public int hashCode() {
    return Objects.hash(roleId, roleSid);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiRoleAllOf {\n");
    
    sb.append("    roleId: ").append(toIndentedString(roleId)).append("\n");
    sb.append("    roleSid: ").append(toIndentedString(roleSid)).append("\n");
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

