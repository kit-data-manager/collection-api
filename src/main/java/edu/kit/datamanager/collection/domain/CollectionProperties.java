package edu.kit.datamanager.collection.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import edu.kit.datamanager.util.json.CustomInstantDeserializer;
import edu.kit.datamanager.util.json.CustomInstantSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import lombok.Data;

/**
 * Functional Properties of the Collection
 */
@ApiModel(description = "Functional Properties of the Collection")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-07-09T15:21:24.632+02:00")
@Entity
@Data
public class CollectionProperties{

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ApiModelProperty(required = true, value = "The date the collection was created.")
  @JsonProperty("dateCreated")
  @JsonDeserialize(using = CustomInstantDeserializer.class)
  @JsonSerialize(using = CustomInstantSerializer.class)
  private Instant dateCreated;

  @ApiModelProperty(required = true, value = "Indicates the owner of the Collection. Implementation is expected to use a controlled vocabulary or PIDs.")
  @JsonProperty("ownership")
  private String ownership = null;

  @ApiModelProperty(required = true, value = "Indicates the license that applies to the Collection. Implementation is expected to use a controlled vocabulary, stable URIs or PIDs of registered data types. ")
  @JsonProperty("license")
  private String license = null;

  @ApiModelProperty(required = true, value = "Identifies the model that the collection adheres to. Iimplementation is expected to use a controlled vocabulary, or PIDs of registered data types. ")
  @JsonProperty("modelType")
  private String modelType = null;

  @ApiModelProperty(required = true, value = "Indicates whether the collection is fully open or has access restrictions. ")
  @JsonProperty("hasAccessRestrictions")
  private Boolean hasAccessRestrictions = false;

  @ApiModelProperty(value = "If provided, this is a list of collection identifiers to which this collection itself belongs. This property is only meaningful if the service features supports a  maximumExpansionDepth > 0.")
  @JsonProperty("memberOf")
  @Valid
  @ElementCollection(fetch = FetchType.EAGER)
  private Set<String> memberOf = new HashSet<>();

  @ApiModelProperty(required = true, value = "Identifies the ontology used for descriptive metadata. Implementation is expected to supply the URI of a controlled vocabulary.")
  @JsonProperty("descriptionOntology")
  private String descriptionOntology = null;

  public static CollectionProperties getDefault(){
    CollectionProperties result = new CollectionProperties();
    return result;
  }
}
