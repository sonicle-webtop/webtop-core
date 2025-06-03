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
 * Represents an apiKey object with base updateable fields.
 **/
@ApiModel(description = "Represents an apiKey object with base updateable fields.")
@JsonTypeName("ApiKeyBase")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-05-30T15:27:46.249+02:00[Europe/Berlin]")
public class ApiApiKeyBase   {
  private @Valid String name;
  private @Valid String description;
  private @Valid String expiresAt;
  private @Valid String shortToken;
  private @Valid String longToken;

  /**
   **/
  public ApiApiKeyBase name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("name")
  @NotNull
  public String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public ApiApiKeyBase description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Expiration timestamp in ISO 8601 format and UTC time.
   **/
  public ApiApiKeyBase expiresAt(String expiresAt) {
    this.expiresAt = expiresAt;
    return this;
  }

  
  @ApiModelProperty(value = "Expiration timestamp in ISO 8601 format and UTC time.")
  @JsonProperty("expiresAt")
  public String getExpiresAt() {
    return expiresAt;
  }

  @JsonProperty("expiresAt")
  public void setExpiresAt(String expiresAt) {
    this.expiresAt = expiresAt;
  }

  /**
   **/
  public ApiApiKeyBase shortToken(String shortToken) {
    this.shortToken = shortToken;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("shortToken")
  public String getShortToken() {
    return shortToken;
  }

  @JsonProperty("shortToken")
  public void setShortToken(String shortToken) {
    this.shortToken = shortToken;
  }

  /**
   **/
  public ApiApiKeyBase longToken(String longToken) {
    this.longToken = longToken;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("longToken")
  public String getLongToken() {
    return longToken;
  }

  @JsonProperty("longToken")
  public void setLongToken(String longToken) {
    this.longToken = longToken;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiApiKeyBase apiKeyBase = (ApiApiKeyBase) o;
    return Objects.equals(this.name, apiKeyBase.name) &&
        Objects.equals(this.description, apiKeyBase.description) &&
        Objects.equals(this.expiresAt, apiKeyBase.expiresAt) &&
        Objects.equals(this.shortToken, apiKeyBase.shortToken) &&
        Objects.equals(this.longToken, apiKeyBase.longToken);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, expiresAt, shortToken, longToken);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiApiKeyBase {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    expiresAt: ").append(toIndentedString(expiresAt)).append("\n");
    sb.append("    shortToken: ").append(toIndentedString(shortToken)).append("\n");
    sb.append("    longToken: ").append(toIndentedString(longToken)).append("\n");
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

