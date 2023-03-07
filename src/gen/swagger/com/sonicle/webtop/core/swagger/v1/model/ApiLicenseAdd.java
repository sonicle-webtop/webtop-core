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

/**
 * The license data for insertion.
 **/
@ApiModel(description = "The license data for insertion.")
@JsonTypeName("LicenseAdd")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2023-01-19T09:40:24.427+01:00[Europe/Berlin]")
public class ApiLicenseAdd   {
  private @Valid String licenseString;
  private @Valid String activatedLicenseString;
  private @Valid Boolean autoLease;
  private @Valid List<String> assignedLeases = null;

  /**
   * The license text string.
   **/
  public ApiLicenseAdd licenseString(String licenseString) {
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
  public ApiLicenseAdd activatedLicenseString(String activatedLicenseString) {
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
  public ApiLicenseAdd autoLease(Boolean autoLease) {
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

  /**
   * A set of user IDs to assign a lease of this license.
   **/
  public ApiLicenseAdd assignedLeases(List<String> assignedLeases) {
    this.assignedLeases = assignedLeases;
    return this;
  }

  
  @ApiModelProperty(value = "A set of user IDs to assign a lease of this license.")
  @JsonProperty("assignedLeases")
  public List<String> getAssignedLeases() {
    return assignedLeases;
  }

  @JsonProperty("assignedLeases")
  public void setAssignedLeases(List<String> assignedLeases) {
    this.assignedLeases = assignedLeases;
  }

  public ApiLicenseAdd addAssignedLeasesItem(String assignedLeasesItem) {
    if (this.assignedLeases == null) {
      this.assignedLeases = new ArrayList<>();
    }

    this.assignedLeases.add(assignedLeasesItem);
    return this;
  }

  public ApiLicenseAdd removeAssignedLeasesItem(String assignedLeasesItem) {
    if (assignedLeasesItem != null && this.assignedLeases != null) {
      this.assignedLeases.remove(assignedLeasesItem);
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
    ApiLicenseAdd licenseAdd = (ApiLicenseAdd) o;
    return Objects.equals(this.licenseString, licenseAdd.licenseString) &&
        Objects.equals(this.activatedLicenseString, licenseAdd.activatedLicenseString) &&
        Objects.equals(this.autoLease, licenseAdd.autoLease) &&
        Objects.equals(this.assignedLeases, licenseAdd.assignedLeases);
  }

  @Override
  public int hashCode() {
    return Objects.hash(licenseString, activatedLicenseString, autoLease, assignedLeases);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiLicenseAdd {\n");
    
    sb.append("    licenseString: ").append(toIndentedString(licenseString)).append("\n");
    sb.append("    activatedLicenseString: ").append(toIndentedString(activatedLicenseString)).append("\n");
    sb.append("    autoLease: ").append(toIndentedString(autoLease)).append("\n");
    sb.append("    assignedLeases: ").append(toIndentedString(assignedLeases)).append("\n");
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

