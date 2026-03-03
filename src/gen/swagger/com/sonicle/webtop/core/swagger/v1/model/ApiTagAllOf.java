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



@JsonTypeName("Tag_allOf")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-02-27T16:20:59.935+01:00[Europe/Berlin]")
public class ApiTagAllOf   {
  private @Valid String id;
  private @Valid Boolean builtIn;

  /**
   **/
  public ApiTagAllOf id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("id")
  @NotNull
  public String getId() {
    return id;
  }

  @JsonProperty("id")
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public ApiTagAllOf builtIn(Boolean builtIn) {
    this.builtIn = builtIn;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("builtIn")
  @NotNull
  public Boolean getBuiltIn() {
    return builtIn;
  }

  @JsonProperty("builtIn")
  public void setBuiltIn(Boolean builtIn) {
    this.builtIn = builtIn;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiTagAllOf tagAllOf = (ApiTagAllOf) o;
    return Objects.equals(this.id, tagAllOf.id) &&
        Objects.equals(this.builtIn, tagAllOf.builtIn);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, builtIn);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiTagAllOf {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    builtIn: ").append(toIndentedString(builtIn)).append("\n");
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

