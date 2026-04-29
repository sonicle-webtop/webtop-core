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
 * Credentials for issuing a new token pair via /auth/login.
 **/
@ApiModel(description = "Credentials for issuing a new token pair via /auth/login.")
@JsonTypeName("AuthLoginRequest")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-04-29T10:39:20.102+02:00[Europe/Berlin]")
public class ApiAuthLoginRequest   {
  private @Valid String username;
  private @Valid String password;
  private @Valid String domain;
  private @Valid String deviceLabel;

  /**
   * User identifier; may be a bare local username (e.g. john.doe) or a full internet name (john.doe@example.com)
   **/
  public ApiAuthLoginRequest username(String username) {
    this.username = username;
    return this;
  }

  
  @ApiModelProperty(example = "john.doe@example.com", required = true, value = "User identifier; may be a bare local username (e.g. john.doe) or a full internet name (john.doe@example.com)")
  @JsonProperty("username")
  @NotNull
  public String getUsername() {
    return username;
  }

  @JsonProperty("username")
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * User password
   **/
  public ApiAuthLoginRequest password(String password) {
    this.password = password;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "User password")
  @JsonProperty("password")
  @NotNull
  public String getPassword() {
    return password;
  }

  @JsonProperty("password")
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * WebTop domain ID; optional when only one domain is enabled or when the username carries an internet-domain suffix
   **/
  public ApiAuthLoginRequest domain(String domain) {
    this.domain = domain;
    return this;
  }

  
  @ApiModelProperty(value = "WebTop domain ID; optional when only one domain is enabled or when the username carries an internet-domain suffix")
  @JsonProperty("domain")
  public String getDomain() {
    return domain;
  }

  @JsonProperty("domain")
  public void setDomain(String domain) {
    this.domain = domain;
  }

  /**
   * Human-readable label for the device/client requesting the token (recorded in the session list)
   **/
  public ApiAuthLoginRequest deviceLabel(String deviceLabel) {
    this.deviceLabel = deviceLabel;
    return this;
  }

  
  @ApiModelProperty(example = "Pixel 7 - Chrome", value = "Human-readable label for the device/client requesting the token (recorded in the session list)")
  @JsonProperty("deviceLabel")
  public String getDeviceLabel() {
    return deviceLabel;
  }

  @JsonProperty("deviceLabel")
  public void setDeviceLabel(String deviceLabel) {
    this.deviceLabel = deviceLabel;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiAuthLoginRequest authLoginRequest = (ApiAuthLoginRequest) o;
    return Objects.equals(this.username, authLoginRequest.username) &&
        Objects.equals(this.password, authLoginRequest.password) &&
        Objects.equals(this.domain, authLoginRequest.domain) &&
        Objects.equals(this.deviceLabel, authLoginRequest.deviceLabel);
  }

  @Override
  public int hashCode() {
    return Objects.hash(username, password, domain, deviceLabel);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiAuthLoginRequest {\n");
    
    sb.append("    username: ").append(toIndentedString(username)).append("\n");
    sb.append("    password: ").append(toIndentedString(password)).append("\n");
    sb.append("    domain: ").append(toIndentedString(domain)).append("\n");
    sb.append("    deviceLabel: ").append(toIndentedString(deviceLabel)).append("\n");
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

