package com.sonicle.webtop.core.swagger.v1.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sonicle.webtop.core.swagger.v1.model.ApiAuthUserInfo;
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
 * Result of /auth/login or /auth/refresh.
 **/
@ApiModel(description = "Result of /auth/login or /auth/refresh.")
@JsonTypeName("AuthTokenPair")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-04-29T10:39:20.102+02:00[Europe/Berlin]")
public class ApiAuthTokenPair   {
  private @Valid String tokenType = "Bearer";
  private @Valid String accessToken;
  private @Valid String refreshToken;
  private @Valid Date accessTokenExpiresAt;
  private @Valid Date refreshTokenExpiresAt;
  private @Valid ApiAuthUserInfo user;

  /**
   * Token scheme. Always &#39;Bearer&#39;.
   **/
  public ApiAuthTokenPair tokenType(String tokenType) {
    this.tokenType = tokenType;
    return this;
  }

  
  @ApiModelProperty(example = "Bearer", required = true, value = "Token scheme. Always 'Bearer'.")
  @JsonProperty("tokenType")
  @NotNull
  public String getTokenType() {
    return tokenType;
  }

  @JsonProperty("tokenType")
  public void setTokenType(String tokenType) {
    this.tokenType = tokenType;
  }

  /**
   * Short-lived token to send as &#39;Authorization: Bearer &lt;token&gt;&#39; on authenticated REST calls.
   **/
  public ApiAuthTokenPair accessToken(String accessToken) {
    this.accessToken = accessToken;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Short-lived token to send as 'Authorization: Bearer <token>' on authenticated REST calls.")
  @JsonProperty("accessToken")
  @NotNull
  public String getAccessToken() {
    return accessToken;
  }

  @JsonProperty("accessToken")
  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  /**
   * Long-lived token used only against /auth/refresh to mint a new pair. Rotated on every use.
   **/
  public ApiAuthTokenPair refreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Long-lived token used only against /auth/refresh to mint a new pair. Rotated on every use.")
  @JsonProperty("refreshToken")
  @NotNull
  public String getRefreshToken() {
    return refreshToken;
  }

  @JsonProperty("refreshToken")
  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  /**
   * Absolute expiry of the access token (ISO-8601, UTC).
   **/
  public ApiAuthTokenPair accessTokenExpiresAt(Date accessTokenExpiresAt) {
    this.accessTokenExpiresAt = accessTokenExpiresAt;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Absolute expiry of the access token (ISO-8601, UTC).")
  @JsonProperty("accessTokenExpiresAt")
  @NotNull
  public Date getAccessTokenExpiresAt() {
    return accessTokenExpiresAt;
  }

  @JsonProperty("accessTokenExpiresAt")
  public void setAccessTokenExpiresAt(Date accessTokenExpiresAt) {
    this.accessTokenExpiresAt = accessTokenExpiresAt;
  }

  /**
   * Absolute expiry of the refresh token (ISO-8601, UTC).
   **/
  public ApiAuthTokenPair refreshTokenExpiresAt(Date refreshTokenExpiresAt) {
    this.refreshTokenExpiresAt = refreshTokenExpiresAt;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Absolute expiry of the refresh token (ISO-8601, UTC).")
  @JsonProperty("refreshTokenExpiresAt")
  @NotNull
  public Date getRefreshTokenExpiresAt() {
    return refreshTokenExpiresAt;
  }

  @JsonProperty("refreshTokenExpiresAt")
  public void setRefreshTokenExpiresAt(Date refreshTokenExpiresAt) {
    this.refreshTokenExpiresAt = refreshTokenExpiresAt;
  }

  /**
   **/
  public ApiAuthTokenPair user(ApiAuthUserInfo user) {
    this.user = user;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("user")
  public ApiAuthUserInfo getUser() {
    return user;
  }

  @JsonProperty("user")
  public void setUser(ApiAuthUserInfo user) {
    this.user = user;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiAuthTokenPair authTokenPair = (ApiAuthTokenPair) o;
    return Objects.equals(this.tokenType, authTokenPair.tokenType) &&
        Objects.equals(this.accessToken, authTokenPair.accessToken) &&
        Objects.equals(this.refreshToken, authTokenPair.refreshToken) &&
        Objects.equals(this.accessTokenExpiresAt, authTokenPair.accessTokenExpiresAt) &&
        Objects.equals(this.refreshTokenExpiresAt, authTokenPair.refreshTokenExpiresAt) &&
        Objects.equals(this.user, authTokenPair.user);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tokenType, accessToken, refreshToken, accessTokenExpiresAt, refreshTokenExpiresAt, user);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiAuthTokenPair {\n");
    
    sb.append("    tokenType: ").append(toIndentedString(tokenType)).append("\n");
    sb.append("    accessToken: ").append(toIndentedString(accessToken)).append("\n");
    sb.append("    refreshToken: ").append(toIndentedString(refreshToken)).append("\n");
    sb.append("    accessTokenExpiresAt: ").append(toIndentedString(accessTokenExpiresAt)).append("\n");
    sb.append("    refreshTokenExpiresAt: ").append(toIndentedString(refreshTokenExpiresAt)).append("\n");
    sb.append("    user: ").append(toIndentedString(user)).append("\n");
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

