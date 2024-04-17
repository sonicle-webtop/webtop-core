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

/**
 * Represents a non-blocking exception captured for a service: it provides the originating service ID, the source className and the message of the exception.
 **/
@ApiModel(description = "Represents a non-blocking exception captured for a service: it provides the originating service ID, the source className and the message of the exception.")
@JsonTypeName("HomedException")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-04-16T12:49:23.936+02:00[Europe/Berlin]")
public class ApiHomedException   {
  private @Valid String serviceId;
  private @Valid String className;
  private @Valid String message;

  /**
   **/
  public ApiHomedException serviceId(String serviceId) {
    this.serviceId = serviceId;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("serviceId")
  @NotNull
  public String getServiceId() {
    return serviceId;
  }

  @JsonProperty("serviceId")
  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  /**
   **/
  public ApiHomedException className(String className) {
    this.className = className;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("className")
  @NotNull
  public String getClassName() {
    return className;
  }

  @JsonProperty("className")
  public void setClassName(String className) {
    this.className = className;
  }

  /**
   **/
  public ApiHomedException message(String message) {
    this.message = message;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("message")
  public String getMessage() {
    return message;
  }

  @JsonProperty("message")
  public void setMessage(String message) {
    this.message = message;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiHomedException homedException = (ApiHomedException) o;
    return Objects.equals(this.serviceId, homedException.serviceId) &&
        Objects.equals(this.className, homedException.className) &&
        Objects.equals(this.message, homedException.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(serviceId, className, message);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiHomedException {\n");
    
    sb.append("    serviceId: ").append(toIndentedString(serviceId)).append("\n");
    sb.append("    className: ").append(toIndentedString(className)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
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

