package edu.kit.datamanager.collection.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;
import lombok.Data;

/**
 * Defines the schema for a collection object.
 */
@Entity
@ApiModel(description = "Defines the schema for a collection object.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-07-09T15:21:24.632+02:00")
@Data
public class CollectionObject implements EtagSupport{

  @ApiModelProperty(required = true, value = "Identifier for the collection. This is ideally a PID.")
  @NotNull
  @JsonProperty("id")
  @Id
  private String id = null;

  @ApiModelProperty(required = true, value = "")
  @NotNull
  @Valid
  @OneToOne(cascade = CascadeType.ALL)
  @JsonProperty("capabilities")
  private CollectionCapabilities capabilities = null;

  @ApiModelProperty(required = true, value = "")
  @NotNull
  @Valid
  @OneToOne(cascade = CascadeType.ALL)
  @JsonProperty("properties")
  private CollectionProperties properties = null;

  @ApiModelProperty(value = "Descriptive metadata about the collection.  The properties available for this object are dependent upon the description ontology used, as define in the collection properties.")
  @JsonProperty("description")
  private String description = null;

  @JsonProperty("members")
  @OneToMany(cascade = CascadeType.ALL)
  @JsonIgnore
  private Set<Membership> members = new HashSet<>();

  @Override
  @JsonIgnore
  public String getEtag(){
    return "\"" + hashCode() + "\"";
  }

}
