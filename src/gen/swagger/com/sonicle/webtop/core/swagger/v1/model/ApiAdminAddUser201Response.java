package com.sonicle.webtop.core.swagger.v1.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sonicle.webtop.core.swagger.v1.model.ApiHomedException;
import com.sonicle.webtop.core.swagger.v1.model.ApiUser;
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



@JsonTypeName("adminAddUser_201_response")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-04-16T12:49:23.936+02:00[Europe/Berlin]")
public class ApiAdminAddUser201Response   {
  private @Valid List<ApiHomedException> exceptions = null;
  private @Valid ApiUser value;

  /**
   **/
  public ApiAdminAddUser201Response exceptions(List<ApiHomedException> exceptions) {
    this.exceptions = exceptions;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("exceptions")
  public List<ApiHomedException> getExceptions() {
    return exceptions;
  }

  @JsonProperty("exceptions")
  public void setExceptions(List<ApiHomedException> exceptions) {
    this.exceptions = exceptions;
  }

  public ApiAdminAddUser201Response addExceptionsItem(ApiHomedException exceptionsItem) {
    if (this.exceptions == null) {
      this.exceptions = new ArrayList<>();
    }

    this.exceptions.add(exceptionsItem);
    return this;
  }

  public ApiAdminAddUser201Response removeExceptionsItem(ApiHomedException exceptionsItem) {
    if (exceptionsItem != null && this.exceptions != null) {
      this.exceptions.remove(exceptionsItem);
    }

    return this;
  }
  /**
   **/
  public ApiAdminAddUser201Response value(ApiUser value) {
    this.value = value;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("value")
  public ApiUser getValue() {
    return value;
  }

  @JsonProperty("value")
  public void setValue(ApiUser value) {
    this.value = value;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiAdminAddUser201Response adminAddUser201Response = (ApiAdminAddUser201Response) o;
    return Objects.equals(this.exceptions, adminAddUser201Response.exceptions) &&
        Objects.equals(this.value, adminAddUser201Response.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(exceptions, value);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiAdminAddUser201Response {\n");
    
    sb.append("    exceptions: ").append(toIndentedString(exceptions)).append("\n");
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
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

