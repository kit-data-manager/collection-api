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
import io.swagger.v3.oas.annotations.media.Schema;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import org.springframework.validation.annotation.Validated;
import lombok.Data;

/**
 * Capabilities define the set of actions that are supported by a collection.
 */
@Schema(description = "Capabilities define the set of actions that are supported by a collection.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-07-09T15:21:24.632+02:00")
@Entity
@Data
public class CollectionCapabilities {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(required = true, description = "Identifies whether the collection items are kept in a consistent, meaningful order. The exact nature of the ordering is not specified, but see also appendsToEnd property.")
    @JsonProperty("isOrdered")
    private Boolean isOrdered = false;

    @Schema(required = true, description = "For an ordered collection, indicates that new items are appended to the end rather than insertable at a specified, possibly invalid, index points. Only valid if isOrdered is true.")
    @JsonProperty("appendsToEnd")
    private Boolean appendsToEnd = true;

    @Schema(required = true, description = "Indicates whether the collection supports assigning roles to its member items. Available roles are determined by the Collection Model type.")
    @JsonProperty("supportsRoles")
    private Boolean supportsRoles = false;

    @Schema(required = true, description = "Indicates whether collection membership mutable (i.e. whether members can be added and removed)")
    @JsonProperty("membershipIsMutable")
    private Boolean membershipIsMutable = true;

    @Schema(required = true, description = "Indicates whether collection properties are mutable (i.e. can the metadata of this collection be changed)")
    @JsonProperty("propertiesAreMutable")
    private Boolean propertiesAreMutable = true;

    @Schema(required = true, description = "If specified, indicates that the collection is made up of homogenous items of the specified type. Type should be specified using the PID of a registered Data Type or a controlled vocabulary.")
    @JsonProperty("restrictedToType")
    private String restrictedToType = null;

    @Schema(required = true, description = "The maximum length of the Collection. -1 means length is not restricted.")
    @JsonProperty("maxLength")
    private Integer maxLength = -1;

    public static CollectionCapabilities getDefault() {
        return new CollectionCapabilities();
    }
}
