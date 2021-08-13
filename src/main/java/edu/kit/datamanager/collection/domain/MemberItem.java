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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import org.springframework.validation.annotation.Validated;
import javax.validation.constraints.*;
import lombok.Data;

/**
 * A member item in a collection
 */
@Entity
@Schema(description = "A member item in a collection")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-07-09T15:21:24.632+02:00")
@Data
public class MemberItem implements EtagSupport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(required = true, description = "Identifier for the member")
   // @NotNull
    @JsonProperty("id")
    private String mid = null;

    @Schema(required = true, description = "Location at which the item data can be retrieved")
    @NotNull
    @JsonProperty("location")
    private String location = null;

    @Schema(description = "Human readable description")
    @JsonProperty("description")
    private String description = null;

    @Schema(description = "URI of the data type of this item")
    @JsonProperty("datatype")
    private String datatype = null;

    @Schema(description = "URI of an ontology model class that applies to this item")
    @JsonProperty("ontology")
    private String ontology = null;

    @Schema(description = "Metadata on an item which is available by mapping from capabilities")
    @JsonProperty("mappings")
    @JsonInclude(Include.NON_NULL)
    @Transient
    private CollectionItemMappingMetadata mappings = null;

    @JsonIgnore
    public Long getId() {
        return id;
    }

    public static MemberItem copy(MemberItem item) {
        return new MemberItem().copyFrom(item);
    }

    /**
     * Copy all properties of the provided MemberItem into this item.
     *
     * @param item The item to copy the properties from.
     *
     * @return This
     */
    public MemberItem copyFrom(MemberItem item) {
        this.datatype = item.datatype;
        this.description = item.description;
        this.id = item.id;
        this.mid = item.mid;
        this.location = item.location;
        this.mappings = item.mappings;
        this.ontology = item.ontology;
        if (item.getMappings() != null) {
            CollectionItemMappingMetadata md = new CollectionItemMappingMetadata();
            md.setId(item.getMappings().getId());
            md.setIndex(item.getMappings().getIndex());
            md.setMemberRole(item.getMappings().getMemberRole());
            md.setDateAdded(item.mappings.getDateAdded());
            md.setDateUpdated(item.mappings.getDateUpdated());
            this.mappings = md;
        }

        return this;
    }

    @Override
    @JsonIgnore
    public String getEtag() {
        return "\"" + hashCode() + "\"";
    }
}
