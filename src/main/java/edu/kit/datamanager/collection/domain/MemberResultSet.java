package edu.kit.datamanager.collection.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import lombok.Data;

/**
 * A resultset containing a potentially iterable list of Member Items. This is
 * the schema for the response to any request which retrieves collection
 * members.
 */
@Schema(description = "A resultset containing a potentially iterable list of Member Items. This is the schema for the response to any request which retrieves collection members.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-07-09T15:21:24.632+02:00")
@Data
public class MemberResultSet{

  @Schema(required = true, description = "list of Member Items returned in responses to a query")
  @JsonProperty("contents")
  @Valid
  private List<MemberItem> contents = new ArrayList<MemberItem>();

  @Schema(description = "If the service supports pagination, and the resultset is paginated, this will be cursor which can be used to retrieve the next page in the results.")
  @JsonProperty("next_cursor")
  private String nextCursor = null;

  @Schema(description = "If the service supports pagination, and the resultset is paginated, this will be cursor which can be used to retrieve the next page in the results.")
  @JsonProperty("prev_cursor")
  private String prevCursor = null;

  public MemberResultSet addContentsItem(MemberItem contentsItem){
    this.contents.add(contentsItem);
    return this;
  }
}
