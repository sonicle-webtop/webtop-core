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
 * Represents a recipient for communications.
 **/
@ApiModel(description = "Represents a recipient for communications.")
@JsonTypeName("Recipient")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-03-16T17:01:06.128+01:00[Europe/Berlin]")
public class ApiRecipient   {
  private @Valid String sourceId;
  private @Valid String sourceName;
  private @Valid String origin;
  private @Valid String recipientId;
  private @Valid String personal;
  private @Valid String address;

  /**
   * The RecipientsProvider&#39;s ID.
   **/
  public ApiRecipient sourceId(String sourceId) {
    this.sourceId = sourceId;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The RecipientsProvider's ID.")
  @JsonProperty("sourceId")
  @NotNull
  public String getSourceId() {
    return sourceId;
  }

  @JsonProperty("sourceId")
  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
  }

  /**
   * The RecipientsProvider&#39;s name.
   **/
  public ApiRecipient sourceName(String sourceName) {
    this.sourceName = sourceName;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The RecipientsProvider's name.")
  @JsonProperty("sourceName")
  @NotNull
  public String getSourceName() {
    return sourceName;
  }

  @JsonProperty("sourceName")
  public void setSourceName(String sourceName) {
    this.sourceName = sourceName;
  }

  /**
   **/
  public ApiRecipient origin(String origin) {
    this.origin = origin;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("origin")
  public String getOrigin() {
    return origin;
  }

  @JsonProperty("origin")
  public void setOrigin(String origin) {
    this.origin = origin;
  }

  /**
   * Provider&#39;s local ID of the recipient entry, if any.
   **/
  public ApiRecipient recipientId(String recipientId) {
    this.recipientId = recipientId;
    return this;
  }

  
  @ApiModelProperty(value = "Provider's local ID of the recipient entry, if any.")
  @JsonProperty("recipientId")
  public String getRecipientId() {
    return recipientId;
  }

  @JsonProperty("recipientId")
  public void setRecipientId(String recipientId) {
    this.recipientId = recipientId;
  }

  /**
   * The recipient personal name.
   **/
  public ApiRecipient personal(String personal) {
    this.personal = personal;
    return this;
  }

  
  @ApiModelProperty(value = "The recipient personal name.")
  @JsonProperty("personal")
  public String getPersonal() {
    return personal;
  }

  @JsonProperty("personal")
  public void setPersonal(String personal) {
    this.personal = personal;
  }

  /**
   * The recipient address (eg. email, number, etc...)
   **/
  public ApiRecipient address(String address) {
    this.address = address;
    return this;
  }

  
  @ApiModelProperty(value = "The recipient address (eg. email, number, etc...)")
  @JsonProperty("address")
  public String getAddress() {
    return address;
  }

  @JsonProperty("address")
  public void setAddress(String address) {
    this.address = address;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiRecipient recipient = (ApiRecipient) o;
    return Objects.equals(this.sourceId, recipient.sourceId) &&
        Objects.equals(this.sourceName, recipient.sourceName) &&
        Objects.equals(this.origin, recipient.origin) &&
        Objects.equals(this.recipientId, recipient.recipientId) &&
        Objects.equals(this.personal, recipient.personal) &&
        Objects.equals(this.address, recipient.address);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sourceId, sourceName, origin, recipientId, personal, address);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiRecipient {\n");
    
    sb.append("    sourceId: ").append(toIndentedString(sourceId)).append("\n");
    sb.append("    sourceName: ").append(toIndentedString(sourceName)).append("\n");
    sb.append("    origin: ").append(toIndentedString(origin)).append("\n");
    sb.append("    recipientId: ").append(toIndentedString(recipientId)).append("\n");
    sb.append("    personal: ").append(toIndentedString(personal)).append("\n");
    sb.append("    address: ").append(toIndentedString(address)).append("\n");
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

