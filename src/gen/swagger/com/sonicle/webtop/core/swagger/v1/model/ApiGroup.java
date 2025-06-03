package com.sonicle.webtop.core.swagger.v1.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sonicle.webtop.core.swagger.v1.model.ApiGroupBase;
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
 * The group data for listing purposes.
 **/
@ApiModel(description = "The group data for listing purposes.")
@JsonTypeName("Group")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-06-03T16:46:12.383+02:00[Europe/Berlin]")
public class ApiGroup extends ApiGroupBase  {
  private @Valid String groupId;
  private @Valid String groupSid;
  private @Valid Boolean builtIn;

  /**
   * Group ID, actually its name.
   **/
  public ApiGroup groupId(String groupId) {
    this.groupId = groupId;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Group ID, actually its name.")
  @JsonProperty("groupId")
  @NotNull
  public String getGroupId() {
    return groupId;
  }

  @JsonProperty("groupId")
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  /**
   * Group SID (internal Subject IDentifier), auto-generated GUID.
   **/
  public ApiGroup groupSid(String groupSid) {
    this.groupSid = groupSid;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Group SID (internal Subject IDentifier), auto-generated GUID.")
  @JsonProperty("groupSid")
  @NotNull
  public String getGroupSid() {
    return groupSid;
  }

  @JsonProperty("groupSid")
  public void setGroupSid(String groupSid) {
    this.groupSid = groupSid;
  }

  /**
   * Specifies if it is an auto-generated Group.
   **/
  public ApiGroup builtIn(Boolean builtIn) {
    this.builtIn = builtIn;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Specifies if it is an auto-generated Group.")
  @JsonProperty("builtIn")
  @NotNull
  public Boolean getBuiltIn() {
    return builtIn;
  }

  @JsonProperty("builtIn")
  public void setBuiltIn(Boolean builtIn) {
    this.builtIn = builtIn;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiGroup group = (ApiGroup) o;
    return Objects.equals(this.groupId, group.groupId) &&
        Objects.equals(this.groupSid, group.groupSid) &&
        Objects.equals(this.builtIn, group.builtIn) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(groupId, groupSid, builtIn, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiGroup {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    groupId: ").append(toIndentedString(groupId)).append("\n");
    sb.append("    groupSid: ").append(toIndentedString(groupSid)).append("\n");
    sb.append("    builtIn: ").append(toIndentedString(builtIn)).append("\n");
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

