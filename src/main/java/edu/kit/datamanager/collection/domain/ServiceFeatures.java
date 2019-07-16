package edu.kit.datamanager.collection.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import lombok.Data;

/**
 * Describes the properties of the response to the Service /features request.
 */
@ApiModel(description = "Describes the properties of the response to the Service /features request.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-07-09T15:21:24.632+02:00")
@Data
public class ServiceFeatures{

  @ApiModelProperty(required = true, value = "Indicates whether this services provides collection PIDs for new collections. If this is false, requests for new Collections must supply the PID for the collection. If this is true, the Service will use its default PID provider (as advertised via the collectionPidProviderType feature) to create new PIDs to assign to new Collections.")
  @JsonProperty("providesCollectionPids")
  private Boolean providesCollectionPids = false;

  @ApiModelProperty(value = "Identifies the PID provider service used by the Collection Service to create new PIDs for new Collection. Required if providesCollectionPids is true, otherwise this property is optional and has no meaning. Recommended to use a Controlled Vocabulary or registered Data Types")
  @JsonProperty("collectionPidProviderType")
  private String collectionPidProviderType = null;

  @ApiModelProperty(required = true, value = "Indicates whether or not the service enforces access controls on requests. Implementation details access are left up to the implementor. This flag simply states whether or not the Service enforces access.")
  @JsonProperty("enforcesAccess")
  private Boolean enforcesAccess = false;

  @ApiModelProperty(required = true, value = "Indicates whether or not the service offers pagination (via cursors) of response data.")
  @JsonProperty("supportsPagination")
  private Boolean supportsPagination = false;

  @ApiModelProperty(required = true, value = "Indicates whether or not actions such as update, delete occur synchronously or may be queued for later action.")
  @JsonProperty("asynchronousActions")
  private Boolean asynchronousActions = false;

  @ApiModelProperty(required = true, value = "Indicates whether or not the service allows rule-based generation of new collections.")
  @JsonProperty("ruleBasedGeneration")
  private Boolean ruleBasedGeneration = null;

  @ApiModelProperty(required = true, value = "The maximum depth to which collection members can be expanded. A value of 0 means that expansion is not supppoted. A value of -1 means that the collections can be expanded to infinite depth.")
  @JsonProperty("maxExpansionDepth")
  private Integer maxExpansionDepth = null;

  @ApiModelProperty(required = true, value = "Indicates whether the service offers support for versioning of Collections. Implementation details are left up to the implementor.")
  @JsonProperty("providesVersioning")
  private Boolean providesVersioning = false;

  @ApiModelProperty(required = true, value = "List of collection-level set operations that are supported by this service.")
  @JsonProperty("supportedCollectionOperations")
  @Valid
  private List<Object> supportedCollectionOperations = new ArrayList<Object>();

  @ApiModelProperty(required = true, value = "List of collection model types supported by this service.  Recommended to use a Controlled Vocabulary or registered Data Types")
  @JsonProperty("supportedModelTypes")
  @Valid
  private List<Object> supportedModelTypes = new ArrayList<Object>();

  public static ServiceFeatures getDefault(){
    ServiceFeatures features = new ServiceFeatures();
    features.setProvidesCollectionPids(false);
    features.setCollectionPidProviderType(null);
    features.setMaxExpansionDepth(-1);
    features.setSupportedCollectionOperations(null);
    features.setEnforcesAccess(false);
    features.setProvidesVersioning(false);
    features.setSupportedModelTypes(null);
    features.setRuleBasedGeneration(false);
    features.setSupportsPagination(true);
    features.setAsynchronousActions(false);
    return features;
  }
}
