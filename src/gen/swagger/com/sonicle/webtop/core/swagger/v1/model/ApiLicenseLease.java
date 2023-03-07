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
 * The lease active on a user.
 **/
@ApiModel(description = "The lease active on a user.")
@JsonTypeName("LicenseLease")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2023-01-19T09:40:24.427+01:00[Europe/Berlin]")
public class ApiLicenseLease   {
  private @Valid String userId;
  private @Valid String timestamp;
  public enum OriginEnum {

    STATIC(String.valueOf("static")), AUTO(String.valueOf("auto"));


    private String value;

    OriginEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Convert a String into String, as specified in the
     * <a href="https://download.oracle.com/otndocs/jcp/jaxrs-2_0-fr-eval-spec/index.html">See JAX RS 2.0 Specification, section 3.2, p. 12</a>
     */
	public static OriginEnum fromString(String s) {
        for (OriginEnum b : OriginEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (java.util.Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
	}
	
    @JsonCreator
    public static OriginEnum fromValue(String value) {
        for (OriginEnum b : OriginEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private @Valid OriginEnum origin;

  /**
   * The user ID holding the lease.
   **/
  public ApiLicenseLease userId(String userId) {
    this.userId = userId;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The user ID holding the lease.")
  @JsonProperty("userId")
  @NotNull
  public String getUserId() {
    return userId;
  }

  @JsonProperty("userId")
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * The instant at which it was activated (ISO date/time YYYYMMDD&#39;T&#39;HHMMSS&#39;Z&#39;).
   **/
  public ApiLicenseLease timestamp(String timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The instant at which it was activated (ISO date/time YYYYMMDD'T'HHMMSS'Z').")
  @JsonProperty("timestamp")
  @NotNull
  public String getTimestamp() {
    return timestamp;
  }

  @JsonProperty("timestamp")
  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  /**
   * The type of attribution when activated.
   **/
  public ApiLicenseLease origin(OriginEnum origin) {
    this.origin = origin;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The type of attribution when activated.")
  @JsonProperty("origin")
  @NotNull
  public OriginEnum getOrigin() {
    return origin;
  }

  @JsonProperty("origin")
  public void setOrigin(OriginEnum origin) {
    this.origin = origin;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiLicenseLease licenseLease = (ApiLicenseLease) o;
    return Objects.equals(this.userId, licenseLease.userId) &&
        Objects.equals(this.timestamp, licenseLease.timestamp) &&
        Objects.equals(this.origin, licenseLease.origin);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, timestamp, origin);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiLicenseLease {\n");
    
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
    sb.append("    origin: ").append(toIndentedString(origin)).append("\n");
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

