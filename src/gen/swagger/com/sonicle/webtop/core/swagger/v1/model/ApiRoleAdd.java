package com.sonicle.webtop.core.swagger.v1.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sonicle.webtop.core.swagger.v1.model.ApiRoleBase;
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
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-03-16T17:01:06.128+01:00[Europe/Berlin]")
public class ApiRoleAdd extends ApiRoleBase  {
  private @Valid String roleId;

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
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(roleId, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiRoleAdd {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
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

