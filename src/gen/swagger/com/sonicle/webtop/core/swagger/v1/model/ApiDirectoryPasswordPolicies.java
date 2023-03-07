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
 * The password policies for some domain authentication directories ().
 **/
@ApiModel(description = "The password policies for some domain authentication directories ().")
@JsonTypeName("DirectoryPasswordPolicies")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2023-01-19T09:40:24.427+01:00[Europe/Berlin]")
public class ApiDirectoryPasswordPolicies   {
  private @Valid Integer minLength;
  private @Valid Boolean complexity;
  private @Valid Boolean avoidConsecutiveChars;
  private @Valid Boolean avoidOldSimilarity;
  private @Valid Boolean avoidUsernameSimilarity;
  private @Valid Integer expiration;
  private @Valid Boolean verifyAtLogin;

  /**
   **/
  public ApiDirectoryPasswordPolicies minLength(Integer minLength) {
    this.minLength = minLength;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("minLength")
  public Integer getMinLength() {
    return minLength;
  }

  @JsonProperty("minLength")
  public void setMinLength(Integer minLength) {
    this.minLength = minLength;
  }

  /**
   **/
  public ApiDirectoryPasswordPolicies complexity(Boolean complexity) {
    this.complexity = complexity;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("complexity")
  @NotNull
  public Boolean getComplexity() {
    return complexity;
  }

  @JsonProperty("complexity")
  public void setComplexity(Boolean complexity) {
    this.complexity = complexity;
  }

  /**
   **/
  public ApiDirectoryPasswordPolicies avoidConsecutiveChars(Boolean avoidConsecutiveChars) {
    this.avoidConsecutiveChars = avoidConsecutiveChars;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("avoidConsecutiveChars")
  @NotNull
  public Boolean getAvoidConsecutiveChars() {
    return avoidConsecutiveChars;
  }

  @JsonProperty("avoidConsecutiveChars")
  public void setAvoidConsecutiveChars(Boolean avoidConsecutiveChars) {
    this.avoidConsecutiveChars = avoidConsecutiveChars;
  }

  /**
   **/
  public ApiDirectoryPasswordPolicies avoidOldSimilarity(Boolean avoidOldSimilarity) {
    this.avoidOldSimilarity = avoidOldSimilarity;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("avoidOldSimilarity")
  @NotNull
  public Boolean getAvoidOldSimilarity() {
    return avoidOldSimilarity;
  }

  @JsonProperty("avoidOldSimilarity")
  public void setAvoidOldSimilarity(Boolean avoidOldSimilarity) {
    this.avoidOldSimilarity = avoidOldSimilarity;
  }

  /**
   **/
  public ApiDirectoryPasswordPolicies avoidUsernameSimilarity(Boolean avoidUsernameSimilarity) {
    this.avoidUsernameSimilarity = avoidUsernameSimilarity;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("avoidUsernameSimilarity")
  @NotNull
  public Boolean getAvoidUsernameSimilarity() {
    return avoidUsernameSimilarity;
  }

  @JsonProperty("avoidUsernameSimilarity")
  public void setAvoidUsernameSimilarity(Boolean avoidUsernameSimilarity) {
    this.avoidUsernameSimilarity = avoidUsernameSimilarity;
  }

  /**
   **/
  public ApiDirectoryPasswordPolicies expiration(Integer expiration) {
    this.expiration = expiration;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("expiration")
  public Integer getExpiration() {
    return expiration;
  }

  @JsonProperty("expiration")
  public void setExpiration(Integer expiration) {
    this.expiration = expiration;
  }

  /**
   **/
  public ApiDirectoryPasswordPolicies verifyAtLogin(Boolean verifyAtLogin) {
    this.verifyAtLogin = verifyAtLogin;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("verifyAtLogin")
  @NotNull
  public Boolean getVerifyAtLogin() {
    return verifyAtLogin;
  }

  @JsonProperty("verifyAtLogin")
  public void setVerifyAtLogin(Boolean verifyAtLogin) {
    this.verifyAtLogin = verifyAtLogin;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiDirectoryPasswordPolicies directoryPasswordPolicies = (ApiDirectoryPasswordPolicies) o;
    return Objects.equals(this.minLength, directoryPasswordPolicies.minLength) &&
        Objects.equals(this.complexity, directoryPasswordPolicies.complexity) &&
        Objects.equals(this.avoidConsecutiveChars, directoryPasswordPolicies.avoidConsecutiveChars) &&
        Objects.equals(this.avoidOldSimilarity, directoryPasswordPolicies.avoidOldSimilarity) &&
        Objects.equals(this.avoidUsernameSimilarity, directoryPasswordPolicies.avoidUsernameSimilarity) &&
        Objects.equals(this.expiration, directoryPasswordPolicies.expiration) &&
        Objects.equals(this.verifyAtLogin, directoryPasswordPolicies.verifyAtLogin);
  }

  @Override
  public int hashCode() {
    return Objects.hash(minLength, complexity, avoidConsecutiveChars, avoidOldSimilarity, avoidUsernameSimilarity, expiration, verifyAtLogin);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiDirectoryPasswordPolicies {\n");
    
    sb.append("    minLength: ").append(toIndentedString(minLength)).append("\n");
    sb.append("    complexity: ").append(toIndentedString(complexity)).append("\n");
    sb.append("    avoidConsecutiveChars: ").append(toIndentedString(avoidConsecutiveChars)).append("\n");
    sb.append("    avoidOldSimilarity: ").append(toIndentedString(avoidOldSimilarity)).append("\n");
    sb.append("    avoidUsernameSimilarity: ").append(toIndentedString(avoidUsernameSimilarity)).append("\n");
    sb.append("    expiration: ").append(toIndentedString(expiration)).append("\n");
    sb.append("    verifyAtLogin: ").append(toIndentedString(verifyAtLogin)).append("\n");
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

