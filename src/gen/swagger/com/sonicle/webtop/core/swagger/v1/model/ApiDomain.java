package com.sonicle.webtop.core.swagger.v1.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sonicle.webtop.core.swagger.v1.model.ApiDirectoryPasswordPolicies;
import com.sonicle.webtop.core.swagger.v1.model.ApiDomainBaseDirRawParameters;
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
 * The domain data.
 **/
@ApiModel(description = "The domain data.")
@JsonTypeName("Domain")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-04-16T12:49:23.936+02:00[Europe/Berlin]")
public class ApiDomain   {
  private @Valid String domainId;
  private @Valid Boolean enabled;
  private @Valid String displayName;
  private @Valid String authDomainName;
  private @Valid String domainName;
  private @Valid String publicURL;
  private @Valid Boolean userAutoCreation;
  private @Valid String dirUri;
  private @Valid String dirAdmin;
  private @Valid String dirPassword;
  public enum DirConnSecurityEnum {

    OFF(String.valueOf("OFF")), SSL(String.valueOf("SSL")), STARTTLS(String.valueOf("STARTTLS"));


    private String value;

    DirConnSecurityEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Convert a String into String, as specified in the
     * <a href="https://download.oracle.com/otndocs/jcp/jaxrs-2_0-fr-eval-spec/index.html">See JAX RS 2.0 Specification, section 3.2, p. 12</a>
     */
	public static DirConnSecurityEnum fromString(String s) {
        for (DirConnSecurityEnum b : DirConnSecurityEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (java.util.Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
	}
	
    @JsonCreator
    public static DirConnSecurityEnum fromValue(String value) {
        for (DirConnSecurityEnum b : DirConnSecurityEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private @Valid DirConnSecurityEnum dirConnSecurity;
  private @Valid Boolean dirCaseSensitive;
  private @Valid ApiDomainBaseDirRawParameters dirRawParameters;
  private @Valid ApiDirectoryPasswordPolicies passwordPolicies;

  /**
   * Domain ID
   **/
  public ApiDomain domainId(String domainId) {
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
  public ApiDomain enabled(Boolean enabled) {
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
  public ApiDomain displayName(String displayName) {
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
  public ApiDomain authDomainName(String authDomainName) {
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
   * [Not used for now] Primary DomainName (defaults to auth DomainName).
   **/
  public ApiDomain domainName(String domainName) {
    this.domainName = domainName;
    return this;
  }

  
  @ApiModelProperty(value = "[Not used for now] Primary DomainName (defaults to auth DomainName).")
  @JsonProperty("domainName")
  public String getDomainName() {
    return domainName;
  }

  @JsonProperty("domainName")
  public void setDomainName(String domainName) {
    this.domainName = domainName;
  }

  /**
   * Public URL to access the service, it uses &#39;public.url&#39; settings for now.
   **/
  public ApiDomain publicURL(String publicURL) {
    this.publicURL = publicURL;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Public URL to access the service, it uses 'public.url' settings for now.")
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
   * Specifies if user auto-creation at logon is active.
   **/
  public ApiDomain userAutoCreation(Boolean userAutoCreation) {
    this.userAutoCreation = userAutoCreation;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Specifies if user auto-creation at logon is active.")
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
   * The URL of underlyning authentication directory.
   **/
  public ApiDomain dirUri(String dirUri) {
    this.dirUri = dirUri;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The URL of underlyning authentication directory.")
  @JsonProperty("dirUri")
  @NotNull
  public String getDirUri() {
    return dirUri;
  }

  @JsonProperty("dirUri")
  public void setDirUri(String dirUri) {
    this.dirUri = dirUri;
  }

  /**
   * Username to access Directory as Admin.
   **/
  public ApiDomain dirAdmin(String dirAdmin) {
    this.dirAdmin = dirAdmin;
    return this;
  }

  
  @ApiModelProperty(value = "Username to access Directory as Admin.")
  @JsonProperty("dirAdmin")
  public String getDirAdmin() {
    return dirAdmin;
  }

  @JsonProperty("dirAdmin")
  public void setDirAdmin(String dirAdmin) {
    this.dirAdmin = dirAdmin;
  }

  /**
   * Password of the Admin username.
   **/
  public ApiDomain dirPassword(String dirPassword) {
    this.dirPassword = dirPassword;
    return this;
  }

  
  @ApiModelProperty(value = "Password of the Admin username.")
  @JsonProperty("dirPassword")
  public String getDirPassword() {
    return dirPassword;
  }

  @JsonProperty("dirPassword")
  public void setDirPassword(String dirPassword) {
    this.dirPassword = dirPassword;
  }

  /**
   * Specifies the security level of the connection.
   **/
  public ApiDomain dirConnSecurity(DirConnSecurityEnum dirConnSecurity) {
    this.dirConnSecurity = dirConnSecurity;
    return this;
  }

  
  @ApiModelProperty(value = "Specifies the security level of the connection.")
  @JsonProperty("dirConnSecurity")
  public DirConnSecurityEnum getDirConnSecurity() {
    return dirConnSecurity;
  }

  @JsonProperty("dirConnSecurity")
  public void setDirConnSecurity(DirConnSecurityEnum dirConnSecurity) {
    this.dirConnSecurity = dirConnSecurity;
  }

  /**
   * Specifies whether Directory is case-sensitive during usernames evaluation.
   **/
  public ApiDomain dirCaseSensitive(Boolean dirCaseSensitive) {
    this.dirCaseSensitive = dirCaseSensitive;
    return this;
  }

  
  @ApiModelProperty(value = "Specifies whether Directory is case-sensitive during usernames evaluation.")
  @JsonProperty("dirCaseSensitive")
  public Boolean getDirCaseSensitive() {
    return dirCaseSensitive;
  }

  @JsonProperty("dirCaseSensitive")
  public void setDirCaseSensitive(Boolean dirCaseSensitive) {
    this.dirCaseSensitive = dirCaseSensitive;
  }

  /**
   **/
  public ApiDomain dirRawParameters(ApiDomainBaseDirRawParameters dirRawParameters) {
    this.dirRawParameters = dirRawParameters;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("dirRawParameters")
  public ApiDomainBaseDirRawParameters getDirRawParameters() {
    return dirRawParameters;
  }

  @JsonProperty("dirRawParameters")
  public void setDirRawParameters(ApiDomainBaseDirRawParameters dirRawParameters) {
    this.dirRawParameters = dirRawParameters;
  }

  /**
   **/
  public ApiDomain passwordPolicies(ApiDirectoryPasswordPolicies passwordPolicies) {
    this.passwordPolicies = passwordPolicies;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("passwordPolicies")
  public ApiDirectoryPasswordPolicies getPasswordPolicies() {
    return passwordPolicies;
  }

  @JsonProperty("passwordPolicies")
  public void setPasswordPolicies(ApiDirectoryPasswordPolicies passwordPolicies) {
    this.passwordPolicies = passwordPolicies;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiDomain domain = (ApiDomain) o;
    return Objects.equals(this.domainId, domain.domainId) &&
        Objects.equals(this.enabled, domain.enabled) &&
        Objects.equals(this.displayName, domain.displayName) &&
        Objects.equals(this.authDomainName, domain.authDomainName) &&
        Objects.equals(this.domainName, domain.domainName) &&
        Objects.equals(this.publicURL, domain.publicURL) &&
        Objects.equals(this.userAutoCreation, domain.userAutoCreation) &&
        Objects.equals(this.dirUri, domain.dirUri) &&
        Objects.equals(this.dirAdmin, domain.dirAdmin) &&
        Objects.equals(this.dirPassword, domain.dirPassword) &&
        Objects.equals(this.dirConnSecurity, domain.dirConnSecurity) &&
        Objects.equals(this.dirCaseSensitive, domain.dirCaseSensitive) &&
        Objects.equals(this.dirRawParameters, domain.dirRawParameters) &&
        Objects.equals(this.passwordPolicies, domain.passwordPolicies);
  }

  @Override
  public int hashCode() {
    return Objects.hash(domainId, enabled, displayName, authDomainName, domainName, publicURL, userAutoCreation, dirUri, dirAdmin, dirPassword, dirConnSecurity, dirCaseSensitive, dirRawParameters, passwordPolicies);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiDomain {\n");
    
    sb.append("    domainId: ").append(toIndentedString(domainId)).append("\n");
    sb.append("    enabled: ").append(toIndentedString(enabled)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    authDomainName: ").append(toIndentedString(authDomainName)).append("\n");
    sb.append("    domainName: ").append(toIndentedString(domainName)).append("\n");
    sb.append("    publicURL: ").append(toIndentedString(publicURL)).append("\n");
    sb.append("    userAutoCreation: ").append(toIndentedString(userAutoCreation)).append("\n");
    sb.append("    dirUri: ").append(toIndentedString(dirUri)).append("\n");
    sb.append("    dirAdmin: ").append(toIndentedString(dirAdmin)).append("\n");
    sb.append("    dirPassword: ").append(toIndentedString(dirPassword)).append("\n");
    sb.append("    dirConnSecurity: ").append(toIndentedString(dirConnSecurity)).append("\n");
    sb.append("    dirCaseSensitive: ").append(toIndentedString(dirCaseSensitive)).append("\n");
    sb.append("    dirRawParameters: ").append(toIndentedString(dirRawParameters)).append("\n");
    sb.append("    passwordPolicies: ").append(toIndentedString(passwordPolicies)).append("\n");
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

