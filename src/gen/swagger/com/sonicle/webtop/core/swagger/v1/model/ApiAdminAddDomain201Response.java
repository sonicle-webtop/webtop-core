package com.sonicle.webtop.core.swagger.v1.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sonicle.webtop.core.swagger.v1.model.ApiDomain;
import com.sonicle.webtop.core.swagger.v1.model.ApiHomedException;
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



@JsonTypeName("adminAddDomain_201_response")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-04-16T12:49:23.936+02:00[Europe/Berlin]")
public class ApiAdminAddDomain201Response   {
  private @Valid List<ApiHomedException> exceptions = null;
  private @Valid ApiDomain value;

  /**
   **/
  public ApiAdminAddDomain201Response exceptions(List<ApiHomedException> exceptions) {
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

  public ApiAdminAddDomain201Response addExceptionsItem(ApiHomedException exceptionsItem) {
    if (this.exceptions == null) {
      this.exceptions = new ArrayList<>();
    }

    this.exceptions.add(exceptionsItem);
    return this;
  }

  public ApiAdminAddDomain201Response removeExceptionsItem(ApiHomedException exceptionsItem) {
    if (exceptionsItem != null && this.exceptions != null) {
      this.exceptions.remove(exceptionsItem);
    }

    return this;
  }
  /**
   **/
  public ApiAdminAddDomain201Response value(ApiDomain value) {
    this.value = value;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("value")
  public ApiDomain getValue() {
    return value;
  }

  @JsonProperty("value")
  public void setValue(ApiDomain value) {
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
    ApiAdminAddDomain201Response adminAddDomain201Response = (ApiAdminAddDomain201Response) o;
    return Objects.equals(this.exceptions, adminAddDomain201Response.exceptions) &&
        Objects.equals(this.value, adminAddDomain201Response.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(exceptions, value);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiAdminAddDomain201Response {\n");
    
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

