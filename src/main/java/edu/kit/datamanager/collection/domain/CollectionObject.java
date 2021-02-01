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
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PreRemove;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;
import lombok.Data;

/**
 * Defines the schema for a collection object.
 */
@Entity
@Schema(description = "Defines the schema for a collection object.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-07-09T15:21:24.632+02:00")
@Data
public class CollectionObject implements EtagSupport {

    @Schema(required = true, description = "Identifier for the collection. This is ideally a PID.")
   // @NotNull
    @JsonProperty("id")
    @Id
    private String id = null;

    @Schema(required = true, description = "")
    //@NotNull
    //@Valid
    //@NotEmpty
    @OneToOne(cascade = CascadeType.ALL)
    @JsonProperty("capabilities")
    private CollectionCapabilities capabilities = null;

    @Schema(required = true, description = "")
   // @NotNull
   // @Valid
    @OneToOne(cascade = CascadeType.ALL)
    @JsonProperty("properties")
    private CollectionProperties properties = null;

    @Schema(description = "Descriptive metadata about the collection.  The properties available for this object are dependent upon the description ontology used, as define in the collection properties.")
    @JsonProperty("description")
    private String description = null;

    @JsonProperty("members")
    @OneToMany(cascade = CascadeType.ALL)
    private Set<Membership> members = new HashSet<>();

    @JsonIgnore
    public Set<Membership> getMembers() {
        return members;
    }

    @Override
    @JsonIgnore
    public String getEtag() {
        return "\"" + hashCode() + "\"";
    }

    @PreRemove
    private void preRemove() {
        members.forEach((member) -> {
            member.setMember(null);
        });
    }
}
