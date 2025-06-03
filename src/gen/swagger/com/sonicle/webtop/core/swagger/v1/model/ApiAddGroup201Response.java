package com.sonicle.webtop.core.swagger.v1.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sonicle.webtop.core.swagger.v1.model.ApiGroup;
import com.sonicle.webtop.core.swagger.v1.model.ApiHomedException;
import com.sonicle.webtop.core.swagger.v1.model.ApiResultExceptions;
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



@JsonTypeName("addGroup_201_response")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-06-03T16:46:12.383+02:00[Europe/Berlin]")
public class ApiAddGroup201Response extends ApiResultExceptions  {
  private @Valid ApiGroup value;

  /**
   **/
  public ApiAddGroup201Response value(ApiGroup value) {
    this.value = value;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("value")
  public ApiGroup getValue() {
    return value;
  }

  @JsonProperty("value")
  public void setValue(ApiGroup value) {
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
    ApiAddGroup201Response addGroup201Response = (ApiAddGroup201Response) o;
    return Objects.equals(this.value, addGroup201Response.value) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiAddGroup201Response {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
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

