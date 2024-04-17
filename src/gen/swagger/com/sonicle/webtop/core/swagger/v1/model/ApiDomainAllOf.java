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



@JsonTypeName("Domain_allOf")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-04-16T12:49:23.936+02:00[Europe/Berlin]")
public class ApiDomainAllOf   {
  private @Valid String domainId;

  /**
   * Domain ID
   **/
  public ApiDomainAllOf domainId(String domainId) {
    this.domainId = domainId;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Domain ID")
  @JsonProperty("domainId")
  @NotNull
  public String getDomainId() {
    return domainId;
  }

  @JsonProperty("domainId")
  public void setDomainId(String domainId) {
    this.domainId = domainId;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiDomainAllOf domainAllOf = (ApiDomainAllOf) o;
    return Objects.equals(this.domainId, domainAllOf.domainId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(domainId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiDomainAllOf {\n");
    
    sb.append("    domainId: ").append(toIndentedString(domainId)).append("\n");
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

