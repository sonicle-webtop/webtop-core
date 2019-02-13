package com.sonicle.webtop.core.swagger.v1.model;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;


import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;


public class PrincipalInfo   {
  
  private @Valid String profileId = null;
  private @Valid String profileUsername = null;
  private @Valid String displayName = null;
  private @Valid String emailAddress = null;
  private @Valid String timezoneId = null;
  private @Valid String languageTag = null;
  private @Valid List<Boolean> evalPermRefs = new ArrayList<Boolean>();

  /**
   * Internal profile ID (user@wtdomain)
   **/
  public PrincipalInfo profileId(String profileId) {
    this.profileId = profileId;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Internal profile ID (user@wtdomain)")
  @JsonProperty("profileId")
  @NotNull
  public String getProfileId() {
    return profileId;
  }
  public void setProfileId(String profileId) {
    this.profileId = profileId;
  }

  /**
   * Full profile username (user@domain.tld)
   **/
  public PrincipalInfo profileUsername(String profileUsername) {
    this.profileUsername = profileUsername;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Full profile username (user@domain.tld)")
  @JsonProperty("profileUsername")
  @NotNull
  public String getProfileUsername() {
    return profileUsername;
  }
  public void setProfileUsername(String profileUsername) {
    this.profileUsername = profileUsername;
  }

  /**
   * Associated display name
   **/
  public PrincipalInfo displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Associated display name")
  @JsonProperty("displayName")
  @NotNull
  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * Associated email address
   **/
  public PrincipalInfo emailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
    return this;
  }

  
  @ApiModelProperty(value = "Associated email address")
  @JsonProperty("emailAddress")
  public String getEmailAddress() {
    return emailAddress;
  }
  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }

  /**
   * Timezone ID
   **/
  public PrincipalInfo timezoneId(String timezoneId) {
    this.timezoneId = timezoneId;
    return this;
  }

  
  @ApiModelProperty(example = "Europe/Rome", required = true, value = "Timezone ID")
  @JsonProperty("timezoneId")
  @NotNull
  public String getTimezoneId() {
    return timezoneId;
  }
  public void setTimezoneId(String timezoneId) {
    this.timezoneId = timezoneId;
  }

  /**
   * Language identifier (language_country)
   **/
  public PrincipalInfo languageTag(String languageTag) {
    this.languageTag = languageTag;
    return this;
  }

  
  @ApiModelProperty(example = "en_EN", required = true, value = "Language identifier (language_country)")
  @JsonProperty("languageTag")
  @NotNull
  public String getLanguageTag() {
    return languageTag;
  }
  public void setLanguageTag(String languageTag) {
    this.languageTag = languageTag;
  }

  /**
   **/
  public PrincipalInfo evalPermRefs(List<Boolean> evalPermRefs) {
    this.evalPermRefs = evalPermRefs;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("evalPermRefs")
  public List<Boolean> getEvalPermRefs() {
    return evalPermRefs;
  }
  public void setEvalPermRefs(List<Boolean> evalPermRefs) {
    this.evalPermRefs = evalPermRefs;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PrincipalInfo principalInfo = (PrincipalInfo) o;
    return Objects.equals(profileId, principalInfo.profileId) &&
        Objects.equals(profileUsername, principalInfo.profileUsername) &&
        Objects.equals(displayName, principalInfo.displayName) &&
        Objects.equals(emailAddress, principalInfo.emailAddress) &&
        Objects.equals(timezoneId, principalInfo.timezoneId) &&
        Objects.equals(languageTag, principalInfo.languageTag) &&
        Objects.equals(evalPermRefs, principalInfo.evalPermRefs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(profileId, profileUsername, displayName, emailAddress, timezoneId, languageTag, evalPermRefs);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PrincipalInfo {\n");
    
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
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

