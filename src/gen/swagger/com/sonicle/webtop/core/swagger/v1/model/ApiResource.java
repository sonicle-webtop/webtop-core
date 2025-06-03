package com.sonicle.webtop.core.swagger.v1.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sonicle.webtop.core.swagger.v1.model.ApiResourceBase;
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
 * The resource data for listing purposes.
 **/
@ApiModel(description = "The resource data for listing purposes.")
@JsonTypeName("Resource")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-06-03T16:46:12.383+02:00[Europe/Berlin]")
public class ApiResource extends ApiResourceBase  {
  private @Valid String resourceId;
  private @Valid String resourceSid;

  /**
   * Resource ID, actually its name.
   **/
  public ApiResource resourceId(String resourceId) {
    this.resourceId = resourceId;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Resource ID, actually its name.")
  @JsonProperty("resourceId")
  @NotNull
  public String getResourceId() {
    return resourceId;
  }

  @JsonProperty("resourceId")
  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  /**
   * Resource SID (internal Subject IDentifier), auto-generated GUID.
   **/
  public ApiResource resourceSid(String resourceSid) {
    this.resourceSid = resourceSid;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Resource SID (internal Subject IDentifier), auto-generated GUID.")
  @JsonProperty("resourceSid")
  @NotNull
  public String getResourceSid() {
    return resourceSid;
  }

  @JsonProperty("resourceSid")
  public void setResourceSid(String resourceSid) {
    this.resourceSid = resourceSid;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiResource resource = (ApiResource) o;
    return Objects.equals(this.resourceId, resource.resourceId) &&
        Objects.equals(this.resourceSid, resource.resourceSid) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(resourceId, resourceSid, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiResource {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    resourceId: ").append(toIndentedString(resourceId)).append("\n");
    sb.append("    resourceSid: ").append(toIndentedString(resourceSid)).append("\n");
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

