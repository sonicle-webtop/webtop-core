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



@JsonTypeName("License_allOf")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2023-01-19T09:40:24.427+01:00[Europe/Berlin]")
public class ApiLicenseAllOf   {
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

  /**
   * The unique product code.
   **/
  public ApiLicenseAllOf productCode(String productCode) {
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
  public ApiLicenseAllOf owningServiceId(String owningServiceId) {
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
  public ApiLicenseAllOf builtIn(Boolean builtIn) {
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
  public ApiLicenseAllOf revisionTimestamp(String revisionTimestamp) {
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
  public ApiLicenseAllOf activationTimestamp(String activationTimestamp) {
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
  public ApiLicenseAllOf activationHwId(String activationHwId) {
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
  public ApiLicenseAllOf expirationDate(String expirationDate) {
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
  public ApiLicenseAllOf status(Long status) {
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
  public ApiLicenseAllOf maxLease(Integer maxLease) {
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
  public ApiLicenseAllOf leases(List<ApiLicenseLease> leases) {
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

  public ApiLicenseAllOf addLeasesItem(ApiLicenseLease leasesItem) {
    if (this.leases == null) {
      this.leases = new ArrayList<>();
    }

    this.leases.add(leasesItem);
    return this;
  }

  public ApiLicenseAllOf removeLeasesItem(ApiLicenseLease leasesItem) {
    if (leasesItem != null && this.leases != null) {
      this.leases.remove(leasesItem);
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
    ApiLicenseAllOf licenseAllOf = (ApiLicenseAllOf) o;
    return Objects.equals(this.productCode, licenseAllOf.productCode) &&
        Objects.equals(this.owningServiceId, licenseAllOf.owningServiceId) &&
        Objects.equals(this.builtIn, licenseAllOf.builtIn) &&
        Objects.equals(this.revisionTimestamp, licenseAllOf.revisionTimestamp) &&
        Objects.equals(this.activationTimestamp, licenseAllOf.activationTimestamp) &&
        Objects.equals(this.activationHwId, licenseAllOf.activationHwId) &&
        Objects.equals(this.expirationDate, licenseAllOf.expirationDate) &&
        Objects.equals(this.status, licenseAllOf.status) &&
        Objects.equals(this.maxLease, licenseAllOf.maxLease) &&
        Objects.equals(this.leases, licenseAllOf.leases);
  }

  @Override
  public int hashCode() {
    return Objects.hash(productCode, owningServiceId, builtIn, revisionTimestamp, activationTimestamp, activationHwId, expirationDate, status, maxLease, leases);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiLicenseAllOf {\n");
    
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

