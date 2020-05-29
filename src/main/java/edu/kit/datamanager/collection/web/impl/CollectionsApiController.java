package edu.kit.datamanager.collection.web.impl;

import com.google.common.base.Objects;
import edu.kit.datamanager.collection.configuration.CollectionRegistryConfig;
import edu.kit.datamanager.collection.domain.CollectionCapabilities;
import edu.kit.datamanager.collection.domain.CollectionObject;
import edu.kit.datamanager.collection.domain.CollectionResultSet;
import edu.kit.datamanager.collection.domain.MemberItem;
import edu.kit.datamanager.collection.domain.MemberResultSet;
import edu.kit.datamanager.collection.dao.ICollectionObjectDao;
import edu.kit.datamanager.collection.dao.IMemberItemDao;
import edu.kit.datamanager.collection.web.CollectionsApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import javax.validation.Valid;
import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import edu.kit.datamanager.collection.dao.IMembershipDao;
import edu.kit.datamanager.collection.util.JPAQueryHelper;
import edu.kit.datamanager.collection.domain.CollectionItemMappingMetadata;
import edu.kit.datamanager.collection.domain.CollectionProperties;
import edu.kit.datamanager.collection.domain.Membership;
import edu.kit.datamanager.collection.domain.d3.CollectionNode;
import edu.kit.datamanager.collection.domain.d3.DataWrapper;
import edu.kit.datamanager.collection.domain.d3.Link;
import edu.kit.datamanager.collection.domain.d3.MemberItemNode;
import edu.kit.datamanager.collection.exceptions.CircularDependencyException;
import edu.kit.datamanager.collection.util.ControllerUtils;
import edu.kit.datamanager.collection.util.PaginationHelper;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-07-09T15:21:24.632+02:00")
@RestController
@Schema(title = "collections", description = "the collections API")
@RequestMapping(value = "/api/v1")
public class CollectionsApiController implements CollectionsApi {

    private static final Logger LOG = LoggerFactory.getLogger(CollectionsApiController.class);

    private final HttpServletRequest request;

    @Autowired
    private ICollectionObjectDao collectionDao;
    @Autowired
    private IMemberItemDao memberDao;
    @Autowired
    private IMembershipDao membershipDao;

    @Autowired
    private CollectionRegistryConfig collectionRegistry;
    
    @PersistenceContext
    private EntityManager em;

    @org.springframework.beans.factory.annotation.Autowired
    public CollectionsApiController(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public ResponseEntity<List<CollectionObject>> collectionsPost(@Valid @RequestBody List<CollectionObject> content) {
        LOG.trace("Calling collectionsPost({}).", content);

        LOG.trace("Checking collection ids for duplicates.");
        List<String> ids = new ArrayList<>();
        for (CollectionObject object : content) {
            if (object.getId() == null) {
                //TODO: Add PID generator here
                LOG.trace("Adding local pid to collection without provided id");
                object.setId(UUID.randomUUID().toString());
                LOG.trace("Assigned local pid {} to collection object.", object.getId());
            }
            ids.add(object.getId());
        }

        long existingCollectionCount = collectionDao.countByIdIn(ids.toArray(new String[]{}));

        if (existingCollectionCount > 0) {
            LOG.debug("There is already a collection at least one of the following ids: {}.", ids);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        Long existingMemberCount = memberDao.countByMidIn(ids.toArray(new String[]{}));
        if (existingMemberCount > 0){
             LOG.debug("There is already a member at least one of the following ids: {}.", ids);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        
        LOG.trace("Checking properties and creating collections.");
        for (CollectionObject collection : content) {
            LOG.trace("Checking collection properties.");
            CollectionProperties props = collection.getProperties();
            if (props == null) {
                LOG.trace("Collection properties not present. Creating new collection properties instance.");
                props = new CollectionProperties();
                collection.setProperties(props);
            }

            LOG.trace("Setting property 'dateCreated' to now().");
            props.setDateCreated(Instant.now());

            LOG.trace("Checking collection capabilities.");
            CollectionCapabilities caps = collection.getCapabilities();

            if (caps == null) {
                LOG.trace("Collection capabilities not present. Creating new default collection capabilities instance.");
                collection.setCapabilities(CollectionCapabilities.getDefault());
            }

            LOG.trace("Persisting new collection.");
            collectionDao.save(collection);
            
            collectionRegistry.getCollectionGraph().addEdge(collection.getId(), new HashSet<String>());
        }

        LOG.trace("Returning created collections.");
        return new ResponseEntity<>(content, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<CollectionResultSet> collectionsGet(
            @Valid @RequestParam(value = "f_modelType", required = false) String fModelType,
            @Valid @RequestParam(value = "f_memberType", required = false) String fMemberType,
            @Valid @RequestParam(value = "f_ownership", required = false) String fOwnership,
            final Pageable pgbl,
            final HttpServletRequest request,
            final UriComponentsBuilder uriBuilder) {
        LOG.trace("Calling collectionsGet({}, {}, {}, {}).", fModelType, fMemberType, fOwnership, pgbl);
        int pageSize = (pgbl != null) ? pgbl.getPageSize() : 20;
        int offset = (pgbl != null) ? pgbl.getPageNumber() * pgbl.getPageSize() : 0;
        JPAQueryHelper helper = new JPAQueryHelper(em);
        LOG.trace("Listing collection from index {} with page size of {}.", offset, pageSize);
        List<CollectionObject> resultList = helper.getCollectionsByFilters(fOwnership, fModelType, fMemberType, offset, pageSize);
        LOG.trace("Obtained {} result(s). Obtaining total element count.", resultList.size());
        Long totalElementCount = helper.getCollectionsCountByFilters(fOwnership, fModelType, fMemberType);
        LOG.trace("Total element count is {}. Calculating total page.", totalElementCount);
        int totalPages = (totalElementCount > 0) ? (int) Math.rint(totalElementCount / pageSize) + ((totalElementCount % pageSize != 0) ? 1 : 0) : 0;

        CollectionResultSet resultSet = new CollectionResultSet();
        LOG.trace("Filling result set with {} results from result list.", resultList.size());
        resultList.forEach((o) -> {
            resultSet.addContentsItem(o);
        });

        LOG.trace("Setting cursor values.");
        resultSet.setNextCursor(PaginationHelper.create(pgbl.getPageNumber(), totalElementCount).withElementsPerPage(pgbl.getPageSize()).getNextPageLink());
        resultSet.setPrevCursor(PaginationHelper.create(pgbl.getPageNumber(), totalElementCount).withElementsPerPage(pgbl.getPageSize()).getPrevPageLink());
        LOG.trace("Returning result set.");
        
        LOG.trace("Structure of Collections");
       LOG.trace("{}",collectionRegistry.getCollectionGraph().toString());
        return new ResponseEntity<>(resultSet, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<DataWrapper> collectionsGetD3(HttpServletRequest request, UriComponentsBuilder uriBuilder) {
        LOG.trace("Calling collectionsGetD3().");

        DataWrapper wrapper = new DataWrapper();
        List<String> collectionIds = new ArrayList<>();
        JPAQueryHelper helper = new JPAQueryHelper(em);

        List<CollectionObject> collections = collectionDao.findAll();
        for (CollectionObject o : collections) {
            CollectionNode n = new CollectionNode();
            n.setId(o.getId());
            n.setRadius(8 + o.getProperties().getMemberOf().size());
            n.setDescription(o.getDescription());
            n.setProperties(o.getProperties());
            n.setCapabilities(o.getCapabilities());
            wrapper.getNodes().add(n);
            collectionIds.add(o.getId());
        }

        for (CollectionObject o : collections) {
            List<Membership> memberships = helper.getColletionsMembershipsByFilters(Arrays.asList(o.getId()), null, null, null, null, false, 0, 20);
            for (Membership m : memberships) {
                if (!collectionIds.contains(m.getMember().getMid())) {
                    MemberItemNode n_m = new MemberItemNode();
                    n_m.setId(m.getMember().getMid());
                    n_m.setRadius(5);
                    n_m.setDescription(m.getMember().getDescription());
                    n_m.setLocation(m.getMember().getLocation());
                    n_m.setMapping(m.getMappings());
                    wrapper.getNodes().add(n_m);
                }
                Link l = new Link();
                l.setSource(o.getId());
                l.setTarget(m.getMember().getMid());
                l.setDistance((int) Math.rint(Math.random() * 40.0));
                wrapper.getLinks().add(l);
            }
        }

        return new ResponseEntity<>(wrapper, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CollectionObject> collectionsIdGet(@PathVariable("id") String id) {

//test data creation
//    collectionDao.deleteAll();
//    membershipDao.deleteAll();
//
//    TestDataCreationHelper testHelper = TestDataCreationHelper.initialize(collectionDao, memberDao);
//    for(int i = 0; i < 20; i++){
//      testHelper = testHelper.addCollection("c" + i, CollectionProperties.getDefault());
//    }
//
//    for(int i = 0; i < 20; i++){
//      for(int j = 0; j < 20; j++){
//        testHelper = testHelper.addMemberItem("c" + i, "m" + i + "-" + j, "localhost");
//      }
//    }
//
//    testHelper = testHelper.addMemberItem("c2", "c1", "localhost");
//    testHelper = testHelper.addMemberItem("c3", "c1", "localhost");
//    testHelper = testHelper.addMemberItem("c2", "c4", "localhost");
//    testHelper = testHelper.addMemberItem("c4", "c5", "localhost");
//    testHelper = testHelper.addMemberItem("c8", "c12", "localhost");
//    testHelper = testHelper.addMemberItem("c1", "c18", "localhost");
//    testHelper = testHelper.addMemberItem("c15", "c17", "localhost");
//    testHelper = testHelper.addMemberItem("c9", "c19", "localhost");
//    testHelper = testHelper.addMemberItem("c9", "c5", "localhost");
//
//    testHelper.persist();
        LOG.trace("Calling collectionsIdGet({}).", id);

        Optional<CollectionObject> result = collectionDao.findById(id);

        if (result.isEmpty()) {
            LOG.debug("No collection with id {} found.", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        LOG.trace("Returning collection capabilities.");
        return ResponseEntity.ok().eTag(result.get().getEtag()).body(result.get());
    }

    @Override
    public ResponseEntity<CollectionObject> collectionsIdPut(
            @PathVariable("id") String id,
            @Valid @RequestBody CollectionObject content) {
        LOG.trace("Calling collectionsIdPut({}, {}).", id, content);

        Optional<CollectionObject> result = collectionDao.findById(id);

        if (result.isEmpty()) {
            LOG.debug("No collection with id {} found.", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        CollectionObject existing = result.get();

        ControllerUtils.checkEtag(request, existing);

        if (content.getDescription() != null) {
            LOG.debug("Updating collection description from value {} to value {}.", existing.getDescription(), content.getDescription());
            existing.setDescription(content.getDescription());
        }

        LOG.trace("Checking collection properties.");
        if ((existing.getCapabilities() == null || existing.getCapabilities().getPropertiesAreMutable()) && content.getProperties() != null) {
            CollectionProperties props = existing.getProperties();

            LOG.debug("Updating collection property 'dateCreated' from value {} to value {}.", props.getDateCreated(), content.getProperties().getDateCreated());
            props.setDateCreated(content.getProperties().getDateCreated());

            LOG.debug("Updating collection property 'descriptionOntology' from value {} to value {}.", props.getDescriptionOntology(), content.getProperties().getDescriptionOntology());
            props.setDescriptionOntology(content.getProperties().getDescriptionOntology());

            LOG.debug("Updating collection property 'license' from value {} to value {}.", props.getLicense(), content.getProperties().getLicense());
            props.setLicense(content.getProperties().getLicense());

            LOG.debug("Updating collection property 'modelType' from value {} to value {}.", props.getModelType(), content.getProperties().getModelType());
            props.setModelType(content.getProperties().getModelType());

            LOG.debug("Updating collection property 'ownership' from value {} to value {}.", props.getOwnership(), content.getProperties().getOwnership());
            props.setOwnership(content.getProperties().getOwnership());

            LOG.debug("Updating collection property 'memberOf' from value {} to value {}.", props.getMemberOf(), content.getProperties().getMemberOf());
            props.setMemberOf(content.getProperties().getMemberOf());
        } else if (existing.getCapabilities() != null && !existing.getCapabilities().getPropertiesAreMutable()) {
            if (existing.getCapabilities() != null && !existing.getCapabilities().getPropertiesAreMutable()) {
                LOG.error("Collection does not allow property update by its capabilities. Returning HTTP 403.");
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        }

        LOG.trace("Checking collection capabilities.");
        if (content.getCapabilities() != null) {
            if (existing.getCapabilities() == null) {
                //should not happen as default capabilities are created for every collection
                LOG.trace("No capabilities defined for existing collection. Transferring capabilities from provided collection.");
                existing.setCapabilities(content.getCapabilities());
                existing.getCapabilities().setId(null);
            } else {
                LOG.trace("Transferring capabilities from provided collection.");
                LOG.debug("Updating collection capability 'appendsToEnd' from value {} to value {}.", existing.getCapabilities().getAppendsToEnd(), content.getCapabilities().getAppendsToEnd());
                existing.getCapabilities().setAppendsToEnd(content.getCapabilities().getAppendsToEnd());

                LOG.debug("Updating collection capability 'isOrdered' from value {} to value {}.", existing.getCapabilities().getIsOrdered(), content.getCapabilities().getIsOrdered());
                existing.getCapabilities().setIsOrdered(content.getCapabilities().getIsOrdered());

                LOG.debug("Updating collection capability 'membershipIsMutable' from value {} to value {}.", existing.getCapabilities().getMembershipIsMutable(), content.getCapabilities().getMembershipIsMutable());
                existing.getCapabilities().setMembershipIsMutable(content.getCapabilities().getMembershipIsMutable());

                LOG.debug("Updating collection capability 'propertiesAreMutable' from value {} to value {}.", existing.getCapabilities().getPropertiesAreMutable(), content.getCapabilities().getPropertiesAreMutable());
                existing.getCapabilities().setPropertiesAreMutable(content.getCapabilities().getPropertiesAreMutable());

                LOG.debug("Updating collection capability 'supportsRoles' from value {} to value {}.", existing.getCapabilities().getSupportsRoles(), content.getCapabilities().getSupportsRoles());
                existing.getCapabilities().setSupportsRoles(content.getCapabilities().getSupportsRoles());

                LOG.debug("Updating collection capability 'maxLength' from value {} to value {}.", existing.getCapabilities().getMaxLength(), content.getCapabilities().getMaxLength());
                existing.getCapabilities().setMaxLength(content.getCapabilities().getMaxLength());

                LOG.debug("Updating collection capability 'restrictedToType' from value {} to value {}.", existing.getCapabilities().getRestrictedToType(), content.getCapabilities().getRestrictedToType());
                existing.getCapabilities().setRestrictedToType(content.getCapabilities().getRestrictedToType());
            }
        }

        LOG.trace("Persisting updated collection.");
        existing = collectionDao.save(existing);
        LOG.trace("Returning updated collection.");
        return new ResponseEntity<>(existing, HttpStatus.OK);

    }

    @Override
    public ResponseEntity<Void> collectionsIdDelete(@PathVariable("id") String id) {
        LOG.trace("Calling collectionsIdDelete({}).", id);
        Optional<CollectionObject> result = collectionDao.findById(id);

        if (!result.isEmpty()) {
            ControllerUtils.checkEtag(request, result.get());

            Set<Membership> memberships = result.get().getMembers();
            Set<MemberItem> memberItems = new HashSet<>();
            memberships.forEach(membership -> {
                memberItems.add(membership.getMember());
            });  
            Set<String> collectionMemberOfs = result.get().getProperties().getMemberOf();
            LOG.trace("Deleting collection with id {}.", id);
            collectionDao.delete(result.get());

            LOG.trace("Returning HTTP 204.");
            
            memberItems.forEach(memberItem -> {
                Optional<Membership> membership = membershipDao.findByMember(memberItem);
                Optional<CollectionObject> collection = collectionDao.findById(memberItem.getMid());
                
                //delete id of the deleted collection from MemberOf
                if (!collection.isEmpty()){
                    LOG.trace("Deleting the id of the deleted collection {} from MemberOf of the collection with id {}", id, collection.get().getId());
                    collection.get().getProperties().getMemberOf().remove(id);
                    collectionDao.save(collection.get());
                }
                //delete memberItem if it has no memberships
                if (membership.isEmpty()){
                    LOG.trace("Deleting MemberItem with id {} having no membership", memberItem.getMid());
                    memberDao.delete(memberItem);
                    LOG.trace("Returning HTTP 204.");
                }
                
            });   
            //delete the member Item which is a collection id from the parent collection
            Set<MemberItem> memberItemsToDelete= new HashSet<>();
            for (String collectionId: collectionMemberOfs){
                Optional<CollectionObject> collection = collectionDao.findById(collectionId);
                if (!collection.isEmpty()){
                    memberships = collection.get().getMembers();
                    for (Membership membershipToDelete: memberships){
                        MemberItem memberItemToDelete = membershipToDelete.getMember();
                        if (memberItemToDelete.getMid().equals(id)){
                             LOG.trace("Deleting MemberItem with id {} from Collection with id {}", id, collectionId);
                            collection.get().getMembers().remove(membershipToDelete);
                            collectionDao.save(collection.get());
                            memberItemsToDelete.add(memberItemToDelete);
                            membershipToDelete.setMember(null);
                            membershipDao.delete(membershipToDelete); 
                        }
                    }
                }
            }
            memberItemsToDelete.forEach((memberItem) -> {
                memberDao.delete(memberItem);
            });
        //delete collection id from the structure of collections
        collectionRegistry.getCollectionGraph().removeCollection(id);
        } else {
            LOG.trace("No collection with id {} found. Returning HTTP 204.", id);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<CollectionCapabilities> collectionsIdCapabilitiesGet(@PathVariable("id") String id) {
        LOG.trace("Calling collectionsIdCapabilitiesGet({}).", id);

        Optional<CollectionObject> result = collectionDao.findById(id);

        if (result.isEmpty()) {
            LOG.debug("No collection with id {} found.", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        LOG.trace("Returning collection capabilities.");
        return ResponseEntity.ok().eTag(result.get().getEtag()).body(result.get().getCapabilities());
    }

    @Override
    public ResponseEntity<List<MemberItem>> collectionsIdMembersPost(
            @PathVariable("id") String id,
            @Valid @RequestBody List<MemberItem> content) {
        LOG.trace("Calling collectionsIdMembersPost({}).", id);

        Optional<CollectionObject> result = collectionDao.findById(id);
        if (result.isEmpty()) {
            LOG.debug("No collection with id {} found.", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        CollectionObject existing = result.get();
        Map<String, MemberItem> existingMembers = new HashMap<>();

        String restrictedToType = null;
        LOG.trace("Checking if update is allowed by collection capabilities.");
        if (existing.getCapabilities() != null) {
            LOG.trace("Check property 'membershipMutable'.");
            if (!existing.getCapabilities().getMembershipIsMutable()) {
                LOG.error("Unable to add members to immutable collection with id {}.", id);
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            LOG.trace("Check property 'maxLength'.");
            if (existing.getCapabilities().getMaxLength() > -1) {
                LOG.trace("Checking collection maxLength property.");
                int membershipCount = existing.getMembers().size();
                if (membershipCount + content.size() >= existing.getCapabilities().getMaxLength()) {
                    LOG.error("Adding {} members to collection would exceed max count {}.", content.size(), existing.getCapabilities().getMaxLength());
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            }
            LOG.trace("Obtaining property 'restrictedToType'.");
            restrictedToType = existing.getCapabilities().getRestrictedToType();
        }

        LOG.trace("Checking {} member items for proper type, assigned ids and membership conflicts.");
        for (MemberItem item : content) {
            if (Objects.equal(id, item.getMid())) {
                LOG.error("Unable to add collection {} as member to itself.", id);
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            if (restrictedToType != null && !restrictedToType.equals(item.getDatatype())) {
                LOG.error("Member has invalid type. Collection with id {} only supports type {}, but member provided type {}.", id, restrictedToType, item.getDatatype());
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            LOG.trace("Checking member for existing id.");

            if (item.getMid() == null) {
                LOG.trace("Member item has no id, yet. Setting random UUID.");
                item.setMid(UUID.randomUUID().toString());
                LOG.trace("UUID {} assigned as member item id.", item.getId());
            } else {
                //check if id is collection
                Optional<CollectionObject> optionalCollection = collectionDao.findById(item.getMid());
                if (optionalCollection.isPresent()) {
                    CollectionObject collection = optionalCollection.get();
                    if (collection.getCapabilities().getRestrictedToType() != null && !collection.getCapabilities().getRestrictedToType().equals(existing.getCapabilities().getRestrictedToType())){
                        LOG.error("Collection has invalid resctricted Type. Collection with id {} only supports type {}, but member collection provided type {}.", id, existing.getCapabilities().getRestrictedToType(), collection.getCapabilities().getRestrictedToType());
                        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                    }
                    //check if the graph is circular when adding a new member
                    try{
                        collectionRegistry.getCollectionGraph().isCircular(id, item.getMid());
                    }catch (CircularDependencyException e){
                        LOG.error("Member cannot be added. {}", e.getMessage());
                        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                    }
                    LOG.trace("Provided member with id {} represents an existing collection. Adding parent collection id {} to memberOf property.", item.getMid(), id);
                    collection.getProperties().getMemberOf().add(id);
                    
                    //a collection might have already a memberItem
                    Optional<MemberItem> optionalMember = memberDao.findByMid(item.getMid());
                    if (optionalMember.isPresent()) {
                        //existing member item of a collection found, do not persist member, only membership
                        LOG.trace("Existing member found for mid {}.", item.getMid());
                        existingMembers.put(item.getMid(), optionalMember.get());
                    }
                } else {
                    //might be another existing member?
                    Optional<MemberItem> optionalMember = memberDao.findByMid(item.getMid());
                    if (optionalMember.isPresent()) {
                        if (restrictedToType != null && !restrictedToType.equals(optionalMember.get().getDatatype())){
                            LOG.error("Member has invalid type. Collection with id {} only supports type {}, but member provided type {}.", id, restrictedToType, optionalMember.get().getDatatype());
                            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                        }
                        //existing member found, do not persist member, only membership
                        LOG.trace("Existing member found for mid {}.", item.getMid());
                        existingMembers.put(item.getMid(), optionalMember.get());
                    }
                }
            }

            JPAQueryHelper helper;

            LOG.trace("Checking for existing membership between collection {} and member item id {}.", id, item.getMid());
            if (new JPAQueryHelper(em).isMemberPartOfCollection(id, item.getMid())) {
                LOG.error("Existing membership found. Returning HTTP CONFLICT.");
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
        }

        LOG.trace("All member items are fine. Adding {} member(s) to collection {}.", content.size(), id);
        for (MemberItem item : content) {
            LOG.trace("Obtaining collection item mapping metadata.");
            CollectionItemMappingMetadata mappingMetadata = item.getMappings();
            if (mappingMetadata == null) {
                LOG.trace("No collection item metadata provided. Creating new metadata instance.");
                mappingMetadata = CollectionItemMappingMetadata.getDefault();
            }

            MemberItem membershipMember;

            if (!existingMembers.containsKey(item.getMid())) {
                LOG.trace("Persisting new member item with id {}.", item.getId());
                membershipMember = memberDao.save(item);
            } else {
                LOG.trace("Skip persisting existing member with id {}.", item.getId());
                membershipMember = existingMembers.get(item.getMid());
                item.copyFrom(membershipMember);
            }

            Membership m = new Membership();
            m.setMember(item);
            m.setMappings(mappingMetadata);

            existing.getMembers().add(m);

            LOG.trace("Persisting new membership between collection {} and member item {}.", id, item.getId());
            collectionDao.save(existing);

            Optional<CollectionObject> optionalCollection = collectionDao.findById(item.getMid());
            if (optionalCollection.isPresent()) {
                    collectionRegistry.getCollectionGraph().addEdge(item.getMid(), id);
            }
            item.setMappings(mappingMetadata);
        }

        LOG.trace("Returning collection of created member items.");
        return new ResponseEntity<>(content, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<MemberResultSet> collectionsIdMembersGet(
            @PathVariable("id") String id,
            @Valid @RequestParam(value = "f_datatype", required = false) String fDatatype,
            @Valid @RequestParam(value = "f_role", required = false) String fRole,
            @Valid @RequestParam(value = "f_index", required = false) Integer fIndex,
            @Valid @RequestParam(value = "f_dateAdded", required = false) Instant fDateAdded,
            @Valid @RequestParam(value = "expandDepth", required = false) Integer expandDepth,
            final Pageable pgbl) {
        LOG.trace("Calling collectionsIdMembersGet({}, {}, {}, {}, {}, {}).", id, fDatatype, fRole, fIndex, fDateAdded, expandDepth);

        Optional<CollectionObject> result = collectionDao.findById(id);

        if (result.isEmpty()) {
            LOG.debug("No collection with id {} found.", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        JPAQueryHelper helper = new JPAQueryHelper(em);
        int offset = pgbl.getPageNumber() * pgbl.getPageSize();
        int pageSize = pgbl.getPageSize();
        int depth = (expandDepth == null) ? 0 : expandDepth;

        LOG.trace("Obtaining sub collections of collection with id {} with expandDepth {}.", id, depth);
        Set<String> collectionIds = helper.getSubCollections(id, depth);
        LOG.trace("Sub-collections: {}", collectionIds);

        List<String> collectionIdList = new ArrayList<>();
        collectionIdList.add(id);
        collectionIds.remove(id);
        collectionIdList.addAll(collectionIds);

        LOG.trace("Obtaining members from collections {}.", collectionIdList);
        List<Membership> itemList = helper.getColletionsMembershipsByFilters(collectionIdList, fDatatype, fIndex, fRole, fDateAdded, result.get().getCapabilities().getIsOrdered(), offset, pageSize);

        MemberResultSet resultSet = new MemberResultSet();
        LOG.trace("Filling result set with {} results from result list.", itemList.size());

        for (Membership membership : itemList) {
            //we have to copy the item in case an item is in multiple collections, in that case the same item  pointer is returned multiple times pointing to the same memory location
            MemberItem item = membership.getMember();
            item.setMappings(membership.getMappings());
            resultSet.addContentsItem(item);
        }

        LOG.trace("Obtaining total element count.");
        long totalElementCount = helper.getColletionsMembershipsCountByFilters(collectionIdList, fDatatype, fIndex, fRole, fDateAdded);

        long totalPages = (totalElementCount > 0) ? (int) Math.rint(totalElementCount / pageSize) + ((totalElementCount % pageSize != 0) ? 1 : 0) : 0;
        LOG.trace("Setting cursor values.");
        resultSet.setNextCursor(PaginationHelper.create(pgbl.getPageNumber(), totalElementCount).withElementsPerPage(pgbl.getPageSize()).getNextPageLink());
        resultSet.setPrevCursor(PaginationHelper.create(pgbl.getPageNumber(), totalElementCount).withElementsPerPage(pgbl.getPageSize()).getPrevPageLink());
        LOG.trace("Returning result set.");
        return new ResponseEntity<>(resultSet, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<MemberItem> collectionsIdMembersMidGet(
            @PathVariable("id") String id,
            @PathVariable("mid") String mid) {
        LOG.trace("Calling collectionsIdMembersMidGet({}, {}).", id, mid);

        Optional<Membership> membership = new JPAQueryHelper(em).getMembershipByMid(id, mid);

        if (membership.isEmpty()) {
            LOG.debug("No membership for collection with id {} and member with id {} found.", id, mid);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        MemberItem resultItem = membership.get().getMember();
        resultItem.setMappings(membership.get().getMappings());

        LOG.trace("Returning member with id {}.", mid);
        return ResponseEntity.ok().eTag(resultItem.getEtag()).body(resultItem);
    }

    @Override
    public ResponseEntity<MemberItem> collectionsIdMembersMidPut(
            @PathVariable("id") String id,
            @PathVariable("mid") String mid,
            @Valid @RequestBody MemberItem content) {
        LOG.trace("Calling collectionsIdMembersMidPut({}, {}, {}).", id, mid, content);

        Optional<Membership> membership = new JPAQueryHelper(em).getMembershipByMid(id, mid);

        if (membership.isEmpty()) {
            LOG.debug("No membership between collection {} and member item {} found.", id, mid);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Membership m = membership.get();

        MemberItem existingMember = m.getMember();
        existingMember.setMappings(m.getMappings());

        ControllerUtils.checkEtag(request, existingMember);

        LOG.trace("Transferring member item properties from provided member item.");

        LOG.debug("Updating member item property 'dataType' from value {} to value {}.", existingMember.getDatatype(), content.getDatatype());
        existingMember.setDatatype(content.getDatatype());
        LOG.debug("Updating member item property 'description' from value {} to value {}.", existingMember.getDescription(), content.getDescription());
        existingMember.setDescription(content.getDescription());
        LOG.debug("Updating member item property 'location' from value {} to value {}.", existingMember.getLocation(), content.getLocation());
        existingMember.setLocation(content.getLocation());
        LOG.debug("Updating member item property 'ontology' from value {} to value {}.", existingMember.getOntology(), content.getOntology());
        existingMember.setOntology(content.getOntology());

        CollectionItemMappingMetadata mMetadata = m.getMappings();
        CollectionItemMappingMetadata itemMetadata = content.getMappings();
        LOG.trace("Transferring collection item mapping metadata from provided member item.");

        if (itemMetadata != null) {

            LOG.trace("Transferring property 'index'.");
            mMetadata.setIndex(itemMetadata.getIndex());

            LOG.trace("Transferring property 'role'.");
            mMetadata.setMemberRole(itemMetadata.getMemberRole());

            LOG.trace("Setting property 'dateUpdated'.");
            mMetadata.setDateUpdated(Instant.now());
        }

        LOG.trace("Persisting updated membership with new collection item metadata.");
        m = membershipDao.save(m);

        MemberItem member = m.getMember();
        member.setMappings(m.getMappings());

        LOG.trace("Returning collection member item.");
        return new ResponseEntity<>(member, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> collectionsIdMembersMidDelete(
            @PathVariable("id") String id,
            @PathVariable("mid") String mid) {
        LOG.trace("Calling collectionsIdMembersMidDelete({}, {}).", id, mid);
        Optional<Membership> membership = new JPAQueryHelper(em).getMembershipByMid(id, mid);

        if (!membership.isEmpty()) {

            MemberItem item = membership.get().getMember();
            item.setMappings(membership.get().getMappings());

            ControllerUtils.checkEtag(request, item);

            CollectionObject memberCollection = null;
            LOG.trace("Checking if member id {} is a collection.", mid);
            Optional<CollectionObject> optionalMemberCollection = collectionDao.findById(mid);
            if (optionalMemberCollection.isPresent()) {
                memberCollection = optionalMemberCollection.get();
                LOG.trace("Member is a collection. Removing collection id {} from 'memberOf' list of collection with id {}.", id, mid);
                memberCollection.getProperties().getMemberOf().remove(id);
            }

            LOG.trace("Obtaining membership collection.");
            CollectionObject collection = collectionDao.findById(id).get();

            if (collection.getCapabilities() != null && !collection.getCapabilities().getMembershipIsMutable()) {
                LOG.warn("Memberships of collection with id {} are immutable. Returning HTTP 403.", id);
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            LOG.trace("Deleting membership for collection {} and member {}.", id, mid);
            collection.getMembers().remove(membership.get());
            LOG.trace("Persisting updated collection with id {}.", id);
            collectionDao.save(collection);
            MemberItem memberItemToDelete = membership.get().getMember();
            membership.get().setMember(null);
            membershipDao.delete(membership.get());
            if (membershipDao.findByMember(item).isEmpty()){
                memberDao.delete(memberItemToDelete);
            }
            
            if (memberCollection != null) {
                LOG.trace("Persisting updated member collection with id {}.", mid);
                collectionDao.save(memberCollection);
            }

            collectionRegistry.getCollectionGraph().removeParentFromChild(id, mid);
            LOG.trace("Returning HTTP 204.");
        } else {
            LOG.trace("No membership for collection with id {} and member with id {} found. Returning HTTP 204.", id, mid);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<String> collectionsIdMembersMidPropertiesPropertyGet(
            @PathVariable("id") String id,
            @PathVariable("mid") String mid,
            @PathVariable("property") String property) {
        LOG.trace("Calling collectionsIdMembersMidPropertiesPropertyGet({}, {}. {}).", id, mid, property);

        Optional<Membership> membership = new JPAQueryHelper(em).getMembershipByMid(id, mid);

        if (membership.isEmpty()) {
            LOG.debug("No membership for collection with id {} and member with id {} found.", id, mid);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Membership membershipItem = membership.get();
        CollectionItemMappingMetadata mappingMetadata = membershipItem.getMappings();

        String value;
        switch (property) {
            case "role":
                LOG.trace("Getting property 'role'.");
                value = mappingMetadata.getMemberRole();
                break;
            case "index":
                LOG.trace("Getting property 'index'.");
                if (mappingMetadata.getIndex() != null) {
                    value = Integer.toString(mappingMetadata.getIndex());
                } else {
                    value = null;
                }
                break;
            case "dateAdded":
                LOG.trace("Getting property 'dateAdded'.");
                if (mappingMetadata.getDateAdded() != null) {
                    value = DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC).format(mappingMetadata.getDateAdded());
                } else {
                    value = null;
                }
                break;
            case "dateUpdated":
                LOG.trace("Getting property 'dateUpdated'.");
                if (mappingMetadata.getDateUpdated() != null) {
                    value = DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC).format(mappingMetadata.getDateUpdated());
                } else {
                    value = null;
                }
                break;
            default:
                LOG.warn("Property {} not found. Supported properties are 'index', 'role', 'dateAdded' and 'dateUpdated'.", property);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        MemberItem item = membership.get().getMember();
        item.setMappings(membership.get().getMappings());

        return ResponseEntity.ok().eTag(item.getEtag()).body(value);
    }

    @Override
    public ResponseEntity<String> collectionsIdMembersMidPropertiesPropertyPut(
            @PathVariable("id") String id,
            @PathVariable("mid") String mid,
            @PathVariable("property") String property,
            @Valid @RequestBody String content) {
        LOG.trace("Calling collectionsIdMembersMidPropertiesPropertyPut({}, {}. {}, {}).", id, mid, property, content);

        Optional<Membership> membership = new JPAQueryHelper(em).getMembershipByMid(id, mid);

        if (membership.isEmpty()) {
            LOG.debug("No membership for collection with id {} and member with id {} found.", id, mid);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Membership membershipItem = membership.get();
        CollectionItemMappingMetadata mappingMetadata = membershipItem.getMappings();

        MemberItem item = membershipItem.getMember();
        item.setMappings(mappingMetadata);

        ControllerUtils.checkEtag(request, item);

        switch (property) {
            case "role":
                LOG.trace("Setting property 'role'.");
                mappingMetadata.setMemberRole(content);
                break;
            case "index":
                LOG.trace("Setting property 'index'.");
                try {
                    mappingMetadata.setIndex(Integer.parseInt(content));
                } catch (NumberFormatException ex) {
                    LOG.error("Invalid value '{}' provided for property '{}'.", content, property);
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
                break;
            case "dateAdded":
                LOG.trace("Setting property 'dateAdded'.");
                try {
                    mappingMetadata.setDateAdded(DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC).parse(content, Instant::from));
                } catch (DateTimeParseException ex) {
                    LOG.error("Invalid value '{}' provided for property '{}'.", content, property);
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
                break;
            case "dateUpdated":
                LOG.trace("Setting property 'dateUpdated'.");
                try {
                    mappingMetadata.setDateUpdated(DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC).parse(content, Instant::from));
                } catch (DateTimeParseException ex) {
                    LOG.error("Invalid value '{}' provided for property '{}'.", content, property);
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
                break;
            default:
                LOG.warn("Property {} not found. Supported properties are 'index', 'role', 'dateAdded' and 'dateUpdated'.", property);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        LOG.trace("Persisting updated membership.");
        membershipDao.save(membershipItem);
        LOG.trace("Returning HTTP 200.");
        return new ResponseEntity<>(content, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> collectionsIdMembersMidPropertiesPropertyDelete(
            @PathVariable("id") String id,
            @PathVariable("mid") String mid,
            @PathVariable("property") String property) {
        LOG.trace("Calling collectionsIdMembersMidPropertiesPropertyDelete({}, {}, {}).", id, mid, property);

        Optional<Membership> membership = new JPAQueryHelper(em).getMembershipByMid(id, mid);

        if (membership.isEmpty()) {
            LOG.debug("No membership for collection with id {} and member with id {} found.", id, mid);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Membership membershipItem = membership.get();

        MemberItem item = membershipItem.getMember();
        item.setMappings(membershipItem.getMappings());

        ControllerUtils.checkEtag(request, item);

        CollectionItemMappingMetadata mappingMetadata = membershipItem.getMappings();

        switch (property) {
            case "role":
                LOG.trace("Deleting property 'role'.");
                mappingMetadata.setMemberRole(null);
                break;
            case "index":
                LOG.trace("Deleting property 'index'.");
                mappingMetadata.setIndex(null);
                break;
            case "dateAdded":
                LOG.trace("Deleting property 'dateAdded'.");
                mappingMetadata.setDateAdded(null);
                break;
            case "dateUpdated":
                LOG.trace("Deleting property 'dateUpdated'.");
                mappingMetadata.setDateUpdated(null);
                break;
            default:
                LOG.warn("Property {} not found. Supported properties are 'index', 'role', 'dateAdded' and 'dateUpdated'.", property);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        LOG.trace("Persisting updated membership.");
        membershipDao.save(membershipItem);
        LOG.trace("Returning HTTP 204.");
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
////////////////////////////
////COLLECTION OPS
///////////////////////////

    @Override
    public ResponseEntity<MemberResultSet> collectionsIdOpsFindMatchPost(
            @PathVariable("id") String id,
            @RequestBody MemberItem memberProperties,
            final Pageable pgbl) {
        LOG.trace("Calling collectionsIdOpsFindMatchPost({}, {}).", id, memberProperties);
        Optional<CollectionObject> result = collectionDao.findById(id);

        if (result.isEmpty()) {
            LOG.debug("No collection with id {} found.", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        JPAQueryHelper helper = new JPAQueryHelper(em);
        int offset = pgbl.getPageNumber() * pgbl.getPageSize();
        int pageSize = pgbl.getPageSize();
        List<Membership> memberships = helper.findCollectionMembershipsByExample(id, memberProperties, result.get().getCapabilities().getIsOrdered(), offset, pageSize);

        MemberResultSet resultSet = new MemberResultSet();
        LOG.trace("Filling result set with {} results from result list.", memberships.size());

        for (Membership membership : memberships) {
            //we have to copy the item in case an item is in multiple collections, in that case the same item  pointer is returned multiple times pointing to the same memory location
            MemberItem item = membership.getMember();
            item.setMappings(membership.getMappings());
            resultSet.addContentsItem(item);
        }

        LOG.trace("Obtaining total element count.");
        long totalElementCount = helper.findCollectionMembershipsCountByExample(id, memberProperties);

        long totalPages = (totalElementCount > 0) ? (int) Math.rint(totalElementCount / pageSize) + ((totalElementCount % pageSize != 0) ? 1 : 0) : 0;
        LOG.trace("Setting cursor values.");
        resultSet.setNextCursor(PaginationHelper.create(pgbl.getPageNumber(), totalElementCount).withElementsPerPage(pgbl.getPageSize()).getNextPageLink());
        resultSet.setPrevCursor(PaginationHelper.create(pgbl.getPageNumber(), totalElementCount).withElementsPerPage(pgbl.getPageSize()).getPrevPageLink());
        LOG.trace("Returning result set.");
        return new ResponseEntity<>(resultSet, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<MemberResultSet> collectionsIdOpsFlattenGet(
            @PathVariable("id") String id,
            final Pageable pgbl) {
        LOG.trace("Calling collectionsIdOpsFlattenGet({}, {}).", id, pgbl);
        return collectionsIdMembersGet(id, null, null, null, null, Integer.MAX_VALUE, pgbl);
    }

    @Override
    public ResponseEntity<MemberResultSet> collectionsIdOpsIntersectionOtherIdGet(
            @PathVariable("id") String id,
            @PathVariable("otherId") String otherId,
            final Pageable pgbl) {
        LOG.trace("Calling collectionsIdOpsIntersectionOtherIdGet({}, {}).", id, otherId);

        Optional<CollectionObject> left = collectionDao.findById(id);

        if (left.isEmpty()) {
            LOG.debug("No collection with id {} found.", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Optional<CollectionObject> right = collectionDao.findById(otherId);

        if (right.isEmpty()) {
            LOG.debug("No collection with id {} found.", otherId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        JPAQueryHelper helper = new JPAQueryHelper(em);
        int offset = pgbl.getPageNumber() * pgbl.getPageSize();
        int pageSize = pgbl.getPageSize();

        List<String> collectionIdList = new ArrayList<>();
        collectionIdList.add(id);
        collectionIdList.add(otherId);

        LOG.trace("Obtaining members from collections {}.", collectionIdList);
        List<Membership> itemList = helper.getCollectionIntersection(id, otherId, left.get().getCapabilities().getIsOrdered() && right.get().getCapabilities().getIsOrdered(), offset, pageSize);

        MemberResultSet resultSet = new MemberResultSet();
        LOG.trace("Filling result set with {} results from result list.", itemList.size());

        for (Membership membership : itemList) {
            //we have to copy the item in case an item is in multiple collections, in that case the same item  pointer is returned multiple times pointing to the same memory location
            MemberItem item = membership.getMember();
            item.setMappings(membership.getMappings());
            resultSet.addContentsItem(item);
        }

        LOG.trace("Obtaining total element count.");
        long totalElementCount = helper.getCollectionIntersectionCount(id, otherId);

        LOG.trace("Setting cursor values.");
        resultSet.setNextCursor(PaginationHelper.create(pgbl.getPageNumber(), totalElementCount).withElementsPerPage(pgbl.getPageSize()).getNextPageLink());
        resultSet.setPrevCursor(PaginationHelper.create(pgbl.getPageNumber(), totalElementCount).withElementsPerPage(pgbl.getPageSize()).getPrevPageLink());
        LOG.trace("Returning result set.");
        return new ResponseEntity<>(resultSet, HttpStatus.OK);

    }

    @Override
    public ResponseEntity<MemberResultSet> collectionsIdOpsUnionOtherIdGet(
            @PathVariable("id") String id,
            @PathVariable("otherId") String otherId,
            final Pageable pgbl) {
        LOG.trace("Calling collectionsIdOpsUnionOtherIdGet({}, {}).", id, otherId);

        Optional<CollectionObject> left = collectionDao.findById(id);

        if (left.isEmpty()) {
            LOG.debug("No collection with id {} found.", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Optional<CollectionObject> right = collectionDao.findById(otherId);

        if (right.isEmpty()) {
            LOG.debug("No collection with id {} found.", otherId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        JPAQueryHelper helper = new JPAQueryHelper(em);
        int offset = pgbl.getPageNumber() * pgbl.getPageSize();
        int pageSize = pgbl.getPageSize();

        List<String> collectionIdList = new ArrayList<>();
        collectionIdList.add(id);
        collectionIdList.add(otherId);

        LOG.trace("Obtaining members from collections {}.", collectionIdList);
        List<Membership> itemList = helper.getColletionsMembershipsByFilters(collectionIdList, null, null, null, null, left.get().getCapabilities().getIsOrdered() && right.get().getCapabilities().getIsOrdered(), offset, pageSize);

        MemberResultSet resultSet = new MemberResultSet();
        LOG.trace("Filling result set with {} results from result list.", itemList.size());

        for (Membership membership : itemList) {
            //we have to copy the item in case an item is in multiple collections, in that case the same item  pointer is returned multiple times pointing to the same memory location
             MemberItem item = membership.getMember();
             MemberItem copyItem = MemberItem.copy(item);
            copyItem.setMappings(membership.getMappings());
            resultSet.addContentsItem(copyItem);
        }

        LOG.trace("Obtaining total element count.");
        long totalElementCount = helper.getColletionsMembershipsCountByFilters(collectionIdList, null, null, null, null);

        LOG.trace("Setting cursor values.");
        resultSet.setNextCursor(PaginationHelper.create(pgbl.getPageNumber(), totalElementCount).withElementsPerPage(pgbl.getPageSize()).getNextPageLink());
        resultSet.setPrevCursor(PaginationHelper.create(pgbl.getPageNumber(), totalElementCount).withElementsPerPage(pgbl.getPageSize()).getPrevPageLink());
        LOG.trace("Returning result set.");
        return new ResponseEntity<>(resultSet, HttpStatus.OK);
    }

}