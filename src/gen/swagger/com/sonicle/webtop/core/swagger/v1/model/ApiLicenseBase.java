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
 * The license base data.
 **/
@ApiModel(description = "The license base data.")
@JsonTypeName("LicenseBase")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-04-16T12:49:23.936+02:00[Europe/Berlin]")
public class ApiLicenseBase   {
  private @Valid String licenseString;
  private @Valid String activatedLicenseString;
  private @Valid Boolean autoLease;

  /**
   * The license text string.
   **/
  public ApiLicenseBase licenseString(String licenseString) {
    this.licenseString = licenseString;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The license text string.")
  @JsonProperty("licenseString")
  @NotNull
  public String getLicenseString() {
    return licenseString;
  }

  @JsonProperty("licenseString")
  public void setLicenseString(String licenseString) {
    this.licenseString = licenseString;
  }

  /**
   * The licence activation string.
   **/
  public ApiLicenseBase activatedLicenseString(String activatedLicenseString) {
    this.activatedLicenseString = activatedLicenseString;
    return this;
  }

  
  @ApiModelProperty(value = "The licence activation string.")
  @JsonProperty("activatedLicenseString")
  public String getActivatedLicenseString() {
    return activatedLicenseString;
  }

  @JsonProperty("activatedLicenseString")
  public void setActivatedLicenseString(String activatedLicenseString) {
    this.activatedLicenseString = activatedLicenseString;
  }

  /**
   * Specifies if auto-lease feature is active for this license.
   **/
  public ApiLicenseBase autoLease(Boolean autoLease) {
    this.autoLease = autoLease;
    return this;
  }

  
  @ApiModelProperty(value = "Specifies if auto-lease feature is active for this license.")
  @JsonProperty("autoLease")
  public Boolean getAutoLease() {
    return autoLease;
  }

  @JsonProperty("autoLease")
  public void setAutoLease(Boolean autoLease) {
    this.autoLease = autoLease;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiLicenseBase licenseBase = (ApiLicenseBase) o;
    return Objects.equals(this.licenseString, licenseBase.licenseString) &&
        Objects.equals(this.activatedLicenseString, licenseBase.activatedLicenseString) &&
        Objects.equals(this.autoLease, licenseBase.autoLease);
  }

  @Override
  public int hashCode() {
    return Objects.hash(licenseString, activatedLicenseString, autoLease);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiLicenseBase {\n");
    
    sb.append("    licenseString: ").append(toIndentedString(licenseString)).append("\n");
    sb.append("    activatedLicenseString: ").append(toIndentedString(activatedLicenseString)).append("\n");
    sb.append("    autoLease: ").append(toIndentedString(autoLease)).append("\n");
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

