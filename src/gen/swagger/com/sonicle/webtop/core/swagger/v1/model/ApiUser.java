package com.sonicle.webtop.core.swagger.v1.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sonicle.webtop.core.swagger.v1.model.ApiUserBase;
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

/**
 * The user data for listing purposes.
 **/
@ApiModel(description = "The user data for listing purposes.")
@JsonTypeName("User")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-03-16T17:01:06.128+01:00[Europe/Berlin]")
public class ApiUser extends ApiUserBase  {
  private @Valid String userId;
  private @Valid String userSid;

  /**
   * User ID, actually its name.
   **/
  public ApiUser userId(String userId) {
    this.userId = userId;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "User ID, actually its name.")
  @JsonProperty("userId")
  @NotNull
  public String getUserId() {
    return userId;
  }

  @JsonProperty("userId")
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * User SID (internal Subject IDentifier), auto-generated GUID.
   **/
  public ApiUser userSid(String userSid) {
    this.userSid = userSid;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "User SID (internal Subject IDentifier), auto-generated GUID.")
  @JsonProperty("userSid")
  @NotNull
  public String getUserSid() {
    return userSid;
  }

  @JsonProperty("userSid")
  public void setUserSid(String userSid) {
    this.userSid = userSid;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiUser user = (ApiUser) o;
    return Objects.equals(this.userId, user.userId) &&
        Objects.equals(this.userSid, user.userSid) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, userSid, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiUser {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    userSid: ").append(toIndentedString(userSid)).append("\n");
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

