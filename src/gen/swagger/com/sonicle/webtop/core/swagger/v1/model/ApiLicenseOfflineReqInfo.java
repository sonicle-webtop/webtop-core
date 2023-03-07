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



@JsonTypeName("LicenseOfflineReqInfo")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2023-01-19T09:40:24.427+01:00[Europe/Berlin]")
public class ApiLicenseOfflineReqInfo   {
  private @Valid String url;
  private @Valid String requestString;
  private @Valid String hardwareId;

  /**
   **/
  public ApiLicenseOfflineReqInfo url(String url) {
    this.url = url;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("url")
  @NotNull
  public String getUrl() {
    return url;
  }

  @JsonProperty("url")
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   **/
  public ApiLicenseOfflineReqInfo requestString(String requestString) {
    this.requestString = requestString;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("requestString")
  @NotNull
  public String getRequestString() {
    return requestString;
  }

  @JsonProperty("requestString")
  public void setRequestString(String requestString) {
    this.requestString = requestString;
  }

  /**
   **/
  public ApiLicenseOfflineReqInfo hardwareId(String hardwareId) {
    this.hardwareId = hardwareId;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("hardwareId")
  @NotNull
  public String getHardwareId() {
    return hardwareId;
  }

  @JsonProperty("hardwareId")
  public void setHardwareId(String hardwareId) {
    this.hardwareId = hardwareId;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiLicenseOfflineReqInfo licenseOfflineReqInfo = (ApiLicenseOfflineReqInfo) o;
    return Objects.equals(this.url, licenseOfflineReqInfo.url) &&
        Objects.equals(this.requestString, licenseOfflineReqInfo.requestString) &&
        Objects.equals(this.hardwareId, licenseOfflineReqInfo.hardwareId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(url, requestString, hardwareId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiLicenseOfflineReqInfo {\n");
    
    sb.append("    url: ").append(toIndentedString(url)).append("\n");
    sb.append("    requestString: ").append(toIndentedString(requestString)).append("\n");
    sb.append("    hardwareId: ").append(toIndentedString(hardwareId)).append("\n");
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

