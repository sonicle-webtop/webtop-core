package com.sonicle.webtop.core.swagger.v1.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import javax.validation.constraints.*;
import javax.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Active-session entry returned by GET /auth/sessions.
 **/
@ApiModel(description = "Active-session entry returned by GET /auth/sessions.")
@JsonTypeName("AuthSessionInfo")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-04-29T10:39:20.102+02:00[Europe/Berlin]")
public class ApiAuthSessionInfo   {
  private @Valid String sessionId;
  private @Valid String deviceLabel;
  private @Valid Date createdAt;
  private @Valid Date lastUsedAt;
  private @Valid Date expiresAt;
  private @Valid String clientIpAddress;
  private @Valid String clientUserAgent;
  private @Valid Boolean current;

  /**
   * Opaque session identifier; pass to DELETE /auth/sessions/{session_id} to revoke this session.
   **/
  public ApiAuthSessionInfo sessionId(String sessionId) {
    this.sessionId = sessionId;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Opaque session identifier; pass to DELETE /auth/sessions/{session_id} to revoke this session.")
  @JsonProperty("sessionId")
  @NotNull
  public String getSessionId() {
    return sessionId;
  }

  @JsonProperty("sessionId")
  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  /**
   * Device label provided at /auth/login (or derived from User-Agent if absent).
   **/
  public ApiAuthSessionInfo deviceLabel(String deviceLabel) {
    this.deviceLabel = deviceLabel;
    return this;
  }

  
  @ApiModelProperty(value = "Device label provided at /auth/login (or derived from User-Agent if absent).")
  @JsonProperty("deviceLabel")
  public String getDeviceLabel() {
    return deviceLabel;
  }

  @JsonProperty("deviceLabel")
  public void setDeviceLabel(String deviceLabel) {
    this.deviceLabel = deviceLabel;
  }

  /**
   * When the session was issued.
   **/
  public ApiAuthSessionInfo createdAt(Date createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "When the session was issued.")
  @JsonProperty("createdAt")
  @NotNull
  public Date getCreatedAt() {
    return createdAt;
  }

  @JsonProperty("createdAt")
  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  /**
   * When the session was last used (last refresh or last access-token validation).
   **/
  public ApiAuthSessionInfo lastUsedAt(Date lastUsedAt) {
    this.lastUsedAt = lastUsedAt;
    return this;
  }

  
  @ApiModelProperty(value = "When the session was last used (last refresh or last access-token validation).")
  @JsonProperty("lastUsedAt")
  public Date getLastUsedAt() {
    return lastUsedAt;
  }

  @JsonProperty("lastUsedAt")
  public void setLastUsedAt(Date lastUsedAt) {
    this.lastUsedAt = lastUsedAt;
  }

  /**
   * When the underlying refresh token expires.
   **/
  public ApiAuthSessionInfo expiresAt(Date expiresAt) {
    this.expiresAt = expiresAt;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "When the underlying refresh token expires.")
  @JsonProperty("expiresAt")
  @NotNull
  public Date getExpiresAt() {
    return expiresAt;
  }

  @JsonProperty("expiresAt")
  public void setExpiresAt(Date expiresAt) {
    this.expiresAt = expiresAt;
  }

  /**
   * Source IP recorded at issue time.
   **/
  public ApiAuthSessionInfo clientIpAddress(String clientIpAddress) {
    this.clientIpAddress = clientIpAddress;
    return this;
  }

  
  @ApiModelProperty(value = "Source IP recorded at issue time.")
  @JsonProperty("clientIpAddress")
  public String getClientIpAddress() {
    return clientIpAddress;
  }

  @JsonProperty("clientIpAddress")
  public void setClientIpAddress(String clientIpAddress) {
    this.clientIpAddress = clientIpAddress;
  }

  /**
   * User-Agent recorded at issue time.
   **/
  public ApiAuthSessionInfo clientUserAgent(String clientUserAgent) {
    this.clientUserAgent = clientUserAgent;
    return this;
  }

  
  @ApiModelProperty(value = "User-Agent recorded at issue time.")
  @JsonProperty("clientUserAgent")
  public String getClientUserAgent() {
    return clientUserAgent;
  }

  @JsonProperty("clientUserAgent")
  public void setClientUserAgent(String clientUserAgent) {
    this.clientUserAgent = clientUserAgent;
  }

  /**
   * True if this session corresponds to the access token used for the current request.
   **/
  public ApiAuthSessionInfo current(Boolean current) {
    this.current = current;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "True if this session corresponds to the access token used for the current request.")
  @JsonProperty("current")
  @NotNull
  public Boolean getCurrent() {
    return current;
  }

  @JsonProperty("current")
  public void setCurrent(Boolean current) {
    this.current = current;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiAuthSessionInfo authSessionInfo = (ApiAuthSessionInfo) o;
    return Objects.equals(this.sessionId, authSessionInfo.sessionId) &&
        Objects.equals(this.deviceLabel, authSessionInfo.deviceLabel) &&
        Objects.equals(this.createdAt, authSessionInfo.createdAt) &&
        Objects.equals(this.lastUsedAt, authSessionInfo.lastUsedAt) &&
        Objects.equals(this.expiresAt, authSessionInfo.expiresAt) &&
        Objects.equals(this.clientIpAddress, authSessionInfo.clientIpAddress) &&
        Objects.equals(this.clientUserAgent, authSessionInfo.clientUserAgent) &&
        Objects.equals(this.current, authSessionInfo.current);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sessionId, deviceLabel, createdAt, lastUsedAt, expiresAt, clientIpAddress, clientUserAgent, current);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiAuthSessionInfo {\n");
    
    sb.append("    sessionId: ").append(toIndentedString(sessionId)).append("\n");
    sb.append("    deviceLabel: ").append(toIndentedString(deviceLabel)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    lastUsedAt: ").append(toIndentedString(lastUsedAt)).append("\n");
    sb.append("    expiresAt: ").append(toIndentedString(expiresAt)).append("\n");
    sb.append("    clientIpAddress: ").append(toIndentedString(clientIpAddress)).append("\n");
    sb.append("    clientUserAgent: ").append(toIndentedString(clientUserAgent)).append("\n");
    sb.append("    current: ").append(toIndentedString(current)).append("\n");
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

