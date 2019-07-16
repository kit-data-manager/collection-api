package edu.kit.datamanager.collection.web.impl;

import edu.kit.datamanager.collection.domain.CollectionCapabilities;
import edu.kit.datamanager.collection.domain.CollectionObject;
import edu.kit.datamanager.collection.domain.CollectionResultSet;
import edu.kit.datamanager.collection.domain.MemberItem;
import edu.kit.datamanager.collection.domain.MemberResultSet;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.datamanager.collection.dao.ICollectionObjectDao;
import edu.kit.datamanager.collection.dao.IMemberItemDao;
import edu.kit.datamanager.collection.web.CollectionsApi;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import javax.validation.Valid;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import edu.kit.datamanager.collection.dao.IMembershipDao;
import edu.kit.datamanager.collection.dao.spec.MembershipQueryHelper;
import edu.kit.datamanager.collection.domain.CollectionItemMappingMetadata;
import edu.kit.datamanager.collection.domain.CollectionProperties;
import edu.kit.datamanager.collection.domain.Membership;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-07-09T15:21:24.632+02:00")
@Controller
public class CollectionsApiController implements CollectionsApi{

  private static final Logger log = LoggerFactory.getLogger(CollectionsApiController.class);

  private final ObjectMapper objectMapper;

  private final HttpServletRequest request;

  @Autowired
  private ICollectionObjectDao collectionDao;
  @Autowired
  private IMemberItemDao memberDao;
  @Autowired
  private IMembershipDao membershipDao;

  @PersistenceContext
  private EntityManager em;

  @org.springframework.beans.factory.annotation.Autowired
  public CollectionsApiController(ObjectMapper objectMapper, HttpServletRequest request){
    this.objectMapper = objectMapper;
    this.request = request;
  }

  private void buildData(){
//    membershipDao.deleteAll();
//    collectionDao.deleteAll();
//    memberDao.deleteAll();

    CollectionObject collection = new CollectionObject();
    collection.setId("3");
    collection.setDescription("This is collection 3");
    CollectionProperties props = new CollectionProperties();
    props.setDateCreated(Instant.now());
    props.setOwnership("me");
    props.setLicense("Apache 2.0");
    props.setModelType("anotherType");
    props.setDescriptionOntology("custom");
    collection.setProperties(props);
    CollectionCapabilities caps = new CollectionCapabilities();
    collection.setCapabilities(caps);
    collection = collectionDao.save(collection);
//
//    CollectionObject collection2 = new CollectionObject();
//    collection2.setId("2");
//    collection2.setDescription("This is collection 1");
//    CollectionProperties props2 = new CollectionProperties();
//    props2.setDateCreated(Instant.now());
//    props2.setOwnership("Thomas");
//    props2.setLicense("Apache 2.0");
//    props2.setModelType("myType");
//    props2.setDescriptionOntology("custom");
//    collection2.setProperties(props2);
//    CollectionCapabilities caps2 = new CollectionCapabilities();
//    collection2.setCapabilities(caps2);
//    collection2 = collectionDao.save(collection2);
//
//    for(int i = 1; i < 6; i++){
//      MemberItem item = new MemberItem();
//      item.setId(Integer.toString(i));
//      item.setLocation("google.com");
//      if(i != 3 && i != 5){
//        item.setDatatype("type1");
//      } else{
//        item.setDatatype("type2");
//      }
//      item.setDescription("Item description");
//      item = memberDao.save(item);
//
//      Membership m = new Membership();
//      m.setCollection(collection);
//      m.setMember(item);
//      CollectionItemMappingMetadata md = new CollectionItemMappingMetadata();
//      md.setRole("USER");
//      m.setMappings(md);
//      membershipDao.save(m);
//
//      if(i != 3 && i != 5){
//        Membership m1 = new Membership();
//        m1.setCollection(collection2);
//        m1.setMember(item);
//        CollectionItemMappingMetadata md1 = new CollectionItemMappingMetadata();
//        md1.setRole("USER");
//        m1.setMappings(md1);
//        membershipDao.save(m1);
//      }
//
//    }

  }

  public ResponseEntity<CollectionResultSet> collectionsGet(
          @ApiParam(value = "Filter response by the modelType property of the collection.") @Valid @RequestParam(value = "f_modelType", required = false) String fModelType,
          @ApiParam(value = "Filter response by the data type of contained collection member. A collection will meet this requirement if any of its members are of the requested type.") @Valid @RequestParam(value = "f_memberType", required = false) String fMemberType,
          @ApiParam(value = "Filter response by the ownership property of the collection") @Valid @RequestParam(value = "f_ownership", required = false) String fOwnership,
          final Pageable pgbl,
          final HttpServletRequest request,
          final UriComponentsBuilder uriBuilder
  ){

    //buildData();
    //check model type and ownership
    //if not null, query for collections
    //take result, extract ids and put in membership query
    //if both null, query for members of type
    //take result, extract ids and put in membership query
    //extract collection id from members page
    log.trace("Calling collectionsGet({}, {}, {}, {}).", fModelType, fMemberType, fOwnership, pgbl);
    Set<CollectionObject> resultList = new HashSet<>();
    CollectionResultSet resultSet = new CollectionResultSet();

    int pageSize = (pgbl != null) ? pgbl.getPageSize() : 20;

    int totalPages = 0;
    if(fMemberType == null){
      log.trace("fModelType and/or fOwnership are not null, fMemberType is null, filtering only collections.");
      //query for collections by model/ownership[
      CollectionObject sample = new CollectionObject();

      CollectionProperties props = new CollectionProperties();
      props.setHasAccessRestrictions(null);
      log.trace("Building collection template for model type {} and ownership{}.", fModelType, fOwnership);
      props.setModelType(fModelType);
      props.setOwnership(fOwnership);
      sample.setProperties(props);

      Example<CollectionObject> example = Example.of(sample, ExampleMatcher.matchingAny());

      Page<CollectionObject> collections = collectionDao.findAll(example, pgbl);
      log.trace("Transferring membership collections to result list.");
      collectionDao.findAll(example, pgbl).forEach((o) -> {
        resultList.add(o);
      });
      log.trace("Found {} collections.", resultList.size());

      totalPages = collections.getTotalPages();
      log.trace("Obtained number of total pages with value {}.", totalPages);

    } else if(fMemberType != null && fModelType == null && fOwnership == null){
      log.trace("fModelType and/or fOwnership are null, fMemberType is not null, filtering only by member type.");
      MemberItem mem = new MemberItem();
      log.trace("Building memberItem template for member type {}.", fMemberType);
      mem.setDatatype(fMemberType);
      Example<MemberItem> example = Example.of(mem);
      List<String> memberIds = new ArrayList<>();
      memberDao.findAll(example).forEach((o) -> {
        memberIds.add(o.getId());
      });

      log.trace("Found {} memberItem id(s). Obtaining memberships for member items.", memberIds.size());

      Page<Membership> memberships = membershipDao.findByMemberIdIn(memberIds, pgbl);
      totalPages = memberships.getTotalPages();
      log.trace("Obtained number of total pages with value {}.", totalPages);

      log.trace("Transferring membership collections to result list.");
      memberships.forEach((o) -> {
        resultList.add(o.getCollection());
      });
    } else if(fMemberType != null && fModelType != null && fOwnership != null){
      log.trace("fModelType, fOwnership and fMemberType are not null, filtering by collection and by member type.");
      CollectionObject collectionTemplate = new CollectionObject();

      CollectionProperties propsTemplate = new CollectionProperties();
      propsTemplate.setHasAccessRestrictions(null);
      log.trace("Building collection template for model type {} and ownership {}.", fModelType, fOwnership);
      propsTemplate.setModelType(fModelType);
      propsTemplate.setOwnership(fOwnership);
      collectionTemplate.setProperties(propsTemplate);

      Example<CollectionObject> collectionExample = Example.of(collectionTemplate, ExampleMatcher.matchingAny());
      log.trace("Performing query by example using collection template {}.", collectionTemplate);
      List<String> collectionIds = new ArrayList<>();
      collectionDao.findAll(collectionExample).forEach((o) -> {
        collectionIds.add(o.getId());
      });

      if(collectionIds.isEmpty()){
        log.debug("No collection ids found. Returning empty result set.");
        return new ResponseEntity<>(resultSet, HttpStatus.OK);
      } else{
        log.trace("Found {} collection id(s).", collectionIds.size());
      }

      MemberItem memberTemplate = new MemberItem();
      log.trace("Building memberItem template for member type {}.", fMemberType);
      memberTemplate.setDatatype(fMemberType);
      Example<MemberItem> memberExample = Example.of(memberTemplate);
      List<String> memberIds = new ArrayList<>();
      memberDao.findAll(memberExample).forEach((o) -> {
        memberIds.add(o.getId());
      });

      if(memberIds.isEmpty()){
        log.debug("No memberItem ids found. Returning empty result set.");
        return new ResponseEntity<>(resultSet, HttpStatus.OK);
      } else{
        log.trace("Found {} memberItem id(s).", collectionIds.size());
      }

      int offset = (pgbl != null) ? pgbl.getPageNumber() * pgbl.getPageSize() : 0;
      log.trace("Using query offset {}.", offset);

      MembershipQueryHelper qh = new MembershipQueryHelper(em);
      log.trace("Querying for max. {} collection ids from index {}.", pageSize, offset);
      List<String> list = qh.getCollectionsIdsByFilter(memberIds, collectionIds, offset, pageSize);
      log.trace("Querying for collection id count.");
      long totalElementCount = qh.getCollectionsIdsCountByFilter(memberIds, collectionIds);
      log.trace("Obtained collection id count of {}.", totalElementCount);

      collectionDao.findAllById(list).forEach((o) -> {
        resultList.add(o);
      });

      log.trace("Obtaining number of total pages.");
      totalPages = (totalElementCount > 0) ? (int) Math.rint(totalElementCount / totalElementCount) + ((totalElementCount % totalElementCount != 0) ? 1 : 0) : 0;
      log.trace("Total pages are: {}", totalPages);
    }

    log.trace("Building cursor links.");
    int nextPage = (pgbl != null) ? pgbl.getPageNumber() + 1 : 0;
    log.trace("Next page has number {}.", nextPage);
    String nextPageLink = (nextPage >= totalPages) ? null : ServletUriComponentsBuilder.fromCurrentRequest().replaceQueryParam("page", nextPage).replaceQueryParam("size", pageSize).build().toString();
    log.trace("Next page link is: {}", nextPage);
    int prevPage = (pgbl != null) ? pgbl.getPageNumber() - 1 : -1;
    log.trace("Next page has number {}.", prevPage);
    String prevPageLink = (prevPage < 0) ? null : ServletUriComponentsBuilder.fromCurrentRequest().replaceQueryParam("page", prevPage).replaceQueryParam("size", pageSize).build().toString();
    log.trace("Prev page link is: {}", nextPage);

    log.trace("Filling result set with {} results from result list.", resultList.size());
    resultList.forEach((o) -> {
      resultSet.addContentsItem(o);
    });

    log.trace("Setting cursor values.");
    resultSet.setNextCursor(nextPageLink);
    resultSet.setPrevCursor(prevPageLink);
    log.trace("Returning result set.");
    return new ResponseEntity<>(resultSet, HttpStatus.OK);
  }

  public ResponseEntity<CollectionCapabilities> collectionsIdCapabilitiesGet(@ApiParam(value = "Identifier for the collection", required = true) @PathVariable("id") String id){
    log.trace("Calling collectionsIdCapabilitiesGet({}).", id);

    Optional<CollectionObject> result = collectionDao.findById(id);

    if(result.isEmpty()){
      log.debug("No collection with id {} found.", id);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    log.trace("Returning collection capabilities.");
    return new ResponseEntity<>(result.get().getCapabilities(), HttpStatus.OK);
  }

  public ResponseEntity<Void> collectionsIdDelete(@ApiParam(value = "identifier for the collection", required = true) @PathVariable("id") String id){
    log.trace("Calling collectionsIdDelete({}).", id);
    Optional<CollectionObject> result = collectionDao.findById(id);

    if(result.isEmpty()){
      log.debug("No collection with id {} found.", id);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    //@TODO Delete membership and members? mark as deleted?
    log.trace("Deleting collection with id {}.", id);
    collectionDao.delete(result.get());

    log.trace("Returning HTTP 200.");
    return new ResponseEntity<>(HttpStatus.OK);
  }

  public ResponseEntity<CollectionObject> collectionsIdGet(@ApiParam(value = "Identifier for the collection", required = true) @PathVariable("id") String id){
    log.trace("Calling collectionsIdGet({}).", id);

    Optional<CollectionObject> result = collectionDao.findById(id);

    if(result.isEmpty()){
      log.debug("No collection with id {} found.", id);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    log.trace("Returning collection capabilities.");
    return new ResponseEntity<>(result.get(), HttpStatus.OK);
  }

  public ResponseEntity<MemberResultSet> collectionsIdMembersGet(
          @ApiParam(value = "Identifier for the collection", required = true) @PathVariable("id") String id,
          @ApiParam(value = "Filter response to members matching the requested datatype.") @Valid @RequestParam(value = "f_datatype", required = false) String fDatatype,
          @ApiParam(value = "Filter response to members who are assigned the requested role. (Only if the collection capability supportsRoles is true).") @Valid @RequestParam(value = "f_role", required = false) String fRole,
          @ApiParam(value = "Filter response to the members assigned the requested index. (Only if the collection capability isOrdered is true).") @Valid @RequestParam(value = "f_index", required = false) Integer fIndex,
          @ApiParam(value = "Filter response to the members added on the requestd datetime.") @Valid @RequestParam(value = "f_dateAdded", required = false) Instant fDateAdded,
          @ApiParam(value = "Expand members which are collections to this depth. may not exceed maxExpansionDepth feature setting for the service.") @Valid @RequestParam(value = "expandDepth", required = false) Integer expandDepth,
          final Pageable pgbl){
    //get COllection
    //get Member filtered by datatype
    //get membership filtered by mappings.role, mappings.index, mappings.dateadded
    //if member is collection, expand until depth
    Optional<CollectionObject> result = collectionDao.findById(id);

    if(result.isEmpty()){
      log.debug("No collection with id {} found.", id);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    log.trace("Resolving collection ids up to expand depth {}.", expandDepth);
    Set<String> collectionIds = new HashSet<>();
    collectionIds.add(id);
    if(expandDepth != null && expandDepth > 0){
      MembershipQueryHelper helper = new MembershipQueryHelper(em);
      for(CollectionObject o : helper.getMemberCollections(id, expandDepth)){
        collectionIds.add(o.getId());
      }
    }

    log.trace("Obtained {} collection id(s) in total.", collectionIds.size());

    List<String> memberIds = new ArrayList<>();
    if(fDatatype != null){
      log.trace("Filtering member ids by datatype.");
      MemberItem mem = new MemberItem();
      log.trace("Building memberItem template for member type {}.", fDatatype);
      mem.setDatatype(fDatatype);
      Example<MemberItem> example = Example.of(mem);
      memberDao.findAll(example).forEach((o) -> {
        memberIds.add(o.getId());
      });
    } else{
      log.trace("Skip filtering member ids by datatype.");
    }

    int offset = (pgbl != null) ? pgbl.getPageNumber() * pgbl.getPageSize() : 0;

    MembershipQueryHelper qh = new MembershipQueryHelper(em);

//    log.trace("Querying for member ids with role {}, index {} and dateAdded {} in {} collection(s).", fRole, fIndex, fDatatype, collectionIds.size());
//    List<String> filteredMemberIds = qh.getMemberIdsByFilter(memberIds, new ArrayList<>(collectionIds), fRole, fIndex, fDateAdded, offset, pgbl.getPageSize());
//    log.trace("Found {} member id(s). Obtaining member items.", filteredMemberIds.size());
//    List<MemberItem> members = memberDao.findAllById(filteredMemberIds);
    List<Membership> memberships = qh.getMemberIdsByFilter(memberIds, new ArrayList<>(collectionIds), fRole, fIndex, fDateAdded, offset, pgbl.getPageSize());

    List<MemberItem> members = new ArrayList<>();
    for(Membership membership : memberships){
      //we have to copy the item in case an item is in multiple collections, in that case the same item  pointer is returned multiple times pointing to the same memory location
      MemberItem item = MemberItem.copy(membership.getMember());
      item.setMappings(membership.getMappings());
      members.add(item);
    }

    log.trace("Obtaining total element count.");
    long totalElementCount = qh.getMemberIdsCountByFilter(memberIds, new ArrayList<>(collectionIds), fRole, fIndex, fDateAdded, offset, pgbl.getPageSize());
    long totalPages = (totalElementCount > 0) ? (int) Math.rint(totalElementCount / totalElementCount) + ((totalElementCount % totalElementCount != 0) ? 1 : 0) : 0;
    log.trace("Total element count is {}. This represents {} page(s) in total.", totalElementCount, totalPages);

    long pageSize = (pgbl != null) ? pgbl.getPageSize() : 20;
    log.trace("Building cursor links.");
    int nextPage = (pgbl != null) ? pgbl.getPageNumber() + 1 : 0;
    log.trace("Next page has number {}.", nextPage);
    String nextPageLink = (nextPage >= totalPages) ? null : ServletUriComponentsBuilder.fromCurrentRequest().replaceQueryParam("page", nextPage).replaceQueryParam("size", pageSize).build().toString();
    log.trace("Next page link is: {}", nextPage);
    int prevPage = (pgbl != null) ? pgbl.getPageNumber() - 1 : -1;
    log.trace("Next page has number {}.", prevPage);
    String prevPageLink = (prevPage < 0) ? null : ServletUriComponentsBuilder.fromCurrentRequest().replaceQueryParam("page", prevPage).replaceQueryParam("size", pageSize).build().toString();
    log.trace("Prev page link is: {}", nextPage);

    log.trace("Filling result set with {} results from result list.", members.size());
    MemberResultSet resultSet = new MemberResultSet();
    members.forEach((o) -> {
      resultSet.addContentsItem(o);
    });

    log.trace("Setting cursor values.");
    resultSet.setNextCursor(nextPageLink);
    resultSet.setPrevCursor(prevPageLink);
    log.trace("Returning result set.");
    return new ResponseEntity<>(resultSet, HttpStatus.OK);
  }

  public ResponseEntity<Void> collectionsIdMembersMidDelete(
          @ApiParam(value = "Persistent identifier for the collection", required = true) @PathVariable("id") String id,
          @ApiParam(value = "Identifier for the collection member", required = true) @PathVariable("mid") String mid){
    log.trace("Calling collectionsIdMembersMidDelete({}, {}).", id, mid);

    Optional<Membership> membership = membershipDao.findByCollectionIdEqualsAndMemberIdEquals(id, mid);

    if(membership.isEmpty()){
      log.debug("No membership for collection with id {} and member with id {} found.", id, mid);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    //@TODO Delete member with membership
    log.trace("Deleting membership for collection {} and member {}.", id, mid);
    membershipDao.delete(membership.get());
    log.trace("Returning HTTP 200.");
    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  public ResponseEntity<MemberItem> collectionsIdMembersMidGet(
          @ApiParam(value = "Identifier for the collection", required = true) @PathVariable("id") String id,
          @ApiParam(value = "Identifier for the collection member item.", required = true) @PathVariable("mid") String mid){
    log.trace("Calling collectionsIdMembersMidGet({}, {}).", id, mid);

    Optional<Membership> membership = membershipDao.findByCollectionIdEqualsAndMemberIdEquals(id, mid);

    if(membership.isEmpty()){
      log.debug("No membership for collection with id {} and member with id {} found.", id, mid);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    log.trace("Returning member with id {}.", mid);
    return new ResponseEntity<>(membership.get().getMember(), HttpStatus.OK);
  }

  public ResponseEntity<Void> collectionsIdMembersMidPropertiesPropertyDelete(
          @ApiParam(value = "Identifier for the collection", required = true) @PathVariable("id") String id,
          @ApiParam(value = "Identifier for the collection member item.", required = true) @PathVariable("mid") String mid,
          @ApiParam(value = "The name of a property to delete", required = true) @PathVariable("property") String property){

    log.trace("Calling collectionsIdMembersMidPropertiesPropertyDelete({}, {}. {}).", id, mid, property);

    Optional<Membership> membership = membershipDao.findByCollectionIdEqualsAndMemberIdEquals(id, mid);

    if(membership.isEmpty()){
      log.debug("No membership for collection with id {} and member with id {} found.", id, mid);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    Membership membershipItem = membership.get();
    CollectionItemMappingMetadata mappingMetadata = membershipItem.getMappings();

    switch(property){
      case "role":
        log.trace("Deleting property 'role'.");
        mappingMetadata.setRole(null);
        break;
      case "index":
        log.trace("Deleting property 'index'.");
        mappingMetadata.setIndex(null);
        break;
      case "dateAdded":
        log.trace("Deleting property 'dateAdded'.");
        mappingMetadata.setDateAdded(null);
        break;
      case "dateUpdated":
        log.trace("Deleting property 'dateUpdated'.");
        mappingMetadata.setDateUpdated(null);
        break;
      default:
        log.warn("Property {} not found. Supported properties are 'index', 'role', 'dateAdded' and 'dateUpdated'.", property);
        return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
    }

    log.trace("Persisting updated membership.");
    membershipDao.save(membershipItem);
    log.trace("Returning HTTP 200.");
    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  public ResponseEntity<String> collectionsIdMembersMidPropertiesPropertyGet(
          @ApiParam(value = "Identifier for the collection", required = true) @PathVariable("id") String id,
          @ApiParam(value = "Identifier for the collection member item.", required = true) @PathVariable("mid") String mid,
          @ApiParam(value = "the name of a property to retrieve (e.g. index)", required = true) @PathVariable("property") String property){
    log.trace("Calling collectionsIdMembersMidPropertiesPropertyGet({}, {}. {}).", id, mid, property);

    Optional<Membership> membership = membershipDao.findByCollectionIdEqualsAndMemberIdEquals(id, mid);

    if(membership.isEmpty()){
      log.debug("No membership for collection with id {} and member with id {} found.", id, mid);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    Membership membershipItem = membership.get();
    CollectionItemMappingMetadata mappingMetadata = membershipItem.getMappings();

    String value;
    switch(property){
      case "role":
        log.trace("Getting property 'role'.");
        value = mappingMetadata.getRole();
        break;
      case "index":
        log.trace("Getting property 'index'.");
        if(mappingMetadata.getIndex() != null){
          value = Integer.toString(mappingMetadata.getIndex());
        } else{
          value = null;
        }
        break;
      case "dateAdded":
        log.trace("Getting property 'dateAdded'.");
        if(mappingMetadata.getDateAdded() != null){
          value = DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC).format(mappingMetadata.getDateAdded());
        } else{
          value = null;
        }
        break;
      case "dateUpdated":
        log.trace("Getting property 'dateUpdated'.");
        if(mappingMetadata.getDateUpdated() != null){
          value = DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC).format(mappingMetadata.getDateUpdated());
        } else{
          value = null;
        }
        break;
      default:
        log.warn("Property {} not found. Supported properties are 'index', 'role', 'dateAdded' and 'dateUpdated'.", property);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    return new ResponseEntity<>(value, HttpStatus.OK);
  }

  public ResponseEntity<String> collectionsIdMembersMidPropertiesPropertyPut(
          @ApiParam(value = "Identifier for the collection", required = true) @PathVariable("id") String id,
          @ApiParam(value = "Identifier for the collection member item.", required = true) @PathVariable("mid") String mid,
          @ApiParam(value = "The name of a property to update", required = true) @PathVariable("property") String property,
          @ApiParam(value = "New property value", required = true) @Valid @RequestBody String content){

    log.trace("Calling collectionsIdMembersMidPropertiesPropertyPut({}, {}. {}, {}).", id, mid, property, content);

    Optional<Membership> membership = membershipDao.findByCollectionIdEqualsAndMemberIdEquals(id, mid);

    if(membership.isEmpty()){
      log.debug("No membership for collection with id {} and member with id {} found.", id, mid);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    Membership membershipItem = membership.get();
    CollectionItemMappingMetadata mappingMetadata = membershipItem.getMappings();

    switch(property){
      case "role":
        log.trace("Setting property 'role'.");
        mappingMetadata.setRole(content);
        break;
      case "index":
        log.trace("Setting property 'index'.");
        try{
          mappingMetadata.setIndex(Integer.parseInt(content));
        } catch(NumberFormatException ex){
          log.error("Invalid value '{}' provided for property '{}'.", content, property);
          return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        break;
      case "dateAdded":
        log.trace("Setting property 'dateAdded'.");
        try{
          mappingMetadata.setDateAdded(DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC).parse(content, Instant::from));
        } catch(DateTimeParseException ex){
          log.error("Invalid value '{}' provided for property '{}'.", content, property);
          return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        break;
      case "dateUpdated":
        log.trace("Setting property 'dateUpdated'.");
        try{
          mappingMetadata.setDateUpdated(DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC).parse(content, Instant::from));
        } catch(DateTimeParseException ex){
          log.error("Invalid value '{}' provided for property '{}'.", content, property);
          return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        break;
      default:
        log.warn("Property {} not found. Supported properties are 'index', 'role', 'dateAdded' and 'dateUpdated'.", property);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    log.trace("Persisting updated membership.");
    membershipDao.save(membershipItem);
    log.trace("Returning HTTP 200.");
    return new ResponseEntity<>(content, HttpStatus.OK);
  }

  public ResponseEntity<MemberItem> collectionsIdMembersMidPut(
          @ApiParam(value = "Identifier for the collection", required = true) @PathVariable("id") String id,
          @ApiParam(value = "Identifier for the collection member", required = true) @PathVariable("mid") String mid,
          @ApiParam(value = "Member item metadata", required = true) @Valid @RequestBody MemberItem content){
    log.trace("Calling collectionsIdMembersMidPut({}, {}, {}).", id, mid, content);

    Optional<Membership> membership = membershipDao.findByCollectionIdEqualsAndMemberIdEquals(id, mid);

    if(membership.isEmpty()){
      log.debug("No membership between collection {} and member item {} found.", id, mid);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    Membership m = membership.get();

    MemberItem existingMember = m.getMember();

    log.trace("Transferring member item properties from provided member item.");

    log.debug("Updating member item property 'dataType' from value {} to value {}.", existingMember.getDatatype(), content.getDatatype());
    existingMember.setDatatype(content.getDatatype());
    log.debug("Updating member item property 'description' from value {} to value {}.", existingMember.getDescription(), content.getDescription());
    existingMember.setDescription(content.getDescription());
    log.debug("Updating member item property 'location' from value {} to value {}.", existingMember.getLocation(), content.getLocation());
    existingMember.setLocation(content.getLocation());
    log.debug("Updating member item property 'ontology' from value {} to value {}.", existingMember.getOntology(), content.getOntology());
    existingMember.setOntology(content.getOntology());

    CollectionItemMappingMetadata mMetadata = m.getMappings();
    CollectionItemMappingMetadata itemMetadata = content.getMappings();
    log.trace("Transferring collection item mapping metadata from provided member item.");
    if(itemMetadata != null){
      log.trace("Transferring property 'dateAdded'.");
      mMetadata.setDateAdded(itemMetadata.getDateAdded());

      log.trace("Transferring property 'index'.");
      mMetadata.setIndex(itemMetadata.getIndex());

      log.trace("Transferring property 'role'.");
      mMetadata.setRole(itemMetadata.getRole());

      log.trace("Transferring property 'dateUpdated'.");
      mMetadata.setDateUpdated(itemMetadata.getDateUpdated());
    }
    log.trace("Persisting updated membership with new collection item metadata.");
    m = membershipDao.save(m);

    MemberItem member = m.getMember();
    member.setMappings(mMetadata);;

    log.trace("Returning collection member item.");
    return new ResponseEntity<>(member, HttpStatus.OK);
  }

  public ResponseEntity<List<MemberItem>> collectionsIdMembersPost(
          @ApiParam(value = "Identifier for the collection", required = true) @PathVariable("id") String id,
          @ApiParam(value = "The properties of the member item to add to the collection. Id may be required.", required = true) @Valid @RequestBody List<MemberItem> content){
    log.trace("Calling collectionsIdMembersPost({}).", id);

    Optional<CollectionObject> result = collectionDao.findById(id);

    if(result.isEmpty()){
      log.debug("No collection with id {} found.", id);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    String restrictedToType = null;
    if(result.get().getCapabilities() != null){
      if(!result.get().getCapabilities().getMembershipIsMutable()){
        log.error("Unable to add members to immutable collection with id {}.", id);
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
      }

      if(result.get().getCapabilities().getMaxLength() != null && result.get().getCapabilities().getMaxLength() > -1){
        log.trace("Checking collection maxLength property.");
        Long membershipCount = membershipDao.countByCollectionId(id);
        if(membershipCount + content.size() >= result.get().getCapabilities().getMaxLength()){
          log.error("Adding {} members to collection would exceed max count {}.", content.size(), result.get().getCapabilities().getMaxLength());
          return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
      }
      restrictedToType = result.get().getCapabilities().getRestrictedToType();
    }

    log.trace("Checking {} member items for proper type, assigned ids and membership conflicts.");
    for(MemberItem item : content){
      if(restrictedToType != null && !restrictedToType.equals(item.getDatatype())){
        log.error("Member has invalid type. Collection with id {} only supports type {}, but member provided type {}.", restrictedToType, item.getDatatype());
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
      }

      if(item.getId() == null){
        log.trace("Member item has no id. Setting random UUID.");
        item.setId(UUID.randomUUID().toString());
        log.trace("UUID {} assigned as member item id.", item.getId());
      }

      log.trace("Checking for existing membership between collection {} and member item id {}.", id, item.getId());
      Optional<Membership> existing = membershipDao.findByCollectionIdEqualsAndMemberIdEquals(id, item.getId());
      if(existing.isPresent()){
        log.error("Existing membership found. Returning HTTP CONFLICT.");
        return new ResponseEntity<>(HttpStatus.CONFLICT);
      }
    }

    log.trace("All member items are fine. Adding {} member(s) to collection {}.", content.size(), id);
    for(MemberItem item : content){
      Membership m = new Membership();
      m.setCollection(result.get());
      log.trace("Obtaining collection item mapping metadata.");
      CollectionItemMappingMetadata md = item.getMappings();
      if(md == null){
        log.trace("No collection item metadata provided. Creating new metadata instance.");
        md = new CollectionItemMappingMetadata();
        item.setMappings(md);
      }
      log.trace("Setting metadata property 'dateAdded' to now().");
      md.setDateAdded(Instant.now());
      m.setMember(item);
      m.setMappings(md);
      log.trace("Persisting new membership between collection {} and member item {}.", id, item.getId());
      membershipDao.save(m);
    }

    log.trace("Returning collection of created member items.");
    return new ResponseEntity<>(content, HttpStatus.CREATED);
  }

  public ResponseEntity<CollectionObject> collectionsIdPut(
          @ApiParam(value = "Persistent identifier for the collection", required = true) @PathVariable("id") String id,
          @ApiParam(value = "The properties of the collection to be updated.", required = true) @Valid @RequestBody CollectionObject content){
    log.trace("Calling collectionsIdPut({}, {}).", id, content);

    Optional<CollectionObject> result = collectionDao.findById(id);

    if(result.isEmpty()){
      log.debug("No collection with id {} found.", id);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    CollectionObject existing = result.get();
    if(content.getDescription() != null){
      log.debug("Updating collection description from value {} to value {}.", existing.getDescription(), content.getDescription());
      existing.setDescription(content.getDescription());
    }

    log.trace("Checking collection properties.");
    if(existing.getCapabilities() != null && existing.getCapabilities().getPropertiesAreMutable() && content.getProperties() != null){
      CollectionProperties props = existing.getProperties();

      if(content.getProperties().getDescriptionOntology() != null){
        log.debug("Updating collection property 'descriptionOntology' from value {} to value {}.", props.getDescriptionOntology(), content.getProperties().getDescriptionOntology());
        props.setDescriptionOntology(content.getProperties().getDescriptionOntology());
      }

      if(content.getProperties().getLicense() != null){
        log.debug("Updating collection property 'license' from value {} to value {}.", props.getLicense(), content.getProperties().getLicense());
        props.setLicense(content.getProperties().getLicense());
      }

      if(content.getProperties().getModelType() != null){
        log.debug("Updating collection property 'modelType' from value {} to value {}.", props.getModelType(), content.getProperties().getModelType());
        props.setModelType(content.getProperties().getModelType());
      }
      if(content.getProperties().getOwnership() != null){
        log.debug("Updating collection property 'ownership' from value {} to value {}.", props.getOwnership(), content.getProperties().getOwnership());
        props.setOwnership(content.getProperties().getOwnership());
      }
      if(content.getProperties().getMemberOf() != null){
        log.debug("Updating collection property 'memberOf' from value {} to value {}.", props.getMemberOf(), content.getProperties().getMemberOf());
        props.setMemberOf(content.getProperties().getMemberOf());
      }
    } else{
      log.debug("Either no collection properties provided or properties are not allowed to be updated.");
    }

    log.trace("Checking collection capabilities.");
    if(content.getCapabilities() != null){
      if(existing.getCapabilities() == null){
        log.trace("No capabilities defined for existing collection. Transferring capabilities from provided collection.");
        existing.setCapabilities(content.getCapabilities());
        existing.getCapabilities().setId(null);
      } else{
        log.trace("Transferring capabilities from provided collection.");
        if(content.getCapabilities().getAppendsToEnd() != null){
          log.debug("Updating collection capability 'appendsToEnd' from value {} to value {}.", existing.getCapabilities().getAppendsToEnd(), content.getCapabilities().getAppendsToEnd());
          existing.getCapabilities().setAppendsToEnd(content.getCapabilities().getAppendsToEnd());
        }

        if(content.getCapabilities().getIsOrdered() != null){
          log.debug("Updating collection capability 'isOrdered' from value {} to value {}.", existing.getCapabilities().getIsOrdered(), content.getCapabilities().getIsOrdered());
          existing.getCapabilities().setIsOrdered(content.getCapabilities().getIsOrdered());
        }

        if(content.getCapabilities().getMembershipIsMutable() != null){
          log.debug("Updating collection capability 'membershipIsMutable' from value {} to value {}.", existing.getCapabilities().getMembershipIsMutable(), content.getCapabilities().getMembershipIsMutable());
          existing.getCapabilities().setMembershipIsMutable(content.getCapabilities().getMembershipIsMutable());
        }

        if(content.getCapabilities().getPropertiesAreMutable() != null){
          log.debug("Updating collection capability 'propertiesAreMutable' from value {} to value {}.", existing.getCapabilities().getPropertiesAreMutable(), content.getCapabilities().getPropertiesAreMutable());
          existing.getCapabilities().setPropertiesAreMutable(content.getCapabilities().getPropertiesAreMutable());
        }

        if(content.getCapabilities().getSupportsRoles() != null){
          log.debug("Updating collection capability 'supportsRoles' from value {} to value {}.", existing.getCapabilities().getSupportsRoles(), content.getCapabilities().getSupportsRoles());
          existing.getCapabilities().setSupportsRoles(content.getCapabilities().getSupportsRoles());
        }

        if(content.getCapabilities().getMaxLength() != null){
          log.debug("Updating collection capability 'maxLength' from value {} to value {}.", existing.getCapabilities().getMaxLength(), content.getCapabilities().getMaxLength());
          existing.getCapabilities().setMaxLength(content.getCapabilities().getMaxLength());
        }

        if(content.getCapabilities().getRestrictedToType() != null){
          log.debug("Updating collection capability 'restrictedToType' from value {} to value {}.", existing.getCapabilities().getRestrictedToType(), content.getCapabilities().getRestrictedToType());
          existing.getCapabilities().setRestrictedToType(content.getCapabilities().getRestrictedToType());
        }
      }

    }

    log.trace("Persisting updated collection.");
    existing = collectionDao.save(existing);
    log.trace("Returning updated collection.");
    return new ResponseEntity<>(existing, HttpStatus.OK);

  }

  public ResponseEntity<List<CollectionObject>> collectionsPost(@ApiParam(value = "The properties of the collection.", required = true) @Valid @RequestBody List<CollectionObject> content){
    log.trace("Calling collectionsPost({}).", content);

    log.trace("Checking collection ids for duplicated.");
    for(CollectionObject collection : content){
      if(collection.getId() != null){
        Optional<CollectionObject> result = collectionDao.findById(collection.getId());

        if(result.isPresent()){
          log.debug("There is already a collection with id {}.", collection.getId());
          return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
      } else{
        log.trace("Collection has no id. Setting random UUID.");
        collection.setId(UUID.randomUUID().toString());
        log.trace("UUID {} assigned as collection id.", collection.getId());
      }
    }

    log.trace("Checking properties and creating collections.");
    for(CollectionObject collection : content){
      log.trace("Checking collection properties.");
      CollectionProperties props = collection.getProperties();
      if(props == null){
        log.trace("Collection properties not present. Creating new collection properties instance.");
        props = new CollectionProperties();
        collection.setProperties(props);
      }

      log.trace("Setting property 'dateCreated' to now().");
      props.setDateCreated(Instant.now());

      log.trace("Checking collection capabilities.");
      CollectionCapabilities caps = collection.getCapabilities();

      if(caps == null){
        log.trace("Collection capabilities not present. Creating new default collection capabilities instance.");
        collection.setCapabilities(CollectionCapabilities.getDefault());
      }

      log.trace("Persisting new collection.");
      collectionDao.save(collection);
    }

    log.trace("Returning created collections.");
    return new ResponseEntity<>(content, HttpStatus.CREATED);
  }

  /**
   * *COLLECTION OPS**
   */
  public ResponseEntity<MemberResultSet> collectionsIdOpsFindMatchPost(
          @ApiParam(value = "identifier for the collection", required = true) @PathVariable("id") String id,
          @ApiParam(value = "the member item properties to use when matching", required = true) @Valid @RequestBody MemberItem memberProperties,
          final Pageable pgbl){
    String accept = request.getHeader("Accept");
    if(accept != null && accept.contains("application/json")){
      try{
        return new ResponseEntity<MemberResultSet>(objectMapper.readValue("{  \"prev_cursor\" : \"prev_cursor\",  \"next_cursor\" : \"next_cursor\",  \"contents\" : [ {    \"mappings\" : {      \"role\" : \"role\",      \"index\" : 0,      \"dateAdded\" : \"2000-01-23T04:56:07.000+00:00\",      \"dateUpdated\" : \"2000-01-23T04:56:07.000+00:00\"    },    \"datatype\" : \"datatype\",    \"description\" : \"description\",    \"location\" : \"location\",    \"id\" : \"id\",    \"ontology\" : \"ontology\"  }, {    \"mappings\" : {      \"role\" : \"role\",      \"index\" : 0,      \"dateAdded\" : \"2000-01-23T04:56:07.000+00:00\",      \"dateUpdated\" : \"2000-01-23T04:56:07.000+00:00\"    },    \"datatype\" : \"datatype\",    \"description\" : \"description\",    \"location\" : \"location\",    \"id\" : \"id\",    \"ontology\" : \"ontology\"  } ]}", MemberResultSet.class), HttpStatus.NOT_IMPLEMENTED);
      } catch(IOException e){
        log.error("Couldn't serialize response for content type application/json", e);
        return new ResponseEntity<MemberResultSet>(HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }

    return new ResponseEntity<MemberResultSet>(HttpStatus.NOT_IMPLEMENTED);
  }

  public ResponseEntity<MemberResultSet> collectionsIdOpsFlattenGet(
          @ApiParam(value = "Identifier for the collection to be flattened", required = true) @PathVariable("id") String id,
          final Pageable pgbl){
    String accept = request.getHeader("Accept");
    if(accept != null && accept.contains("application/json")){
      try{
        return new ResponseEntity<MemberResultSet>(objectMapper.readValue("{  \"prev_cursor\" : \"prev_cursor\",  \"next_cursor\" : \"next_cursor\",  \"contents\" : [ {    \"mappings\" : {      \"role\" : \"role\",      \"index\" : 0,      \"dateAdded\" : \"2000-01-23T04:56:07.000+00:00\",      \"dateUpdated\" : \"2000-01-23T04:56:07.000+00:00\"    },    \"datatype\" : \"datatype\",    \"description\" : \"description\",    \"location\" : \"location\",    \"id\" : \"id\",    \"ontology\" : \"ontology\"  }, {    \"mappings\" : {      \"role\" : \"role\",      \"index\" : 0,      \"dateAdded\" : \"2000-01-23T04:56:07.000+00:00\",      \"dateUpdated\" : \"2000-01-23T04:56:07.000+00:00\"    },    \"datatype\" : \"datatype\",    \"description\" : \"description\",    \"location\" : \"location\",    \"id\" : \"id\",    \"ontology\" : \"ontology\"  } ]}", MemberResultSet.class), HttpStatus.NOT_IMPLEMENTED);
      } catch(IOException e){
        log.error("Couldn't serialize response for content type application/json", e);
        return new ResponseEntity<MemberResultSet>(HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }

    return new ResponseEntity<MemberResultSet>(HttpStatus.NOT_IMPLEMENTED);
  }

  public ResponseEntity<MemberResultSet> collectionsIdOpsIntersectionOtherIdGet(
          @ApiParam(value = "Identifier for the first collection in the operation", required = true) @PathVariable("id") String id,
          @ApiParam(value = "Identifier for the second collection in the operation", required = true) @PathVariable("otherId") String otherId,
          final Pageable pgbl){
    String accept = request.getHeader("Accept");
    if(accept != null && accept.contains("application/json")){
      try{
        return new ResponseEntity<MemberResultSet>(objectMapper.readValue("{  \"prev_cursor\" : \"prev_cursor\",  \"next_cursor\" : \"next_cursor\",  \"contents\" : [ {    \"mappings\" : {      \"role\" : \"role\",      \"index\" : 0,      \"dateAdded\" : \"2000-01-23T04:56:07.000+00:00\",      \"dateUpdated\" : \"2000-01-23T04:56:07.000+00:00\"    },    \"datatype\" : \"datatype\",    \"description\" : \"description\",    \"location\" : \"location\",    \"id\" : \"id\",    \"ontology\" : \"ontology\"  }, {    \"mappings\" : {      \"role\" : \"role\",      \"index\" : 0,      \"dateAdded\" : \"2000-01-23T04:56:07.000+00:00\",      \"dateUpdated\" : \"2000-01-23T04:56:07.000+00:00\"    },    \"datatype\" : \"datatype\",    \"description\" : \"description\",    \"location\" : \"location\",    \"id\" : \"id\",    \"ontology\" : \"ontology\"  } ]}", MemberResultSet.class), HttpStatus.NOT_IMPLEMENTED);
      } catch(IOException e){
        log.error("Couldn't serialize response for content type application/json", e);
        return new ResponseEntity<MemberResultSet>(HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }

    return new ResponseEntity<MemberResultSet>(HttpStatus.NOT_IMPLEMENTED);
  }

  public ResponseEntity<MemberResultSet> collectionsIdOpsUnionOtherIdGet(
          @ApiParam(value = "Identifier for the first collection in the operation", required = true) @PathVariable("id") String id,
          @ApiParam(value = "Identifier for the second collection in the operation", required = true) @PathVariable("otherId") String otherId,
          final Pageable pgbl){
    String accept = request.getHeader("Accept");
    if(accept != null && accept.contains("application/json")){
      try{
        return new ResponseEntity<MemberResultSet>(objectMapper.readValue("{  \"prev_cursor\" : \"prev_cursor\",  \"next_cursor\" : \"next_cursor\",  \"contents\" : [ {    \"mappings\" : {      \"role\" : \"role\",      \"index\" : 0,      \"dateAdded\" : \"2000-01-23T04:56:07.000+00:00\",      \"dateUpdated\" : \"2000-01-23T04:56:07.000+00:00\"    },    \"datatype\" : \"datatype\",    \"description\" : \"description\",    \"location\" : \"location\",    \"id\" : \"id\",    \"ontology\" : \"ontology\"  }, {    \"mappings\" : {      \"role\" : \"role\",      \"index\" : 0,      \"dateAdded\" : \"2000-01-23T04:56:07.000+00:00\",      \"dateUpdated\" : \"2000-01-23T04:56:07.000+00:00\"    },    \"datatype\" : \"datatype\",    \"description\" : \"description\",    \"location\" : \"location\",    \"id\" : \"id\",    \"ontology\" : \"ontology\"  } ]}", MemberResultSet.class), HttpStatus.NOT_IMPLEMENTED);
      } catch(IOException e){
        log.error("Couldn't serialize response for content type application/json", e);
        return new ResponseEntity<MemberResultSet>(HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }

    return new ResponseEntity<MemberResultSet>(HttpStatus.NOT_IMPLEMENTED);
  }
}
