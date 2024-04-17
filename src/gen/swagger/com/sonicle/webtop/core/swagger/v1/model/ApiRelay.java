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
 * mailbridge relay
 **/
@ApiModel(description = "mailbridge relay")
@JsonTypeName("Relay")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-04-16T12:49:23.936+02:00[Europe/Berlin]")
public class ApiRelay   {
  private @Valid String webtopProfileId;
  private @Valid Boolean debug;
  private @Valid String host;
  private @Valid Integer port;
  private @Valid String protocol;
  private @Valid String username;
  private @Valid String password;
  private @Valid String matcher;
  private @Valid Boolean ssl;
  private @Valid Boolean starttls;
  private @Valid String authState;

  /**
   **/
  public ApiRelay webtopProfileId(String webtopProfileId) {
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
  public ApiRelay debug(Boolean debug) {
    this.debug = debug;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("debug")
  public Boolean getDebug() {
    return debug;
  }

  @JsonProperty("debug")
  public void setDebug(Boolean debug) {
    this.debug = debug;
  }

  /**
   **/
  public ApiRelay host(String host) {
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
  public ApiRelay port(Integer port) {
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
  public ApiRelay protocol(String protocol) {
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
  public ApiRelay username(String username) {
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
  public ApiRelay password(String password) {
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
  public ApiRelay matcher(String matcher) {
    this.matcher = matcher;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("matcher")
  public String getMatcher() {
    return matcher;
  }

  @JsonProperty("matcher")
  public void setMatcher(String matcher) {
    this.matcher = matcher;
  }

  /**
   **/
  public ApiRelay ssl(Boolean ssl) {
    this.ssl = ssl;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("ssl")
  public Boolean getSsl() {
    return ssl;
  }

  @JsonProperty("ssl")
  public void setSsl(Boolean ssl) {
    this.ssl = ssl;
  }

  /**
   **/
  public ApiRelay starttls(Boolean starttls) {
    this.starttls = starttls;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("starttls")
  public Boolean getStarttls() {
    return starttls;
  }

  @JsonProperty("starttls")
  public void setStarttls(Boolean starttls) {
    this.starttls = starttls;
  }

  /**
   **/
  public ApiRelay authState(String authState) {
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
    ApiRelay relay = (ApiRelay) o;
    return Objects.equals(this.webtopProfileId, relay.webtopProfileId) &&
        Objects.equals(this.debug, relay.debug) &&
        Objects.equals(this.host, relay.host) &&
        Objects.equals(this.port, relay.port) &&
        Objects.equals(this.protocol, relay.protocol) &&
        Objects.equals(this.username, relay.username) &&
        Objects.equals(this.password, relay.password) &&
        Objects.equals(this.matcher, relay.matcher) &&
        Objects.equals(this.ssl, relay.ssl) &&
        Objects.equals(this.starttls, relay.starttls) &&
        Objects.equals(this.authState, relay.authState);
  }

  @Override
  public int hashCode() {
    return Objects.hash(webtopProfileId, debug, host, port, protocol, username, password, matcher, ssl, starttls, authState);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiRelay {\n");
    
    sb.append("    webtopProfileId: ").append(toIndentedString(webtopProfileId)).append("\n");
    sb.append("    debug: ").append(toIndentedString(debug)).append("\n");
    sb.append("    host: ").append(toIndentedString(host)).append("\n");
    sb.append("    port: ").append(toIndentedString(port)).append("\n");
    sb.append("    protocol: ").append(toIndentedString(protocol)).append("\n");
    sb.append("    username: ").append(toIndentedString(username)).append("\n");
    sb.append("    password: ").append(toIndentedString(password)).append("\n");
    sb.append("    matcher: ").append(toIndentedString(matcher)).append("\n");
    sb.append("    ssl: ").append(toIndentedString(ssl)).append("\n");
    sb.append("    starttls: ").append(toIndentedString(starttls)).append("\n");
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

