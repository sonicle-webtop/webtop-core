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
 * The domain data for listing purposes.
 **/
@ApiModel(description = "The domain data for listing purposes.")
@JsonTypeName("DomainEntry")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-04-16T12:49:23.936+02:00[Europe/Berlin]")
public class ApiDomainEntry   {
  private @Valid String domainId;
  private @Valid Boolean enabled;
  private @Valid String displayName;
  private @Valid String authDomainName;
  private @Valid String domainName;
  private @Valid String publicURL;
  private @Valid Boolean userAutoCreation;
  private @Valid String dirUri;

  /**
   * Domain ID
   **/
  public ApiDomainEntry domainId(String domainId) {
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

  /**
   * Represent the availability status
   **/
  public ApiDomainEntry enabled(Boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Represent the availability status")
  @JsonProperty("enabled")
  @NotNull
  public Boolean getEnabled() {
    return enabled;
  }

  @JsonProperty("enabled")
  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Descriptive name for display purposes
   **/
  public ApiDomainEntry displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Descriptive name for display purposes")
  @JsonProperty("displayName")
  @NotNull
  public String getDisplayName() {
    return displayName;
  }

  @JsonProperty("displayName")
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * Authentication DomainName
   **/
  public ApiDomainEntry authDomainName(String authDomainName) {
    this.authDomainName = authDomainName;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Authentication DomainName")
  @JsonProperty("authDomainName")
  @NotNull
  public String getAuthDomainName() {
    return authDomainName;
  }

  @JsonProperty("authDomainName")
  public void setAuthDomainName(String authDomainName) {
    this.authDomainName = authDomainName;
  }

  /**
   * Primary DomainName (defaults to auth authDomainName)
   **/
  public ApiDomainEntry domainName(String domainName) {
    this.domainName = domainName;
    return this;
  }

  
  @ApiModelProperty(value = "Primary DomainName (defaults to auth authDomainName)")
  @JsonProperty("domainName")
  public String getDomainName() {
    return domainName;
  }

  @JsonProperty("domainName")
  public void setDomainName(String domainName) {
    this.domainName = domainName;
  }

  /**
   * Public URL to access the service, it get data from &#39;public.url&#39; settings for now.
   **/
  public ApiDomainEntry publicURL(String publicURL) {
    this.publicURL = publicURL;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Public URL to access the service, it get data from 'public.url' settings for now.")
  @JsonProperty("publicURL")
  @NotNull
  public String getPublicURL() {
    return publicURL;
  }

  @JsonProperty("publicURL")
  public void setPublicURL(String publicURL) {
    this.publicURL = publicURL;
  }

  /**
   * Specifies if user auto-creation at logon is active
   **/
  public ApiDomainEntry userAutoCreation(Boolean userAutoCreation) {
    this.userAutoCreation = userAutoCreation;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Specifies if user auto-creation at logon is active")
  @JsonProperty("userAutoCreation")
  @NotNull
  public Boolean getUserAutoCreation() {
    return userAutoCreation;
  }

  @JsonProperty("userAutoCreation")
  public void setUserAutoCreation(Boolean userAutoCreation) {
    this.userAutoCreation = userAutoCreation;
  }

  /**
   * The URL of underlyning authentication directory
   **/
  public ApiDomainEntry dirUri(String dirUri) {
    this.dirUri = dirUri;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The URL of underlyning authentication directory")
  @JsonProperty("dirUri")
  @NotNull
  public String getDirUri() {
    return dirUri;
  }

  @JsonProperty("dirUri")
  public void setDirUri(String dirUri) {
    this.dirUri = dirUri;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiDomainEntry domainEntry = (ApiDomainEntry) o;
    return Objects.equals(this.domainId, domainEntry.domainId) &&
        Objects.equals(this.enabled, domainEntry.enabled) &&
        Objects.equals(this.displayName, domainEntry.displayName) &&
        Objects.equals(this.authDomainName, domainEntry.authDomainName) &&
        Objects.equals(this.domainName, domainEntry.domainName) &&
        Objects.equals(this.publicURL, domainEntry.publicURL) &&
        Objects.equals(this.userAutoCreation, domainEntry.userAutoCreation) &&
        Objects.equals(this.dirUri, domainEntry.dirUri);
  }

  @Override
  public int hashCode() {
    return Objects.hash(domainId, enabled, displayName, authDomainName, domainName, publicURL, userAutoCreation, dirUri);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiDomainEntry {\n");
    
    sb.append("    domainId: ").append(toIndentedString(domainId)).append("\n");
    sb.append("    enabled: ").append(toIndentedString(enabled)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    authDomainName: ").append(toIndentedString(authDomainName)).append("\n");
    sb.append("    domainName: ").append(toIndentedString(domainName)).append("\n");
    sb.append("    publicURL: ").append(toIndentedString(publicURL)).append("\n");
    sb.append("    userAutoCreation: ").append(toIndentedString(userAutoCreation)).append("\n");
    sb.append("    dirUri: ").append(toIndentedString(dirUri)).append("\n");
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

