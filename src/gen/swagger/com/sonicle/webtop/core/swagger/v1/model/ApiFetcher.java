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
 * mailbridge fetcher
 **/
@ApiModel(description = "mailbridge fetcher")
@JsonTypeName("Fetcher")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-04-16T12:49:23.936+02:00[Europe/Berlin]")
public class ApiFetcher   {
  private @Valid String webtopProfileId;
  private @Valid Boolean deleteOnForward;
  private @Valid String forwardAddress;
  private @Valid String host;
  private @Valid Integer port;
  private @Valid String protocol;
  private @Valid String username;
  private @Valid String password;
  private @Valid String authState;

  /**
   **/
  public ApiFetcher webtopProfileId(String webtopProfileId) {
    this.webtopProfileId = webtopProfileId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("webtopProfileId")
  public String getWebtopProfileId() {
    return webtopProfileId;
  }

  @JsonProperty("webtopProfileId")
  public void setWebtopProfileId(String webtopProfileId) {
    this.webtopProfileId = webtopProfileId;
  }

  /**
   **/
  public ApiFetcher deleteOnForward(Boolean deleteOnForward) {
    this.deleteOnForward = deleteOnForward;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("deleteOnForward")
  public Boolean getDeleteOnForward() {
    return deleteOnForward;
  }

  @JsonProperty("deleteOnForward")
  public void setDeleteOnForward(Boolean deleteOnForward) {
    this.deleteOnForward = deleteOnForward;
  }

  /**
   **/
  public ApiFetcher forwardAddress(String forwardAddress) {
    this.forwardAddress = forwardAddress;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("forwardAddress")
  public String getForwardAddress() {
    return forwardAddress;
  }

  @JsonProperty("forwardAddress")
  public void setForwardAddress(String forwardAddress) {
    this.forwardAddress = forwardAddress;
  }

  /**
   **/
  public ApiFetcher host(String host) {
    this.host = host;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("host")
  public String getHost() {
    return host;
  }

  @JsonProperty("host")
  public void setHost(String host) {
    this.host = host;
  }

  /**
   **/
  public ApiFetcher port(Integer port) {
    this.port = port;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("port")
  public Integer getPort() {
    return port;
  }

  @JsonProperty("port")
  public void setPort(Integer port) {
    this.port = port;
  }

  /**
   **/
  public ApiFetcher protocol(String protocol) {
    this.protocol = protocol;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("protocol")
  public String getProtocol() {
    return protocol;
  }

  @JsonProperty("protocol")
  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  /**
   **/
  public ApiFetcher username(String username) {
    this.username = username;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("username")
  public String getUsername() {
    return username;
  }

  @JsonProperty("username")
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   **/
  public ApiFetcher password(String password) {
    this.password = password;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("password")
  public String getPassword() {
    return password;
  }

  @JsonProperty("password")
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   **/
  public ApiFetcher authState(String authState) {
    this.authState = authState;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("authState")
  public String getAuthState() {
    return authState;
  }

  @JsonProperty("authState")
  public void setAuthState(String authState) {
    this.authState = authState;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiFetcher fetcher = (ApiFetcher) o;
    return Objects.equals(this.webtopProfileId, fetcher.webtopProfileId) &&
        Objects.equals(this.deleteOnForward, fetcher.deleteOnForward) &&
        Objects.equals(this.forwardAddress, fetcher.forwardAddress) &&
        Objects.equals(this.host, fetcher.host) &&
        Objects.equals(this.port, fetcher.port) &&
        Objects.equals(this.protocol, fetcher.protocol) &&
        Objects.equals(this.username, fetcher.username) &&
        Objects.equals(this.password, fetcher.password) &&
        Objects.equals(this.authState, fetcher.authState);
  }

  @Override
  public int hashCode() {
    return Objects.hash(webtopProfileId, deleteOnForward, forwardAddress, host, port, protocol, username, password, authState);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiFetcher {\n");
    
    sb.append("    webtopProfileId: ").append(toIndentedString(webtopProfileId)).append("\n");
    sb.append("    deleteOnForward: ").append(toIndentedString(deleteOnForward)).append("\n");
    sb.append("    forwardAddress: ").append(toIndentedString(forwardAddress)).append("\n");
    sb.append("    host: ").append(toIndentedString(host)).append("\n");
    sb.append("    port: ").append(toIndentedString(port)).append("\n");
    sb.append("    protocol: ").append(toIndentedString(protocol)).append("\n");
    sb.append("    username: ").append(toIndentedString(username)).append("\n");
    sb.append("    password: ").append(toIndentedString(password)).append("\n");
    sb.append("    authState: ").append(toIndentedString(authState)).append("\n");
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

