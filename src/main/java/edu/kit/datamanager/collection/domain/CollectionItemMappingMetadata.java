package edu.kit.datamanager.collection.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import edu.kit.datamanager.util.json.CustomInstantDeserializer;
import edu.kit.datamanager.util.json.CustomInstantSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import org.springframework.validation.annotation.Validated;
import lombok.Data;

/**
 * metadata on an item which is available by mapping from capabilities
 */
@ApiModel(description = "metadata on an item which is available by mapping from capabilities")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-07-09T15:21:24.632+02:00")
@Entity
@Data
public class CollectionItemMappingMetadata{

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @JsonIgnore
  private Long id;

  @ApiModelProperty(value = "The ole that applies to this item. Only available if the collection supportsRoles per its capabilities. A Controlled Vocabulary should be used.")
  @JsonProperty("role")
  private String role = null;

  @ApiModelProperty(value = "position of the item in the collection. Only available if the Collection isOrdered per its capabilities.")
  @JsonProperty("index")
  private Integer index = null;

  @ApiModelProperty(value = "The date the item was added to the collection.")
  @JsonProperty("dateAdded")
  @JsonDeserialize(using = CustomInstantDeserializer.class)
  @JsonSerialize(using = CustomInstantSerializer.class)
  private Instant dateAdded = null;

  @ApiModelProperty(value = "The date the item's metadata were last updated.")
  @JsonProperty("dateUpdated")
  @JsonDeserialize(using = CustomInstantDeserializer.class)
  @JsonSerialize(using = CustomInstantSerializer.class)
  private Instant dateUpdated = null;
}
