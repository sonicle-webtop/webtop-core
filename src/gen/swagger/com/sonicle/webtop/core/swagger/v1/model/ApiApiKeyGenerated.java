package com.sonicle.webtop.core.swagger.v1.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sonicle.webtop.core.swagger.v1.model.ApiApiKey;
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
 * Represents a newly generated apiKey object.
 **/
@ApiModel(description = "Represents a newly generated apiKey object.")
@JsonTypeName("ApiKeyGenerated")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-03-16T17:01:06.128+01:00[Europe/Berlin]")
public class ApiApiKeyGenerated extends ApiApiKey  {
  private @Valid String secretToken;

  /**
   * The newly generated plain API Key secret token.
   **/
  public ApiApiKeyGenerated secretToken(String secretToken) {
    this.secretToken = secretToken;
    return this;
  }

  
  @ApiModelProperty(value = "The newly generated plain API Key secret token.")
  @JsonProperty("secretToken")
  public String getSecretToken() {
    return secretToken;
  }

  @JsonProperty("secretToken")
  public void setSecretToken(String secretToken) {
    this.secretToken = secretToken;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiApiKeyGenerated apiKeyGenerated = (ApiApiKeyGenerated) o;
    return Objects.equals(this.secretToken, apiKeyGenerated.secretToken) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(secretToken, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiApiKeyGenerated {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    secretToken: ").append(toIndentedString(secretToken)).append("\n");
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

