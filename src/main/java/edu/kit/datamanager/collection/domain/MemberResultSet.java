package edu.kit.datamanager.collection.domain;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import edu.kit.datamanager.collection.domain.MemberItem;
import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;
import lombok.Data;

/**
 * A resultset containing a potentially iterable list of Member Items. This is
 * the schema for the response to any request which retrieves collection
 * members.
 */
@ApiModel(description = "A resultset containing a potentially iterable list of Member Items. This is the schema for the response to any request which retrieves collection members.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-07-09T15:21:24.632+02:00")
@Data
public class MemberResultSet{

  @ApiModelProperty(required = true, value = "list of Member Items returned in responses to a query")
  @JsonProperty("contents")
  @Valid
  private List<MemberItem> contents = new ArrayList<MemberItem>();

  @ApiModelProperty(value = "If the service supports pagination, and the resultset is paginated, this will be cursor which can be used to retrieve the next page in the results.")
  @JsonProperty("next_cursor")
  private String nextCursor = null;

  @ApiModelProperty(value = "If the service supports pagination, and the resultset is paginated, this will be cursor which can be used to retrieve the next page in the results.")
  @JsonProperty("prev_cursor")
  private String prevCursor = null;

  @ApiModelProperty(required = true, value = "list of Member Items returned in responses to a query")

  public MemberResultSet contents(List<MemberItem> contents){
    this.contents = contents;
    return this;
  }

  public MemberResultSet addContentsItem(MemberItem contentsItem){
    this.contents.add(contentsItem);
    return this;
  }
}
