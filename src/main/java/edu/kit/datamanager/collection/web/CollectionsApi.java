/**
 * NOTE: This class is auto generated by the swagger code generator program (2.4.7-SNAPSHOT).
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package edu.kit.datamanager.collection.web;

import edu.kit.datamanager.collection.domain.CollectionCapabilities;
import edu.kit.datamanager.collection.domain.CollectionObject;
import edu.kit.datamanager.collection.domain.CollectionResultSet;
import edu.kit.datamanager.collection.domain.MemberItem;
import edu.kit.datamanager.collection.domain.MemberResultSet;
import edu.kit.datamanager.collection.domain.d3.DataWrapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.time.Instant;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import javax.validation.Valid;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.util.UriComponentsBuilder;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-07-09T15:21:24.632+02:00")
public interface CollectionsApi {

    @Schema(title = "Get a list of all collections provided by this service.", name = "collectionsGet",
            description = "This request returns a list of the collections provided by this service.  This may be a complete list, or if the service features include support for pagination, "
            + "the cursors in the response may be used to iterate backwards and forwards through pages of partial results. Query parameters may be used to supply filtering criteria for the response. "
            + "When combining filters of different types, the boolean AND will be used. When combining multiple instances of filters of the same type, the boolean OR will be used.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "A resultset containing a list of collection objects.", content = @Content(schema = @Schema(implementation = CollectionResultSet.class))),
        @ApiResponse(responseCode = "400", description = "Invalid Input. The query was malformed.")})
    @RequestMapping(value = "/collections",
            produces = {"application/json"},
            method = RequestMethod.GET)
    @Parameters({
        @Parameter(in = ParameterIn.QUERY,
                description = "Page you want to retrieve (0..N)",
                name = "page",
                content = @Content(schema = @Schema(type = "integer", defaultValue = "0"))),
        @Parameter(in = ParameterIn.QUERY,
                description = "Number of records per page.",
                name = "size",
                content = @Content(schema = @Schema(type = "integer", defaultValue = "20"))),
        @Parameter(in = ParameterIn.QUERY,
                description = "Sorting criteria in the format: property(,asc|desc). "
                + "Default sort order is ascending. " + "Multiple sort criteria are supported.",
                name = "sort",
                content = @Content(array = @ArraySchema(schema = @Schema(type = "string"))))
    })
    ResponseEntity<CollectionResultSet> collectionsGet(
            @Parameter(description = "Filter response by the modelType property of the collection.") @Valid @RequestParam(value = "f_modelType", required = false) String fModelType,
            @Parameter(description = "Filter response by the data type of contained collection member. A collection will meet this requirement if any of its members are of the requested type.") @Valid @RequestParam(value = "f_memberType", required = false) String fMemberType,
            @Parameter(description = "Filter response by the ownership property of the collection") @Valid @RequestParam(value = "f_ownership", required = false) String fOwnership,
            final Pageable pgbl,
            final HttpServletRequest request,
            final UriComponentsBuilder uriBuilder);

    @Schema(title = "Get a list of all collections provided by this service.", name = "collectionsGetD3", description = "This request returns a list of the collections provided by this service.  This may be a complete list, or if the service features include support for pagination, the cursors in the response may be used to iterate backwards and forwards through pages of partial results. Query parameters may be used to supply filtering criteria for the response. When combining filters of different types, the boolean AND will be used. When combining multiple instances of filters of the same type, the boolean OR will be used.", implementation = CollectionResultSet.class)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "A resultset containing a list of collection objects.", content = @Content(schema = @Schema(implementation = CollectionResultSet.class))),
        @ApiResponse(responseCode = "400", description = "Invalid Input. The query was malformed.")})
    @RequestMapping(value = "/collections",
            produces = {"application/vnd.datamanager.d3+json"},
            method = RequestMethod.GET)
    ResponseEntity<DataWrapper> collectionsGetD3(
            final HttpServletRequest request,
            final UriComponentsBuilder uriBuilder);

    @Schema(title = "Get the capabilities of this collection.", name = "collectionsIdCapabilitiesGet",
            description = "This request returns the capabilities metadata for the collection identified by the supplied id. "
            + "The collection capabilities describe the actions and operations that are available for this collection.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "The collection capabilities metadata.", content = @Content(schema = @Schema(implementation = CollectionCapabilities.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized. Request was not authorized."),
        @ApiResponse(responseCode = "404", description = "The collection identified was not found")})
    @RequestMapping(value = "/collections/{id}/capabilities",
            produces = {"application/json"},
            method = RequestMethod.GET)
    ResponseEntity<CollectionCapabilities> collectionsIdCapabilitiesGet(@Parameter(description = "Identifier for the collection", required = true) @PathVariable("id") String id);

    @Schema(title = "Delete a collection.", name = "collectionsIdDelete",
            description = "This request deletes the collection idenified by the provided id from the collection store. "
            + "The response may differ depending upon whether or not the service features include support for synchronous actions.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful deletion. Empty response body."),
        @ApiResponse(responseCode = "202", description = "Accepted deletion request. Empty response body. (For asynchronous requests if supported by service features.)"),
        @ApiResponse(responseCode = "401", description = "Unauthorized. Request was not authorized."),
        @ApiResponse(responseCode = "404", description = "The collection identified for deletion was not found")})
    @RequestMapping(value = "/collections/{id}",
            produces = {"application/json"},
            method = RequestMethod.DELETE)
    ResponseEntity<Void> collectionsIdDelete(@Parameter(description = "identifier for the collection", required = true) @PathVariable("id") String id);

    @Schema(title = "Get the properties of a specific collection.", name = "collectionsIdGet",
            description = "This request returns the Collection Object Properties for the collection identified by the provided id.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "The requested collection", content = @Content(schema = @Schema(implementation = CollectionObject.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized. Request was not authorized."),
        @ApiResponse(responseCode = "404", description = "The requested collection was not found")})
    @RequestMapping(value = "/collections/{id}",
            produces = {"application/json"},
            method = RequestMethod.GET)
    ResponseEntity<CollectionObject> collectionsIdGet(@Parameter(description = "Identifier for the collection", required = true) @PathVariable("id") String id);

    @Schema(title = "Get the members in a collection. ", name = "collectionsIdMembersGet",
            description = "This request returns the list of members contained in a collection. "
            + " This may be a complete list, or if the service features include support for pagination, "
            + "the cursors in the response may be used to iterate backwards and forwards through pages of partial results."
            + "Query parameters may be used to supply filtering criteria for the response. When combining filters of different types, "
            + "the boolean AND will be used. When combining multiple instances of filters of the same type, the boolean OR will be used.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "A resultset containing the list of member items in the identified collection.", content = @Content(schema = @Schema(implementation = MemberResultSet.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input. The filter query was malformed."),
        @ApiResponse(responseCode = "401", description = "Unauthorized. Request was not authorized."),
        @ApiResponse(responseCode = "404", description = "The collection identified was not found")})
    @RequestMapping(value = "/collections/{id}/members",
            produces = {"application/json"},
            method = RequestMethod.GET)
    @Parameters({
        @Parameter(in = ParameterIn.QUERY,
                description = "Page you want to retrieve (0..N)",
                name = "page",
                content = @Content(schema = @Schema(type = "integer", defaultValue = "0"))),
        @Parameter(in = ParameterIn.QUERY,
                description = "Number of records per page.",
                name = "size",
                content = @Content(schema = @Schema(type = "integer", defaultValue = "20"))),
        @Parameter(in = ParameterIn.QUERY,
                description = "Sorting criteria in the format: property(,asc|desc). "
                + "Default sort order is ascending. " + "Multiple sort criteria are supported.",
                name = "sort",
                content = @Content(array = @ArraySchema(schema = @Schema(type = "string"))))
    })
    ResponseEntity<MemberResultSet> collectionsIdMembersGet(
            @Parameter(description = "Identifier for the collection", required = true) @PathVariable("id") String id,
            @Parameter(description = "Filter response to members matching the requested datatype.") @Valid @RequestParam(value = "f_datatype", required = false) String fDatatype,
            @Parameter(description = "Filter response to members who are assigned the requested role. (Only if the collection capability supportsRoles is true).") @Valid @RequestParam(value = "f_role", required = false) String fRole,
            @Parameter(description = "Filter response to the members assigned the requested index. (Only if the collection capability isOrdered is true).") @Valid @RequestParam(value = "f_index", required = false) Integer fIndex,
            @Parameter(description = "Filter response to the membered added on the requestd datetime.") @Valid @RequestParam(value = "f_dateAdded", required = false) Instant fDateAdded,
            @Parameter(description = "expand members which are collections to this depth. may not exceed maxExpansionDepth feature setting for the service.") @Valid @RequestParam(value = "expandDepth", required = false) Integer expandDepth,
            final Pageable pgbl);

    @Schema(title = "Remove a collection member item.", name = "collectionsIdMembersMidDelete",
            description = "Removes a member item from a collection. The response may differ depending upon whether or not the "
            + "service features include support for asynchronous actions.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful removal. Empty response body."),
        @ApiResponse(responseCode = "202", description = "Accepted request. Empty response body. (For asynchronous requests, if supported by service features.)"),
        @ApiResponse(responseCode = "401", description = "Unauthorized. Request was not authorized."),
        @ApiResponse(responseCode = "403", description = "Forbidden. May be returned, for example, if a request was made to remove  item from a static collection."),
        @ApiResponse(responseCode = "404", description = "Not Found"),
        @ApiResponse(responseCode = "200", description = "Unexpected error")})
    @RequestMapping(value = "/collections/{id}/members/{mid}",
            produces = {"application/json"},
            method = RequestMethod.DELETE)
    ResponseEntity<Void> collectionsIdMembersMidDelete(
            @Parameter(description = "Persistent identifier for the collection", required = true) @PathVariable("id") String id,
            @Parameter(description = "Identifier for the collection member", required = true) @PathVariable("mid") String mid);
    @Schema(title = "Get the properties of a member item in a collection.", name = "collectionsIdMembersMidGet",
            description = "This request retrieves the properties of a specific member item from a collection")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "The requested member", content = @Content(schema = @Schema(implementation = MemberItem.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized. Request was not authorized."),
        @ApiResponse(responseCode = "404", description = "Not found. The requested collection or member item was not found.")})

     @RequestMapping(value ="/collections/{id}/members/{mid}", 
             produces = {"application/json"},  
             method = RequestMethod.GET)
    ResponseEntity<MemberItem> collectionsIdMembersMidGet(
            @Parameter(description = "Identifier for the collection", required = true) @PathVariable("id") String id,
            @Parameter(description = "Identifier for the collection member item.", required = true) @PathVariable("mid") String mid);

    @Schema(title = "Delete a named property of a member item in a collection.", name = "collectionsIdMembersMidPropertiesPropertyDelete",
            description = "This request deletes a specific named property of a specific member item from a collection")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful deletion. Empty response body."),
        @ApiResponse(responseCode = "202", description = "Accepted delete request. Empty response body. (For asyncrhonous requests, if supported by service features.)"),
        @ApiResponse(responseCode = "401", description = "Unauthorized. Request was not authorized."),
        @ApiResponse(responseCode = "403", description = "Forbidden. May be returned, for example, if a request was made to delete a required metadata property or update a static item."),
        @ApiResponse(responseCode = "404", description = "Not found. The requested collection or member item was not found.")})
    @RequestMapping(value = "/collections/{id}/members/{mid}/properties/{property}",
            produces = {"application/json"},
            method = RequestMethod.DELETE)
    ResponseEntity<Void> collectionsIdMembersMidPropertiesPropertyDelete(
            @Parameter(description = "Identifier for the collection", required = true) @PathVariable("id") String id,
            @Parameter(description = "Identifier for the collection member item.", required = true) @PathVariable("mid") String mid,
            @Parameter(description = "the name of a property to update", required = true) @PathVariable("property") String property);

    @Schema(title = "Get a named property of a member item in a collection.", name = "collectionsIdMembersMidPropertiesPropertyGet",
            description = "This request retrieves a specific named property of a specific member item from a collection")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "The requested member", content = @Content(schema = @Schema(implementation = MemberItem.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized. Request was not authorized."),
        @ApiResponse(responseCode = "404", description = "Not found. The requested collection or member item was not found.")})
    @RequestMapping(value = "/collections/{id}/members/{mid}/properties/{property}",
            produces = {"application/json"},
            method = RequestMethod.GET)
    ResponseEntity<String> collectionsIdMembersMidPropertiesPropertyGet(
            @Parameter(description = "Identifier for the collection", required = true) @PathVariable("id") String id,
            @Parameter(description = "Identifier for the collection member item.", required = true) @PathVariable("mid") String mid,
            @Parameter(description = "the name of a property to retrieve (e.g. index)", required = true) @PathVariable("property") String property);

    @Schema(title = "Update a named property of a member item in a collection.", name = "collectionsIdMembersMidPropertiesPropertyPut",
            description = "This request updates a specific named property of a specific member item from a collection")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful update. The updated member item is returned in the response.", content = @Content(schema = @Schema(implementation = MemberItem.class))),
        @ApiResponse(responseCode = "202", description = "Accepted update request. Empty response body. (For asynchronous requests, if supported by service features.)"),
        @ApiResponse(responseCode = "401", description = "Unauthorized. Request was not authorized."),
        @ApiResponse(responseCode = "403", description = "Forbidden. May be returned, for example, if a request was made to update a static item."),
        @ApiResponse(responseCode = "404", description = "Not found. The requested collection or member item was not found.")})
    @RequestMapping(value = "/collections/{id}/members/{mid}/properties/{property}",
            produces = {"application/json"},
            method = RequestMethod.PUT)
    ResponseEntity<String> collectionsIdMembersMidPropertiesPropertyPut(
            @Parameter(description = "Identifier for the collection", required = true) @PathVariable("id") String id,
            @Parameter(description = "Identifier for the collection member item.", required = true) @PathVariable("mid") String mid,
            @Parameter(description = "the name of a property to update", required = true) @PathVariable("property") String property,
            @Parameter(description = "new property value", required = true) @Valid @RequestBody String content);

    @Schema(title = "Update the properties of a collection member item.", name = "collectionsIdMembersMidPut",
            description = "This request updates the properties of a collection member item.  The updated CollectionItemMappingMetadata  "
            + "must be supplied in the body of the request. The response may differ  depending upon whether or not the  service "
            + "features include support  for asynchronous actions.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful update. The updated CollectionItemMappingMetadata is returned in the response.", content = @Content(schema = @Schema(implementation = MemberItem.class))),
        @ApiResponse(responseCode = "202", description = "Accepted update request. Empty response body. (For asynchronous requests if supported by service features.)"),
        @ApiResponse(responseCode = "400", description = "Invalid Input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized. Request was not authorized."),
        @ApiResponse(responseCode = "403", description = "Forbidden. May be returned, for example, if a request was made to update an item in a static collection."),
        @ApiResponse(responseCode = "404", description = "Not found. The requested collection or member item was not found.")})
    @RequestMapping(value = "/collections/{id}/members/{mid}",
            produces = {"application/json"},
            method = RequestMethod.PUT)
    ResponseEntity<MemberItem> collectionsIdMembersMidPut(
            @Parameter(description = "Identifier for the collection", required = true) @PathVariable("id") String id,
            @Parameter(description = "Identifier for the collection member", required = true) @PathVariable("mid") String mid,
            @Parameter(description = "Collection item mapping metadata", required = true) @Valid @RequestBody MemberItem content);

    @Schema(title = "Add one or more new member items to this collection.", name = "collectionsIdMembersPost",
            description = "This request adds a new member item to a collection. If the service features include support for PID assignment to "
            + "member items, then if no id is supplied for the item it  will be assigned automatically.  ", implementation = MemberItem.class)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Successful creation", content = @Content(array = @ArraySchema(schema = @Schema(type = "MemberItem")))),
        @ApiResponse(responseCode = "202", description = "Accepted add request. Empty response body. (For asyncrhonous requests,  if supported by the service features)."),
        @ApiResponse(responseCode = "400", description = "Invalid Request. Indicates that member properties were incorrect or invalid in  some way, e.g. if the collection only allows items a a specific type."),
        @ApiResponse(responseCode = "401", description = "Unauthorized. Request was not authorized."),
        @ApiResponse(responseCode = "403", description = "Forbidden. May be returned, for example, if a request was made to add an item to a static collection or if adding all elements would exceed the max. collection size."),
        @ApiResponse(responseCode = "404", description = "Not found. The collection was not found for adding items."),
        @ApiResponse(responseCode = "409", description = "Conflict. A member item with the same ID as the one posted already exists.")})
    @RequestMapping(value = "/collections/{id}/members",
            produces = {"application/json"},
            method = RequestMethod.POST)
    ResponseEntity<List<MemberItem>> collectionsIdMembersPost(
            @Parameter(description = "Identifier for the collection", required = true) @PathVariable("id") String id,
            @Parameter(description = "The properties of the member item to add to the collection. Id may be required.", required = true) @Valid @RequestBody List<MemberItem> content);

    @Schema(title = "Find member objects in a collection which match the supplied member object.", name = "collectionsIdOpsFindMatchPost",
            description = "This request accepts as input the complete or partial properties of a member object and returns a ResultSet "
            + "containing any objects which were deemed to 'match' the supplied properties among the members of the identified collection. "
            + "If the service features include support for pagination, a cursor may be supplied to iterate backwards and "
            + "forwards through paged results from prior executions of this query.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "A resulset containing the matching member items from the two collections.", content = @Content(schema = @Schema(implementation = MemberResultSet.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized. Request was not authorized."),
        @ApiResponse(responseCode = "404", description = "The collection identified was not found")})
    @RequestMapping(value = "/collections/{id}/ops/findMatch",
            produces = {"application/json"},
            method = RequestMethod.POST)
    @Parameters({
        @Parameter(in = ParameterIn.QUERY,
                description = "Page you want to retrieve (0..N)",
                name = "page",
                content = @Content(schema = @Schema(type = "integer", defaultValue = "0"))),
        @Parameter(in = ParameterIn.QUERY,
                description = "Number of records per page.",
                name = "size",
                content = @Content(schema = @Schema(type = "integer", defaultValue = "20"))),
        @Parameter(in = ParameterIn.QUERY,
                description = "Sorting criteria in the format: property(,asc|desc). "
                + "Default sort order is ascending. " + "Multiple sort criteria are supported.",
                name = "sort",
                content = @Content(array = @ArraySchema(schema = @Schema(type = "string"))))
    })
    ResponseEntity<MemberResultSet> collectionsIdOpsFindMatchPost(
            @Parameter(description = "identifier for the collection", required = true) @PathVariable("id") String id,
            @Parameter(description = "the member item properties to use when matching", required = true) @RequestBody MemberItem memberProperties,
            final Pageable pgbl);

    @Schema(title = "Flattens the collection.", name = "collectionsIdOpsFlattenGet",
            description = "This request returns a resultset which is a flattened representation of a collection of collections "
            + "into a single collection.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "A resultset containing the union of member items from the two collections", content = @Content(schema = @Schema(implementation = MemberResultSet.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized. Request was not authorized."),
        @ApiResponse(responseCode = "404", description = "One or both of the requested collections was not found.")})
    @RequestMapping(value = "/collections/{id}/ops/flatten",
            produces = {"application/json"},
            method = RequestMethod.GET)
    @Parameters({
        @Parameter(in = ParameterIn.QUERY,
                description = "Page you want to retrieve (0..N)",
                name = "page",
                content = @Content(schema = @Schema(type = "integer", defaultValue = "0"))),
        @Parameter(in = ParameterIn.QUERY,
                description = "Number of records per page.",
                name = "size",
                content = @Content(schema = @Schema(type = "integer", defaultValue = "20"))),
        @Parameter(in = ParameterIn.QUERY,
                description = "Sorting criteria in the format: property(,asc|desc). "
                + "Default sort order is ascending. " + "Multiple sort criteria are supported.",
                name = "sort",
                content = @Content(array = @ArraySchema(schema = @Schema(type = "string"))))
    })
    ResponseEntity<MemberResultSet> collectionsIdOpsFlattenGet(
            @Parameter(description = "Identifier for the collection to be flattened", required = true) @PathVariable("id") String id,
            final Pageable pgbl);

    @Schema(title = "Retrieve the members at the intersection of two collections.", name = "collectionsIdOpsIntersectionOtherIdGet",
            description = "This request returns a resultset containing the members at the intersection of two collections. "
            + "If the service features include support for pagination, a cursor may be supplied to iterate backwards "
            + "and forwards through paged results from prior executions of this query. The response may be an empty set.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "A resultset containing the intersection of member items from the two collections.", content = @Content(schema = @Schema(implementation = MemberResultSet.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized. Request was not authorized."),
        @ApiResponse(responseCode = "404", description = "One or both of the requested collections was not found.")})
    @RequestMapping(value = "/collections/{id}/ops/intersection/{otherId}",
            produces = {"application/json"},
            method = RequestMethod.GET)
    @Parameters({
        @Parameter(in = ParameterIn.QUERY,
                description = "Page you want to retrieve (0..N)",
                name = "page",
                content = @Content(schema = @Schema(type = "integer", defaultValue = "0"))),
        @Parameter(in = ParameterIn.QUERY,
                description = "Number of records per page.",
                name = "size",
                content = @Content(schema = @Schema(type = "integer", defaultValue = "20"))),
        @Parameter(in = ParameterIn.QUERY,
                description = "Sorting criteria in the format: property(,asc|desc). "
                + "Default sort order is ascending. " + "Multiple sort criteria are supported.",
                name = "sort",
                content = @Content(array = @ArraySchema(schema = @Schema(type = "string"))))
    })
    ResponseEntity<MemberResultSet> collectionsIdOpsIntersectionOtherIdGet(
            @Parameter(description = "Identifier for the first collection in the operation", required = true) @PathVariable("id") String id,
            @Parameter(description = "Identifier for the second collection in the operation", required = true) @PathVariable("otherId") String otherId,
            final Pageable pgbl);

    @Schema(title = "Retrieve the union of two collections.", name = "collectionsIdOpsUnionOtherIdGet",
            description = "This request returns a resultset containing the members at the union of two collections. If the service features "
            + "include support for pagination, a cursor may be supplied to iterate backwards and forwards through paged results from "
            + "prior executions of this query. The response may be an empty set.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "A resultset containing the union of member items from the two collections", content = @Content(schema = @Schema(implementation = MemberResultSet.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized. Request was not authorized."),
        @ApiResponse(responseCode = "404", description = "One or both of the requested collections was not found.")})
    @RequestMapping(value = "/collections/{id}/ops/union/{otherId}",
            produces = {"application/json"},
            method = RequestMethod.GET)
    @Parameters({
        @Parameter(in = ParameterIn.QUERY,
                description = "Page you want to retrieve (0..N)",
                name = "page",
                content = @Content(schema = @Schema(type = "integer", defaultValue = "0"))),
        @Parameter(in = ParameterIn.QUERY,
                description = "Number of records per page.",
                name = "size",
                content = @Content(schema = @Schema(type = "integer", defaultValue = "20"))),
        @Parameter(in = ParameterIn.QUERY,
                description = "Sorting criteria in the format: property(,asc|desc). "
                + "Default sort order is ascending. " + "Multiple sort criteria are supported.",
                name = "sort",
                content = @Content(array = @ArraySchema(schema = @Schema(type = "string"))))
    })
    ResponseEntity<MemberResultSet> collectionsIdOpsUnionOtherIdGet(
            @Parameter(description = "Identifier for the first collection in the operation", required = true) @PathVariable("id") String id,
            @Parameter(description = "Identifier for the second collection in the operation", required = true) @PathVariable("otherId") String otherId,
            final Pageable pgbl);

    @Schema(title = "Update the properties of a Collection Object.", name = "collectionsIdPut",
            description = "This request updates the properties of the collection identified by the provided id. "
            + "The updated collection properties must be supplied in the body of the request. "
            + "The response may differ depending upon whether or not the  service features include support for syncrhonous actions.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful update, returns the updated collection.", content = @Content(schema = @Schema(implementation = CollectionObject.class))),
        @ApiResponse(responseCode = "202", description = "Accepted update request. Empty response body. (For asynchronous requests if supported by service features.)"),
        @ApiResponse(responseCode = "400", description = "Invalid Input. The collection properties were malformed or invalid."),
        @ApiResponse(responseCode = "401", description = "Unauthorized. Request was not authorized."),
        @ApiResponse(responseCode = "403", description = "Forbidden. May be returned, for example, if a request was made to update a collection whose metadata is not mutable."),
        @ApiResponse(responseCode = "404", description = "The collection identified for update was not found")})
    @RequestMapping(value = "/collections/{id}",
            produces = {"application/json"},
            method = RequestMethod.PUT)
    ResponseEntity<CollectionObject> collectionsIdPut(
            @Parameter(description = "Persistent identifier for the collection", required = true) @PathVariable("id") String id,
            @Parameter(description = "The properties of the collection to be updated.", required = true) @Valid @RequestBody CollectionObject content);

    @Schema(title = "Create one or more new collections.", name = "collectionsPost",
            description = "This request adds one or more new collections to the collection store. "
            + "The Collection Objects to be created  must be supplied in the body of the request. ")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Successful creation", content = @Content(array = @ArraySchema(schema = @Schema(implementation = CollectionObject.class)))),
        @ApiResponse(responseCode = "202", description = "Accepted create request. Empty response body. (For asyncrhonous requests,  if supported by the service features)."),
        @ApiResponse(responseCode = "400", description = "Invalid Input. The collection properties were malformed or invalid."),
        @ApiResponse(responseCode = "401", description = "Unauthorized. Request was not authorized."),
        @ApiResponse(responseCode = "409", description = "Conflict. A collection with the same ID as the one posted already exists.")})
    @RequestMapping(value = "/collections",
            produces = {"application/json"},
            method = RequestMethod.POST)
    ResponseEntity<List<CollectionObject>> collectionsPost(@Parameter(description = "The properties of the collection.", required = true) @Valid @RequestBody List<CollectionObject> content);

}