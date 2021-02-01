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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import edu.kit.datamanager.util.json.CustomInstantDeserializer;
import edu.kit.datamanager.util.json.CustomInstantSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import org.springframework.validation.annotation.Validated;
import lombok.Data;

/**
 * metadata on an item which is available by mapping from capabilities
 */
@Schema(description = "metadata on an item which is available by mapping from capabilities")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-07-09T15:21:24.632+02:00")
@Entity
@Data
public class CollectionItemMappingMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "The role that applies to this item. Only available if the collection supportsRoles per its capabilities. A Controlled Vocabulary should be used.")
    @JsonProperty("role")
    private String memberRole = null; //name is not 'role' in database, as 'role' is a reserved keyword according to SQL-99 standard

    @Schema(description = "position of the item in the collection. Only available if the Collection isOrdered per its capabilities.")
    @JsonProperty("index")
    private Integer index = null;

    @Schema(description = "The date the item was added to the collection.")
    @JsonProperty("dateAdded")
    @JsonDeserialize(using = CustomInstantDeserializer.class)
    @JsonSerialize(using = CustomInstantSerializer.class)
    private Instant dateAdded = Instant.now().truncatedTo( ChronoUnit.MILLIS );

    @Schema(description = "The date the item's metadata were last updated.")
    @JsonProperty("dateUpdated")
    @JsonDeserialize(using = CustomInstantDeserializer.class)
    @JsonSerialize(using = CustomInstantSerializer.class)
    private Instant dateUpdated = null;

    @JsonIgnore
    public Long getId() {
        return id;
    }

    public static CollectionItemMappingMetadata getDefault() {
        CollectionItemMappingMetadata md = new CollectionItemMappingMetadata();
        md.setIndex(0);
        return md;
    }
}
