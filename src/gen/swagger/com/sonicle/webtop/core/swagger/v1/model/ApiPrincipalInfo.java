package com.sonicle.webtop.core.swagger.v1.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;



@JsonTypeName("PrincipalInfo")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-04-16T12:49:23.936+02:00[Europe/Berlin]")
public class ApiPrincipalInfo   {
  private @Valid String profileId;
  private @Valid String profileUsername;
  private @Valid String displayName;
  private @Valid String emailAddress;
  private @Valid String timezoneId;
  private @Valid String languageTag;
  private @Valid List<Boolean> evalPermRefs = null;

  /**
   * Internal profile ID (user@wtdomain)
   **/
  public ApiPrincipalInfo profileId(String profileId) {
    this.profileId = profileId;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Internal profile ID (user@wtdomain)")
  @JsonProperty("profileId")
  @NotNull
  public String getProfileId() {
    return profileId;
  }

  @JsonProperty("profileId")
  public void setProfileId(String profileId) {
    this.profileId = profileId;
  }

  /**
   * Full profile username (user@domain.tld)
   **/
  public ApiPrincipalInfo profileUsername(String profileUsername) {
    this.profileUsername = profileUsername;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Full profile username (user@domain.tld)")
  @JsonProperty("profileUsername")
  @NotNull
  public String getProfileUsername() {
    return profileUsername;
  }

  @JsonProperty("profileUsername")
  public void setProfileUsername(String profileUsername) {
    this.profileUsername = profileUsername;
  }

  /**
   * Associated display name
   **/
  public ApiPrincipalInfo displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Associated display name")
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
   * Associated email address
   **/
  public ApiPrincipalInfo emailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
    return this;
  }

  
  @ApiModelProperty(value = "Associated email address")
  @JsonProperty("emailAddress")
  public String getEmailAddress() {
    return emailAddress;
  }

  @JsonProperty("emailAddress")
  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }

  /**
   * Timezone ID
   **/
  public ApiPrincipalInfo timezoneId(String timezoneId) {
    this.timezoneId = timezoneId;
    return this;
  }

  
  @ApiModelProperty(example = "Europe/Rome", required = true, value = "Timezone ID")
  @JsonProperty("timezoneId")
  @NotNull
  public String getTimezoneId() {
    return timezoneId;
  }

  @JsonProperty("timezoneId")
  public void setTimezoneId(String timezoneId) {
    this.timezoneId = timezoneId;
  }

  /**
   * Language identifier (language_country)
   **/
  public ApiPrincipalInfo languageTag(String languageTag) {
    this.languageTag = languageTag;
    return this;
  }

  
  @ApiModelProperty(example = "en_EN", required = true, value = "Language identifier (language_country)")
  @JsonProperty("languageTag")
  @NotNull
  public String getLanguageTag() {
    return languageTag;
  }

  @JsonProperty("languageTag")
  public void setLanguageTag(String languageTag) {
    this.languageTag = languageTag;
  }

  /**
   **/
  public ApiPrincipalInfo evalPermRefs(List<Boolean> evalPermRefs) {
    this.evalPermRefs = evalPermRefs;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("evalPermRefs")
  public List<Boolean> getEvalPermRefs() {
    return evalPermRefs;
  }

  @JsonProperty("evalPermRefs")
  public void setEvalPermRefs(List<Boolean> evalPermRefs) {
    this.evalPermRefs = evalPermRefs;
  }

  public ApiPrincipalInfo addEvalPermRefsItem(Boolean evalPermRefsItem) {
    if (this.evalPermRefs == null) {
      this.evalPermRefs = new ArrayList<>();
    }

    this.evalPermRefs.add(evalPermRefsItem);
    return this;
  }

  public ApiPrincipalInfo removeEvalPermRefsItem(Boolean evalPermRefsItem) {
    if (evalPermRefsItem != null && this.evalPermRefs != null) {
      this.evalPermRefs.remove(evalPermRefsItem);
    }

    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiPrincipalInfo principalInfo = (ApiPrincipalInfo) o;
    return Objects.equals(this.profileId, principalInfo.profileId) &&
        Objects.equals(this.profileUsername, principalInfo.profileUsername) &&
        Objects.equals(this.displayName, principalInfo.displayName) &&
        Objects.equals(this.emailAddress, principalInfo.emailAddress) &&
        Objects.equals(this.timezoneId, principalInfo.timezoneId) &&
        Objects.equals(this.languageTag, principalInfo.languageTag) &&
        Objects.equals(this.evalPermRefs, principalInfo.evalPermRefs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(profileId, profileUsername, displayName, emailAddress, timezoneId, languageTag, evalPermRefs);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiPrincipalInfo {\n");
    
    sb.append("    profileId: ").append(toIndentedString(profileId)).append("\n");
    sb.append("    profileUsername: ").append(toIndentedString(profileUsername)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    emailAddress: ").append(toIndentedString(emailAddress)).append("\n");
    sb.append("    timezoneId: ").append(toIndentedString(timezoneId)).append("\n");
    sb.append("    languageTag: ").append(toIndentedString(languageTag)).append("\n");
    sb.append("    evalPermRefs: ").append(toIndentedString(evalPermRefs)).append("\n");
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

