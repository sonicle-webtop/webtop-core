package com.sonicle.webtop.core.swagger.v1.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sonicle.webtop.core.swagger.v1.model.ApiLicenseLease;
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
 * The license data.
 **/
@ApiModel(description = "The license data.")
@JsonTypeName("License")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2023-01-19T09:40:24.427+01:00[Europe/Berlin]")
public class ApiLicense   {
  private @Valid String productCode;
  private @Valid String owningServiceId;
  private @Valid Boolean builtIn;
  private @Valid String revisionTimestamp;
  private @Valid String activationTimestamp;
  private @Valid String activationHwId;
  private @Valid String expirationDate;
  private @Valid Long status;
  private @Valid Integer maxLease;
  private @Valid List<ApiLicenseLease> leases = new ArrayList<>();
  private @Valid String licenseString;
  private @Valid String activatedLicenseString;
  private @Valid Boolean autoLease;

  /**
   * The unique product code.
   **/
  public ApiLicense productCode(String productCode) {
    this.productCode = productCode;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The unique product code.")
  @JsonProperty("productCode")
  @NotNull
  public String getProductCode() {
    return productCode;
  }

  @JsonProperty("productCode")
  public void setProductCode(String productCode) {
    this.productCode = productCode;
  }

  /**
   * The service ID.
   **/
  public ApiLicense owningServiceId(String owningServiceId) {
    this.owningServiceId = owningServiceId;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The service ID.")
  @JsonProperty("owningServiceId")
  @NotNull
  public String getOwningServiceId() {
    return owningServiceId;
  }

  @JsonProperty("owningServiceId")
  public void setOwningServiceId(String owningServiceId) {
    this.owningServiceId = owningServiceId;
  }

  /**
   * Specifies if the license is included in distribution or not.
   **/
  public ApiLicense builtIn(Boolean builtIn) {
    this.builtIn = builtIn;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Specifies if the license is included in distribution or not.")
  @JsonProperty("builtIn")
  @NotNull
  public Boolean getBuiltIn() {
    return builtIn;
  }

  @JsonProperty("builtIn")
  public void setBuiltIn(Boolean builtIn) {
    this.builtIn = builtIn;
  }

  /**
   * Modification instant (ISO date/time YYYYMMDD&#39;T&#39;HHMMSS&#39;Z&#39;).
   **/
  public ApiLicense revisionTimestamp(String revisionTimestamp) {
    this.revisionTimestamp = revisionTimestamp;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Modification instant (ISO date/time YYYYMMDD'T'HHMMSS'Z').")
  @JsonProperty("revisionTimestamp")
  @NotNull
  public String getRevisionTimestamp() {
    return revisionTimestamp;
  }

  @JsonProperty("revisionTimestamp")
  public void setRevisionTimestamp(String revisionTimestamp) {
    this.revisionTimestamp = revisionTimestamp;
  }

  /**
   * Activation instant (ISO date/time YYYYMMDD&#39;T&#39;HHMMSS&#39;Z&#39;).
   **/
  public ApiLicense activationTimestamp(String activationTimestamp) {
    this.activationTimestamp = activationTimestamp;
    return this;
  }

  
  @ApiModelProperty(value = "Activation instant (ISO date/time YYYYMMDD'T'HHMMSS'Z').")
  @JsonProperty("activationTimestamp")
  public String getActivationTimestamp() {
    return activationTimestamp;
  }

  @JsonProperty("activationTimestamp")
  public void setActivationTimestamp(String activationTimestamp) {
    this.activationTimestamp = activationTimestamp;
  }

  /**
   * The hardware ID used during activation process.
   **/
  public ApiLicense activationHwId(String activationHwId) {
    this.activationHwId = activationHwId;
    return this;
  }

  
  @ApiModelProperty(value = "The hardware ID used during activation process.")
  @JsonProperty("activationHwId")
  public String getActivationHwId() {
    return activationHwId;
  }

  @JsonProperty("activationHwId")
  public void setActivationHwId(String activationHwId) {
    this.activationHwId = activationHwId;
  }

  /**
   * The optional expiration date (ISO date YYYYMMDD).
   **/
  public ApiLicense expirationDate(String expirationDate) {
    this.expirationDate = expirationDate;
    return this;
  }

  
  @ApiModelProperty(value = "The optional expiration date (ISO date YYYYMMDD).")
  @JsonProperty("expirationDate")
  public String getExpirationDate() {
    return expirationDate;
  }

  @JsonProperty("expirationDate")
  public void setExpirationDate(String expirationDate) {
    this.expirationDate = expirationDate;
  }

  /**
   * The current computed status of the license. It can assume one of the following values, or a sum of multiple values: 1 - VALID, 2 - ACTIVATED, 4 - PENDING_ACTIVATION, 8 - EXPIRED, 16 - EXPIRE_SOON
   **/
  public ApiLicense status(Long status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The current computed status of the license. It can assume one of the following values, or a sum of multiple values: 1 - VALID, 2 - ACTIVATED, 4 - PENDING_ACTIVATION, 8 - EXPIRED, 16 - EXPIRE_SOON")
  @JsonProperty("status")
  @NotNull
  public Long getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(Long status) {
    this.status = status;
  }

  /**
   * The number of CALs of the license, if expected.
   **/
  public ApiLicense maxLease(Integer maxLease) {
    this.maxLease = maxLease;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The number of CALs of the license, if expected.")
  @JsonProperty("maxLease")
  @NotNull
  public Integer getMaxLease() {
    return maxLease;
  }

  @JsonProperty("maxLease")
  public void setMaxLease(Integer maxLease) {
    this.maxLease = maxLease;
  }

  /**
   * Collection of active leases.
   **/
  public ApiLicense leases(List<ApiLicenseLease> leases) {
    this.leases = leases;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Collection of active leases.")
  @JsonProperty("leases")
  @NotNull
  public List<ApiLicenseLease> getLeases() {
    return leases;
  }

  @JsonProperty("leases")
  public void setLeases(List<ApiLicenseLease> leases) {
    this.leases = leases;
  }

  public ApiLicense addLeasesItem(ApiLicenseLease leasesItem) {
    if (this.leases == null) {
      this.leases = new ArrayList<>();
    }

    this.leases.add(leasesItem);
    return this;
  }

  public ApiLicense removeLeasesItem(ApiLicenseLease leasesItem) {
    if (leasesItem != null && this.leases != null) {
      this.leases.remove(leasesItem);
    }

    return this;
  }
  /**
   * The license text string.
   **/
  public ApiLicense licenseString(String licenseString) {
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
  public ApiLicense activatedLicenseString(String activatedLicenseString) {
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
  public ApiLicense autoLease(Boolean autoLease) {
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
    ApiLicense license = (ApiLicense) o;
    return Objects.equals(this.productCode, license.productCode) &&
        Objects.equals(this.owningServiceId, license.owningServiceId) &&
        Objects.equals(this.builtIn, license.builtIn) &&
        Objects.equals(this.revisionTimestamp, license.revisionTimestamp) &&
        Objects.equals(this.activationTimestamp, license.activationTimestamp) &&
        Objects.equals(this.activationHwId, license.activationHwId) &&
        Objects.equals(this.expirationDate, license.expirationDate) &&
        Objects.equals(this.status, license.status) &&
        Objects.equals(this.maxLease, license.maxLease) &&
        Objects.equals(this.leases, license.leases) &&
        Objects.equals(this.licenseString, license.licenseString) &&
        Objects.equals(this.activatedLicenseString, license.activatedLicenseString) &&
        Objects.equals(this.autoLease, license.autoLease);
  }

  @Override
  public int hashCode() {
    return Objects.hash(productCode, owningServiceId, builtIn, revisionTimestamp, activationTimestamp, activationHwId, expirationDate, status, maxLease, leases, licenseString, activatedLicenseString, autoLease);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiLicense {\n");
    
    sb.append("    productCode: ").append(toIndentedString(productCode)).append("\n");
    sb.append("    owningServiceId: ").append(toIndentedString(owningServiceId)).append("\n");
    sb.append("    builtIn: ").append(toIndentedString(builtIn)).append("\n");
    sb.append("    revisionTimestamp: ").append(toIndentedString(revisionTimestamp)).append("\n");
    sb.append("    activationTimestamp: ").append(toIndentedString(activationTimestamp)).append("\n");
    sb.append("    activationHwId: ").append(toIndentedString(activationHwId)).append("\n");
    sb.append("    expirationDate: ").append(toIndentedString(expirationDate)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    maxLease: ").append(toIndentedString(maxLease)).append("\n");
    sb.append("    leases: ").append(toIndentedString(leases)).append("\n");
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

