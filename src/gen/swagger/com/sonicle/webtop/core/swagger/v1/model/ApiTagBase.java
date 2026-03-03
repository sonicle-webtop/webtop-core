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
 * Represent a tag with base updateable fields.
 **/
@ApiModel(description = "Represent a tag with base updateable fields.")
@JsonTypeName("TagBase")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-02-27T16:20:59.935+01:00[Europe/Berlin]")
public class ApiTagBase   {
  private @Valid Boolean builtIn;
  public enum VisibilityEnum {

    PRIVATE(String.valueOf("private")), SHARED(String.valueOf("shared"));


    private String value;

    VisibilityEnum (String v) {
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
	public static VisibilityEnum fromString(String s) {
        for (VisibilityEnum b : VisibilityEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (java.util.Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
	}
	
    @JsonCreator
    public static VisibilityEnum fromValue(String value) {
        for (VisibilityEnum b : VisibilityEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private @Valid VisibilityEnum visibility;
  private @Valid String name;
  private @Valid String color;
  private @Valid String externalId;

  /**
   **/
  public ApiTagBase builtIn(Boolean builtIn) {
    this.builtIn = builtIn;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("builtIn")
  public Boolean getBuiltIn() {
    return builtIn;
  }

  @JsonProperty("builtIn")
  public void setBuiltIn(Boolean builtIn) {
    this.builtIn = builtIn;
  }

  /**
   **/
  public ApiTagBase visibility(VisibilityEnum visibility) {
    this.visibility = visibility;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("visibility")
  @NotNull
  public VisibilityEnum getVisibility() {
    return visibility;
  }

  @JsonProperty("visibility")
  public void setVisibility(VisibilityEnum visibility) {
    this.visibility = visibility;
  }

  /**
   **/
  public ApiTagBase name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("name")
  @NotNull
  public String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public ApiTagBase color(String color) {
    this.color = color;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("color")
  @NotNull
  public String getColor() {
    return color;
  }

  @JsonProperty("color")
  public void setColor(String color) {
    this.color = color;
  }

  /**
   **/
  public ApiTagBase externalId(String externalId) {
    this.externalId = externalId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("externalId")
  public String getExternalId() {
    return externalId;
  }

  @JsonProperty("externalId")
  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiTagBase tagBase = (ApiTagBase) o;
    return Objects.equals(this.builtIn, tagBase.builtIn) &&
        Objects.equals(this.visibility, tagBase.visibility) &&
        Objects.equals(this.name, tagBase.name) &&
        Objects.equals(this.color, tagBase.color) &&
        Objects.equals(this.externalId, tagBase.externalId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(builtIn, visibility, name, color, externalId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiTagBase {\n");
    
    sb.append("    builtIn: ").append(toIndentedString(builtIn)).append("\n");
    sb.append("    visibility: ").append(toIndentedString(visibility)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    color: ").append(toIndentedString(color)).append("\n");
    sb.append("    externalId: ").append(toIndentedString(externalId)).append("\n");
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

