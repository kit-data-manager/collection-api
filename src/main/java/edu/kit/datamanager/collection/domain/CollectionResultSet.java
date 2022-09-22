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
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import org.springframework.validation.annotation.Validated;
import lombok.Data;

/**
 * A resultset containing a potentially iterable list of Collections Objects.
 * This is the schema for the response to any request which retrieves collection
 * items.
 */
@Schema(description = "A resultset containing a potentially iterable list of Collections Objects. This is the  schema for the response to any request which retrieves collection items.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-07-09T15:21:24.632+02:00")
@Data
public class CollectionResultSet{

  @Schema(required = true, description = "list of Collection Objects returned in response to a query")
  //@NotNull
  @JsonProperty("contents")
  @Valid
  private List<CollectionObject> contents = new ArrayList<CollectionObject>();

  @Schema(description = "If the service supports pagination, and the resultset is paginated, this will be a cursor which can be used to retrieve the next page in the results.")
  @JsonProperty("next_cursor")
  private String nextCursor = null;

  @Schema(description = "If the service supports pagination, and the resultset is paginated, this will be a cursor which can be used to retrieve the next page in the results.")
  @JsonProperty("prev_cursor")
  private String prevCursor = null;

  public CollectionResultSet addContentsItem(CollectionObject contentsItem){
    this.contents.add(contentsItem);
    return this;
  }
}
