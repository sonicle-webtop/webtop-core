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
 * The resource base data.
 **/
@ApiModel(description = "The resource base data.")
@JsonTypeName("ResourceBase")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2023-01-19T09:40:24.427+01:00[Europe/Berlin]")
public class ApiResourceBase   {
  private @Valid Boolean enabled;
  public enum TypeEnum {

    ROOM(String.valueOf("room")), EQUIPMENT(String.valueOf("equipment"));


    private String value;

    TypeEnum (String v) {
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
	public static TypeEnum fromString(String s) {
        for (TypeEnum b : TypeEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (java.util.Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
	}
	
    @JsonCreator
    public static TypeEnum fromValue(String value) {
        for (TypeEnum b : TypeEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private @Valid TypeEnum type;
  private @Valid String displayName;
  private @Valid String email;
  private @Valid String managerSubject;
  private @Valid List<String> allowedSubjects = null;

  /**
   * Represent the availability status
   **/
  public ApiResourceBase enabled(Boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Represent the availability status")
  @JsonProperty("enabled")
  @NotNull
  public Boolean getEnabled() {
    return enabled;
  }

  @JsonProperty("enabled")
  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Type of the Resource.
   **/
  public ApiResourceBase type(TypeEnum type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Type of the Resource.")
  @JsonProperty("type")
  @NotNull
  public TypeEnum getType() {
    return type;
  }

  @JsonProperty("type")
  public void setType(TypeEnum type) {
    this.type = type;
  }

  /**
   * Descriptive name to display purposes
   **/
  public ApiResourceBase displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(value = "Descriptive name to display purposes")
  @JsonProperty("displayName")
  public String getDisplayName() {
    return displayName;
  }

  @JsonProperty("displayName")
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * The email address associated
   **/
  public ApiResourceBase email(String email) {
    this.email = email;
    return this;
  }

  
  @ApiModelProperty(value = "The email address associated")
  @JsonProperty("email")
  public String getEmail() {
    return email;
  }

  @JsonProperty("email")
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * Reference to a User/Group that acts as Manager. It can be an ID or a SID according to &#39;&#39; parameter.
   **/
  public ApiResourceBase managerSubject(String managerSubject) {
    this.managerSubject = managerSubject;
    return this;
  }

  
  @ApiModelProperty(value = "Reference to a User/Group that acts as Manager. It can be an ID or a SID according to '' parameter.")
  @JsonProperty("managerSubject")
  public String getManagerSubject() {
    return managerSubject;
  }

  @JsonProperty("managerSubject")
  public void setManagerSubject(String managerSubject) {
    this.managerSubject = managerSubject;
  }

  /**
   * One or more references to User/Groups that can use the Resource. They can be IDs or SIDs according to &#39;&#39; parameter.
   **/
  public ApiResourceBase allowedSubjects(List<String> allowedSubjects) {
    this.allowedSubjects = allowedSubjects;
    return this;
  }

  
  @ApiModelProperty(value = "One or more references to User/Groups that can use the Resource. They can be IDs or SIDs according to '' parameter.")
  @JsonProperty("allowedSubjects")
  public List<String> getAllowedSubjects() {
    return allowedSubjects;
  }

  @JsonProperty("allowedSubjects")
  public void setAllowedSubjects(List<String> allowedSubjects) {
    this.allowedSubjects = allowedSubjects;
  }

  public ApiResourceBase addAllowedSubjectsItem(String allowedSubjectsItem) {
    if (this.allowedSubjects == null) {
      this.allowedSubjects = new ArrayList<>();
    }

    this.allowedSubjects.add(allowedSubjectsItem);
    return this;
  }

  public ApiResourceBase removeAllowedSubjectsItem(String allowedSubjectsItem) {
    if (allowedSubjectsItem != null && this.allowedSubjects != null) {
      this.allowedSubjects.remove(allowedSubjectsItem);
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
    ApiResourceBase resourceBase = (ApiResourceBase) o;
    return Objects.equals(this.enabled, resourceBase.enabled) &&
        Objects.equals(this.type, resourceBase.type) &&
        Objects.equals(this.displayName, resourceBase.displayName) &&
        Objects.equals(this.email, resourceBase.email) &&
        Objects.equals(this.managerSubject, resourceBase.managerSubject) &&
        Objects.equals(this.allowedSubjects, resourceBase.allowedSubjects);
  }

  @Override
  public int hashCode() {
    return Objects.hash(enabled, type, displayName, email, managerSubject, allowedSubjects);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiResourceBase {\n");
    
    sb.append("    enabled: ").append(toIndentedString(enabled)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    managerSubject: ").append(toIndentedString(managerSubject)).append("\n");
    sb.append("    allowedSubjects: ").append(toIndentedString(allowedSubjects)).append("\n");
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

