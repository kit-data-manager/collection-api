/*
 * Copyright 2018 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.datamanager.collection.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.kit.datamanager.collection.configuration.ApplicationProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import lombok.Data;

/**
 * Describes the properties of the response to the Service /features request.
 */
@Schema(description = "Describes the properties of the response to the Service /features request.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-07-09T15:21:24.632+02:00")
@Data
public class ServiceFeatures{

  @Schema(required = true, description = "Indicates whether this services provides collection PIDs for new collections. If this is false, requests for new Collections must supply the PID for the collection. If this is true, the Service will use its default PID provider (as advertised via the collectionPidProviderType feature) to create new PIDs to assign to new Collections.")
  @JsonProperty("providesCollectionPids")
  private Boolean providesCollectionPids = false;

  @Schema(description = "Identifies the PID provider service used by the Collection Service to create new PIDs for new Collection. Required if providesCollectionPids is true, otherwise this property is optional and has no meaning. Recommended to use a Controlled Vocabulary or registered Data Types")
  @JsonProperty("collectionPidProviderType")
  private String collectionPidProviderType = null;

  @Schema(required = true, description = "Indicates whether or not the service enforces access controls on requests. Implementation details access are left up to the implementor. This flag simply states whether or not the Service enforces access.")
  @JsonProperty("enforcesAccess")
  private Boolean enforcesAccess = false;

  @Schema(required = true, description = "Indicates whether or not the service offers pagination (via cursors) of response data.")
  @JsonProperty("supportsPagination")
  private Boolean supportsPagination = false;

  @Schema(required = true, description = "Indicates whether or not actions such as update, delete occur synchronously or may be queued for later action.")
  @JsonProperty("asynchronousActions")
  private Boolean asynchronousActions = false;

  @Schema(required = true, description = "Indicates whether or not the service allows rule-based generation of new collections.")
  @JsonProperty("ruleBasedGeneration")
  private Boolean ruleBasedGeneration = null;

  @Schema(required = true, description = "The maximum depth to which collection members can be expanded. A value of 0 means that expansion is not supppoted. A value of -1 means that the collections can be expanded to infinite depth.")
  @JsonProperty("maxExpansionDepth")
  private Integer maxExpansionDepth = null;

  @Schema(required = true, description = "Indicates whether the service offers support for versioning of Collections. Implementation details are left up to the implementor.")
  @JsonProperty("providesVersioning")
  private Boolean providesVersioning = false;

  @Schema(required = true, description = "List of collection-level set operations that are supported by this service.")
  @JsonProperty("supportedCollectionOperations")
  @Valid
  private List<Object> supportedCollectionOperations = new ArrayList<Object>();

  @Schema(required = true, description = "List of collection model types supported by this service.  Recommended to use a Controlled Vocabulary or registered Data Types")
  @JsonProperty("supportedModelTypes")
  @Valid
  private List<Object> supportedModelTypes = new ArrayList<Object>();

  public static ServiceFeatures getDefault(){
    ServiceFeatures features = new ServiceFeatures();
    features.setProvidesCollectionPids(false);
    features.setCollectionPidProviderType(null);
    features.setMaxExpansionDepth(ApplicationProperties.maxExpansionDepth);
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
