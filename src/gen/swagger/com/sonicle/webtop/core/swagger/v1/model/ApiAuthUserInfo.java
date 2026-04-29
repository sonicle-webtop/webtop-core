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
 * Compact authenticated-user descriptor returned alongside a token pair on /auth/login.
 **/
@ApiModel(description = "Compact authenticated-user descriptor returned alongside a token pair on /auth/login.")
@JsonTypeName("AuthUserInfo")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-04-29T10:39:20.102+02:00[Europe/Berlin]")
public class ApiAuthUserInfo   {
  private @Valid String profileId;
  private @Valid String profileUsername;
  private @Valid String displayName;

  /**
   * Internal profile ID (user@wtdomain)
   **/
  public ApiAuthUserInfo profileId(String profileId) {
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
  public ApiAuthUserInfo profileUsername(String profileUsername) {
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
  public ApiAuthUserInfo displayName(String displayName) {
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


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiAuthUserInfo authUserInfo = (ApiAuthUserInfo) o;
    return Objects.equals(this.profileId, authUserInfo.profileId) &&
        Objects.equals(this.profileUsername, authUserInfo.profileUsername) &&
        Objects.equals(this.displayName, authUserInfo.displayName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(profileId, profileUsername, displayName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiAuthUserInfo {\n");
    
    sb.append("    profileId: ").append(toIndentedString(profileId)).append("\n");
    sb.append("    profileUsername: ").append(toIndentedString(profileUsername)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
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

