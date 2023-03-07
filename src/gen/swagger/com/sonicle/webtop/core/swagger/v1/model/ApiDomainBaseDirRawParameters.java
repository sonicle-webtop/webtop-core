package com.sonicle.webtop.core.swagger.v1.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sonicle.webtop.core.swagger.v1.model.ApiLdapDirectoryParams;
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
 * Raw Directory parameters to be used with some schemes: ldap, ldapneth, ad
 **/
@ApiModel(description = "Raw Directory parameters to be used with some schemes: ldap, ldapneth, ad")
@JsonTypeName("DomainBase_dirRawParameters")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2023-01-19T09:40:24.427+01:00[Europe/Berlin]")
public class ApiDomainBaseDirRawParameters   {
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
  public ApiDomainBaseDirRawParameters loginDn(String loginDn) {
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
  public ApiDomainBaseDirRawParameters loginFilter(String loginFilter) {
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
  public ApiDomainBaseDirRawParameters userDn(String userDn) {
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
  public ApiDomainBaseDirRawParameters userFilter(String userFilter) {
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
  public ApiDomainBaseDirRawParameters userIdField(String userIdField) {
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
  public ApiDomainBaseDirRawParameters userFirstnameField(String userFirstnameField) {
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
  public ApiDomainBaseDirRawParameters userLastnameField(String userLastnameField) {
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
  public ApiDomainBaseDirRawParameters userDisplayNameField(String userDisplayNameField) {
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
    ApiDomainBaseDirRawParameters domainBaseDirRawParameters = (ApiDomainBaseDirRawParameters) o;
    return Objects.equals(this.loginDn, domainBaseDirRawParameters.loginDn) &&
        Objects.equals(this.loginFilter, domainBaseDirRawParameters.loginFilter) &&
        Objects.equals(this.userDn, domainBaseDirRawParameters.userDn) &&
        Objects.equals(this.userFilter, domainBaseDirRawParameters.userFilter) &&
        Objects.equals(this.userIdField, domainBaseDirRawParameters.userIdField) &&
        Objects.equals(this.userFirstnameField, domainBaseDirRawParameters.userFirstnameField) &&
        Objects.equals(this.userLastnameField, domainBaseDirRawParameters.userLastnameField) &&
        Objects.equals(this.userDisplayNameField, domainBaseDirRawParameters.userDisplayNameField);
  }

  @Override
  public int hashCode() {
    return Objects.hash(loginDn, loginFilter, userDn, userFilter, userIdField, userFirstnameField, userLastnameField, userDisplayNameField);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiDomainBaseDirRawParameters {\n");
    
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

