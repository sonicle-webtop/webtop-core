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
 * The parameters for some LDAP based domain authentication directories (ldap, ldapneth, ad).
 **/
@ApiModel(description = "The parameters for some LDAP based domain authentication directories (ldap, ldapneth, ad).")
@JsonTypeName("LdapDirectoryParams")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-04-16T12:49:23.936+02:00[Europe/Berlin]")
public class ApiLdapDirectoryParams   {
  private @Valid String loginDn;
  private @Valid String loginFilter;
  private @Valid String userDn;
  private @Valid String userFilter;
  private @Valid String userIdField;
  private @Valid String userFirstnameField;
  private @Valid String userLastnameField;
  private @Valid String userDisplayNameField;

  /**
   **/
  public ApiLdapDirectoryParams loginDn(String loginDn) {
    this.loginDn = loginDn;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("loginDn")
  public String getLoginDn() {
    return loginDn;
  }

  @JsonProperty("loginDn")
  public void setLoginDn(String loginDn) {
    this.loginDn = loginDn;
  }

  /**
   **/
  public ApiLdapDirectoryParams loginFilter(String loginFilter) {
    this.loginFilter = loginFilter;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("loginFilter")
  public String getLoginFilter() {
    return loginFilter;
  }

  @JsonProperty("loginFilter")
  public void setLoginFilter(String loginFilter) {
    this.loginFilter = loginFilter;
  }

  /**
   **/
  public ApiLdapDirectoryParams userDn(String userDn) {
    this.userDn = userDn;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("userDn")
  public String getUserDn() {
    return userDn;
  }

  @JsonProperty("userDn")
  public void setUserDn(String userDn) {
    this.userDn = userDn;
  }

  /**
   **/
  public ApiLdapDirectoryParams userFilter(String userFilter) {
    this.userFilter = userFilter;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("userFilter")
  public String getUserFilter() {
    return userFilter;
  }

  @JsonProperty("userFilter")
  public void setUserFilter(String userFilter) {
    this.userFilter = userFilter;
  }

  /**
   **/
  public ApiLdapDirectoryParams userIdField(String userIdField) {
    this.userIdField = userIdField;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("userIdField")
  public String getUserIdField() {
    return userIdField;
  }

  @JsonProperty("userIdField")
  public void setUserIdField(String userIdField) {
    this.userIdField = userIdField;
  }

  /**
   **/
  public ApiLdapDirectoryParams userFirstnameField(String userFirstnameField) {
    this.userFirstnameField = userFirstnameField;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("userFirstnameField")
  public String getUserFirstnameField() {
    return userFirstnameField;
  }

  @JsonProperty("userFirstnameField")
  public void setUserFirstnameField(String userFirstnameField) {
    this.userFirstnameField = userFirstnameField;
  }

  /**
   **/
  public ApiLdapDirectoryParams userLastnameField(String userLastnameField) {
    this.userLastnameField = userLastnameField;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("userLastnameField")
  public String getUserLastnameField() {
    return userLastnameField;
  }

  @JsonProperty("userLastnameField")
  public void setUserLastnameField(String userLastnameField) {
    this.userLastnameField = userLastnameField;
  }

  /**
   **/
  public ApiLdapDirectoryParams userDisplayNameField(String userDisplayNameField) {
    this.userDisplayNameField = userDisplayNameField;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("userDisplayNameField")
  public String getUserDisplayNameField() {
    return userDisplayNameField;
  }

  @JsonProperty("userDisplayNameField")
  public void setUserDisplayNameField(String userDisplayNameField) {
    this.userDisplayNameField = userDisplayNameField;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiLdapDirectoryParams ldapDirectoryParams = (ApiLdapDirectoryParams) o;
    return Objects.equals(this.loginDn, ldapDirectoryParams.loginDn) &&
        Objects.equals(this.loginFilter, ldapDirectoryParams.loginFilter) &&
        Objects.equals(this.userDn, ldapDirectoryParams.userDn) &&
        Objects.equals(this.userFilter, ldapDirectoryParams.userFilter) &&
        Objects.equals(this.userIdField, ldapDirectoryParams.userIdField) &&
        Objects.equals(this.userFirstnameField, ldapDirectoryParams.userFirstnameField) &&
        Objects.equals(this.userLastnameField, ldapDirectoryParams.userLastnameField) &&
        Objects.equals(this.userDisplayNameField, ldapDirectoryParams.userDisplayNameField);
  }

  @Override
  public int hashCode() {
    return Objects.hash(loginDn, loginFilter, userDn, userFilter, userIdField, userFirstnameField, userLastnameField, userDisplayNameField);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiLdapDirectoryParams {\n");
    
    sb.append("    loginDn: ").append(toIndentedString(loginDn)).append("\n");
    sb.append("    loginFilter: ").append(toIndentedString(loginFilter)).append("\n");
    sb.append("    userDn: ").append(toIndentedString(userDn)).append("\n");
    sb.append("    userFilter: ").append(toIndentedString(userFilter)).append("\n");
    sb.append("    userIdField: ").append(toIndentedString(userIdField)).append("\n");
    sb.append("    userFirstnameField: ").append(toIndentedString(userFirstnameField)).append("\n");
    sb.append("    userLastnameField: ").append(toIndentedString(userLastnameField)).append("\n");
    sb.append("    userDisplayNameField: ").append(toIndentedString(userDisplayNameField)).append("\n");
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

