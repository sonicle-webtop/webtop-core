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



@JsonTypeName("LicenseAdd_allOf")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2023-01-19T09:40:24.427+01:00[Europe/Berlin]")
public class ApiLicenseAddAllOf   {
  private @Valid List<String> assignedLeases = null;

  /**
   * A set of user IDs to assign a lease of this license.
   **/
  public ApiLicenseAddAllOf assignedLeases(List<String> assignedLeases) {
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

  public ApiLicenseAddAllOf addAssignedLeasesItem(String assignedLeasesItem) {
    if (this.assignedLeases == null) {
      this.assignedLeases = new ArrayList<>();
    }

    this.assignedLeases.add(assignedLeasesItem);
    return this;
  }

  public ApiLicenseAddAllOf removeAssignedLeasesItem(String assignedLeasesItem) {
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
    ApiLicenseAddAllOf licenseAddAllOf = (ApiLicenseAddAllOf) o;
    return Objects.equals(this.assignedLeases, licenseAddAllOf.assignedLeases);
  }

  @Override
  public int hashCode() {
    return Objects.hash(assignedLeases);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiLicenseAddAllOf {\n");
    
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

