package com.sonicle.webtop.core.swagger.v1.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sonicle.webtop.core.swagger.v1.model.ApiLicenseBase;
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
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-02-27T16:20:59.935+01:00[Europe/Berlin]")
public class ApiLicenseAdd extends ApiLicenseBase  {
  private @Valid List<String> assignedLeases;

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
    return Objects.equals(this.assignedLeases, licenseAdd.assignedLeases) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(assignedLeases, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiLicenseAdd {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
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

