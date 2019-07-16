package edu.kit.datamanager.collection.domain;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;
import lombok.Data;

/**
 * A resultset containing a potentially iterable list of Collections Objects.
 * This is the schema for the response to any request which retrieves collection
 * items.
 */
@ApiModel(description = "A resultset containing a potentially iterable list of Collections Objects. This is the  schema for the response to any request which retrieves collection items.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-07-09T15:21:24.632+02:00")
@Data
public class CollectionResultSet{

  @ApiModelProperty(required = true, value = "list of Collection Objects returned in response to a query")
  @NotNull
  @JsonProperty("contents")
  @Valid
  private List<CollectionObject> contents = new ArrayList<CollectionObject>();

  @ApiModelProperty(value = "If the service supports pagination, and the resultset is paginated, this will be a cursor which can be used to retrieve the next page in the results.")
  @JsonProperty("next_cursor")
  private String nextCursor = null;

  @ApiModelProperty(value = "If the service supports pagination, and the resultset is paginated, this will be a cursor which can be used to retrieve the next page in the results.")
  @JsonProperty("prev_cursor")
  private String prevCursor = null;

  public CollectionResultSet contents(List<CollectionObject> contents){
    this.contents = contents;
    return this;
  }

  public CollectionResultSet addContentsItem(CollectionObject contentsItem){
    this.contents.add(contentsItem);
    return this;
  }
}
