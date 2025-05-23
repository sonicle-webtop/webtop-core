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



@JsonTypeName("RoleAdd_allOf")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-04-16T12:49:23.936+02:00[Europe/Berlin]")
public class ApiRoleAddAllOf   {
  private @Valid String roleId;

  /**
   * Role ID, actually its name.
   **/
  public ApiRoleAddAllOf roleId(String roleId) {
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


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiRoleAddAllOf roleAddAllOf = (ApiRoleAddAllOf) o;
    return Objects.equals(this.roleId, roleAddAllOf.roleId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(roleId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiRoleAddAllOf {\n");
    
    sb.append("    roleId: ").append(toIndentedString(roleId)).append("\n");
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

