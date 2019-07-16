package edu.kit.datamanager.collection.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import org.springframework.validation.annotation.Validated;
import javax.validation.constraints.*;
import lombok.Data;

/**
 * A member item in a collection
 */
@Entity
@ApiModel(description = "A member item in a collection")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-07-09T15:21:24.632+02:00")
@Data
public class MemberItem{

  @ApiModelProperty(required = true, value = "Identifier for the member")
  @NotNull
  @JsonProperty("id")
  @Id
  private String id = null;

  @ApiModelProperty(required = true, value = "Location at which the item data can be retrieved")
  @NotNull
  @JsonProperty("location")
  private String location = null;

  @ApiModelProperty(value = "Human readable description")
  @JsonProperty("description")
  private String description = null;

  @ApiModelProperty(value = "URI of the data type of this item")
  @JsonProperty("datatype")
  private String datatype = null;

  @ApiModelProperty(value = "URI of an ontology model class that applies to this item")
  @JsonProperty("ontology")
  private String ontology = null;

  @ApiModelProperty(value = "Metadata on an item which is available by mapping from capabilities")
  @JsonProperty("mappings")
  @Transient
  private CollectionItemMappingMetadata mappings = null;

  public static MemberItem copy(MemberItem item){
    MemberItem i = new MemberItem();
    i.datatype = item.datatype;
    i.description = item.description;
    i.id = item.id;
    i.location = item.location;
    i.mappings = item.mappings;
    i.ontology = item.ontology;
    return i;
  }
}
