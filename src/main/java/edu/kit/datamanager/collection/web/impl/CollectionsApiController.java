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
package edu.kit.datamanager.collection.web.impl;

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
import edu.kit.datamanager.collection.exceptions.SmartRuleParseException;
import edu.kit.datamanager.collection.util.ControllerUtils;
import edu.kit.datamanager.collection.util.PaginationHelper;
import edu.kit.datamanager.collection.util.SmartRule;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
//import javax.validation.ConstraintViolation;
//import javax.validation.Validation;
//import javax.validation.Validator;
//import javax.validation.ValidatorFactory;
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
            } else {
                try {
                    object.setId(URLDecoder.decode(object.getId(), "UTF-8"));
                   /* Remove if tested...should work from v1.3.0
                    if (object.getId().contains("/")) {
                        LOG.error("Detected slash character in collection id {}.", object.getId());
                        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                    }*/
                } catch (UnsupportedEncodingException ex) {
                    //ignore
                }
            }
            ids.add(object.getId());
            if (null != object.getProperties() && object.getProperties().getSmartRules() != null) {
                for (String ruleString : object.getProperties().getSmartRules()) {
                    try {
                        SmartRule.fromString(ruleString);
                    } catch (SmartRuleParseException ex) {
                        LOG.error("Failed to evaluate smart rule \"" + ruleString + "\" of user-provided collection with id " + object.getId(), ex);
                        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                    }
                }
            }
        }

        long existingCollectionCount = collectionDao.countByIdIn(ids.toArray(new String[]{}));

        if (existingCollectionCount
                > 0) {
            LOG.debug("There is already a collection for at least one of the following ids: {}.", ids);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        Long existingMemberCount = memberDao.countByMidIn(ids.toArray(new String[]{}));
        if (existingMemberCount
                > 0) {
            LOG.debug("There is already a member at least one of the following ids: {}.", ids);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        LOG.trace(
                "Checking properties and creating collections.");
        content.forEach(collection
                -> {
            doPersistCollection(collection);
//            LOG.trace("Checking collection properties.");
//            CollectionProperties props = collection.getProperties();
//            if (props == null) {
//                LOG.trace("Collection properties not present. Creating new collection properties instance.");
//                props = new CollectionProperties();
//                collection.setProperties(props);
//            }
//
//            LOG.trace("Setting property 'dateCreated' to now().");
//            props.setDateCreated(Instant.now().truncatedTo(ChronoUnit.MILLIS));
//
//            LOG.trace("Checking collection capabilities.");
//            CollectionCapabilities caps = collection.getCapabilities();
//
//            if (caps == null) {
//                LOG.trace("Collection capabilities not present. Creating new default collection capabilities instance.");
//                collection.setCapabilities(CollectionCapabilities.getDefault());
//            }
//
//            LOG.trace("Persisting new collection.");
//            collectionDao.save(collection);
//
//            collectionRegistry.getCollectionGraph().addEdge(collection.getId(), new HashSet<String>());
        }
        );

        LOG.trace(
                "Returning created collections.");
        return new ResponseEntity<>(content, HttpStatus.CREATED);
    }

    private CollectionObject doPersistCollection(CollectionObject collection) {
        LOG.trace("Checking collection properties.");
        CollectionProperties props = collection.getProperties();
        if (props == null) {
            LOG.trace("Collection properties not present. Creating new collection properties instance.");
            props = new CollectionProperties();
            collection.setProperties(props);
        }

        LOG.trace("Setting property 'dateCreated' to now().");
        props.setDateCreated(Instant.now().truncatedTo(ChronoUnit.MILLIS));

        LOG.trace("Checking collection capabilities.");
        CollectionCapabilities caps = collection.getCapabilities();

        if (caps == null) {
            LOG.trace("Collection capabilities not present. Creating new default collection capabilities instance.");
            collection.setCapabilities(CollectionCapabilities.getDefault());
        }

        LOG.trace("Persisting new collection.");
        CollectionObject result = collectionDao.save(collection);

        collectionRegistry.getCollectionGraph().addEdge(result.getId(), new HashSet<String>());
        return result;
    }

    @Override
    public ResponseEntity<CollectionResultSet> collectionsGet(
            @Valid
            @RequestParam(value = "f_modelType", required = false) String fModelType,
            @Valid
            @RequestParam(value = "f_memberType", required = false) String fMemberType,
            @Valid
            @RequestParam(value = "f_ownership", required = false) String fOwnership,
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
        LOG.trace("{}", collectionRegistry.getCollectionGraph().toString());
        return new ResponseEntity<>(resultSet, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<DataWrapper> collectionsGetD3(HttpServletRequest request, UriComponentsBuilder uriBuilder) {
        LOG.trace("Calling collectionsGetD3().");

        DataWrapper wrapper = new DataWrapper();
        List<String> collectionIds = new ArrayList<>();
        List<String> memberIds = new ArrayList<>();
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
                if (!collectionIds.contains(m.getMember().getMid()) && !memberIds.contains(m.getMember().getMid())) {
                    MemberItemNode n_m = new MemberItemNode();
                    n_m.setId(m.getMember().getMid());
                    n_m.setRadius(5);
                    n_m.setDescription(m.getMember().getDescription());
                    n_m.setLocation(m.getMember().getLocation());
                    n_m.setMapping(m.getMappings());
                    wrapper.getNodes().add(n_m);
                    memberIds.add(n_m.getId());
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
            @Valid
            @RequestBody CollectionObject content) {
        //  id= getContentPath("/collections/", null);
        LOG.trace("Calling collectionsIdPut({}, {}).", id, content);

        Optional<CollectionObject> result = collectionDao.findById(id);

        if (result.isEmpty()) {
            LOG.debug("No collection with id {} found.", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        CollectionObject existing = result.get();

        ControllerUtils.checkEtag(request, existing);

        if (null != content.getProperties() && content.getProperties().getSmartRules() != null) {
            for (String ruleString : content.getProperties().getSmartRules()) {
                try {
                    SmartRule.fromString(ruleString);
                } catch (SmartRuleParseException ex) {
                    LOG.error("Failed to evaluate smart rule \"" + ruleString + "\" of user-provided collection with id " + content.getId(), ex);
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            }
        }

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
        String decodedIdentifier = id;//getContentPath("/collections/", null);
        LOG.trace("Calling collectionsIdDelete({}).", decodedIdentifier);
        Optional<CollectionObject> result = collectionDao.findById(decodedIdentifier);

        if (!result.isEmpty()) {
            ControllerUtils.checkEtag(request, result.get());

            Set<Membership> memberships = result.get().getMembers();
            Set<MemberItem> memberItems = new HashSet<>();
            memberships.forEach(membership -> {
                memberItems.add(membership.getMember());
            });
            Set<String> collectionMemberOfs = result.get().getProperties().getMemberOf();
            LOG.trace("Deleting collection with id {}.", decodedIdentifier);
            collectionDao.delete(result.get());

            LOG.trace("Returning HTTP 204.");

            memberItems.forEach(memberItem -> {
                Optional<Membership> membership = membershipDao.findByMember(memberItem);
                Optional<CollectionObject> collection = collectionDao.findById(memberItem.getMid());

                //delete id of the deleted collection from MemberOf
                if (!collection.isEmpty()) {
                    LOG.trace("Deleting the id of the deleted collection {} from MemberOf of the collection with id {}", decodedIdentifier, collection.get().getId());
                    collection.get().getProperties().getMemberOf().remove(decodedIdentifier);
                    collectionDao.save(collection.get());
                }
                //delete memberItem if it has no memberships
                if (membership.isEmpty()) {
                    LOG.trace("Deleting MemberItem with id {} having no membership", memberItem.getMid());
                    memberDao.delete(memberItem);
                    LOG.trace("Returning HTTP 204.");
                }

            });
            //delete the member Item which is a collection id from the parent collection
            Set<MemberItem> memberItemsToDelete = new HashSet<>();
            for (String collectionId : collectionMemberOfs) {
                Optional<CollectionObject> collection = collectionDao.findById(collectionId);
                if (!collection.isEmpty()) {
                    memberships = collection.get().getMembers();
                    for (Membership membershipToDelete : memberships) {
                        MemberItem memberItemToDelete = membershipToDelete.getMember();
                        if (memberItemToDelete.getMid().equals(decodedIdentifier)) {
                            LOG.trace("Deleting MemberItem with id {} from Collection with id {}", decodedIdentifier, collectionId);
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
            collectionRegistry.getCollectionGraph().removeCollection(decodedIdentifier);
        } else {
            LOG.trace("No collection with id {} found. Returning HTTP 204.", decodedIdentifier);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<CollectionCapabilities> collectionsIdCapabilitiesGet(@PathVariable("id") String id) {
        // id = getContentPath("/collections/", "/capabilities");
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
            @Valid
            @RequestBody List<MemberItem> content) {
        LOG.trace("Calling collectionsIdMembersPost({}).", id);

        //validate attributes of MemberItem
//        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
//        Validator validator = factory.getValidator();
//        for (MemberItem item : content) {
//            Set<ConstraintViolation<MemberItem>> constraintViolations = validator.validate(item);
//            if (constraintViolations.size() > 0) {
//                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//            }
//        }
        Optional<CollectionObject> result = collectionDao.findById(id);
        if (result.isEmpty()) {
            LOG.debug("No collection with id {} found.", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        CollectionObject collectionToAddTo = result.get();
        Map<String, MemberItem> existingMembers = new HashMap<>();

        String restrictedToType = null;
        LOG.trace("Checking if update is allowed by collection capabilities.");
        if (collectionToAddTo.getCapabilities() != null) {
            LOG.trace("Check property 'membershipMutable'.");
            if (!collectionToAddTo.getCapabilities().getMembershipIsMutable()) {
                LOG.error("Unable to add members to immutable collection with id {}.", id);
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            LOG.trace("Check property 'maxLength'.");
            if (collectionToAddTo.getCapabilities().getMaxLength() > -1) {
                LOG.trace("Checking collection maxLength property.");
                int membershipCount = collectionToAddTo.getMembers().size();
                if (membershipCount + content.size() >= collectionToAddTo.getCapabilities().getMaxLength()) {
                    LOG.error("Adding {} members to collection would exceed max count {}.", content.size(), collectionToAddTo.getCapabilities().getMaxLength());
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            }
            LOG.trace("Obtaining property 'restrictedToType'.");
            restrictedToType = collectionToAddTo.getCapabilities().getRestrictedToType();
        }

        Set<String> collectionItemIds = new HashSet<>();
        LOG.trace("Checking {} member items for proper type, assigned ids and membership conflicts.");
        for (MemberItem item : content) {
            if (Objects.equals(id, item.getMid())) {
                LOG.error("Unable to add collection {} as member to itself.", id);
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            LOG.trace("Checking member for existing id.");

            if (item.getMid() == null) {
                LOG.trace("Member item has no id, yet. Setting random UUID.");
                item.setMid(UUID.randomUUID().toString());
                LOG.trace("UUID {} assigned as member item id.", item.getId());
            } else {
                try {
                    item.setMid(URLDecoder.decode(item.getMid(), "UTF-8"));
                     /* Remove if tested...should work from v1.3.0
                    if (item.getMid().contains("/")) {
                        LOG.error("Detected slash character in member id {}.", item.getMid());
                        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                    }*/
                } catch (UnsupportedEncodingException ex) {
                    //ignore
                }

                //check if id is collection
                Optional<CollectionObject> optionalCollection = collectionDao.findById(item.getMid());
                if (optionalCollection.isPresent()) {
                    collectionItemIds.add(item.getMid());
                    CollectionObject collection = optionalCollection.get();
                    if (collection.getCapabilities().getRestrictedToType() != null && !collection.getCapabilities().getRestrictedToType().equals(collectionToAddTo.getCapabilities().getRestrictedToType())) {
                        LOG.error("Collection has invalid resctricted Type. Collection with id {} only supports type {}, but member collection provided type {}.", id, collectionToAddTo.getCapabilities().getRestrictedToType(), collection.getCapabilities().getRestrictedToType());
                        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                    }
                    //check if the graph is circular when adding a new member
                    try {
                        collectionRegistry.getCollectionGraph().isCircular(id, item.getMid());
                    } catch (CircularDependencyException e) {
                        LOG.error("Member cannot be added. {}", e.getMessage());
                        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                    }
                    LOG.trace("Provided member with id {} represents an existing collection. Adding parent collection id {} to memberOf property.", item.getMid(), id);
                    collection.getProperties().getMemberOf().add(id);

                    //the collection might have already the other collection as member
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
                        if (restrictedToType != null && !restrictedToType.equals(optionalMember.get().getDatatype())) {
                            LOG.error("Member has invalid type. Collection with id {} only supports type {}, but member provided type {}.", id, restrictedToType, optionalMember.get().getDatatype());
                            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                        }
                        //existing member found, do not persist member, only membership
                        LOG.trace("Existing member found for mid {}.", item.getMid());
                        existingMembers.put(item.getMid(), optionalMember.get());
                    } else if (restrictedToType != null && !restrictedToType.equals(item.getDatatype())) {
                        LOG.error("Member has invalid type. Collection with id {} only supports type {}, but member provided type {}.", id, restrictedToType, item.getDatatype());
                        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                    }
                }

                LOG.trace("Checking for existing membership between collection {} and member item id {}.", id, item.getMid());
                if (new JPAQueryHelper(em).isMemberPartOfCollection(id, item.getMid())) {
                    LOG.error("Existing membership found. Returning HTTP CONFLICT.");
                    return new ResponseEntity<>(HttpStatus.CONFLICT);
                }
            }
        }

        LOG.trace("All member items are fine. Adding {} member(s) to collection {}.", content.size(), id);
        for (MemberItem item : content) {
            Set<String> rules = collectionToAddTo.getProperties().getSmartRules();

            //for every rule:
            //check if rule applies
            //applies: 
            //check if target collection exists
            // exists
            // use target collection
            //not exist:
            //create target collection as sub-collection of adressed collection (mark as rule-created?)
            //add member to target collection and continue
            //not apply: continue
            //if no rule applied: add member to adressed collection
            boolean ruleApplied = false;
            //apply rules only to items, not collections
            if (rules != null && !collectionItemIds.contains(item.getMid())) {
                LOG.trace("Checking smart-rules for member {}.", item.getMid());
                for (String rule : rules) {
                    LOG.trace("Checking smart-rule {}.", rule);
                    try {
                        SmartRule smartRule = SmartRule.fromString(rule);
                        if (smartRule.matches(item)) {
                            LOG.trace("Member matches smart-rule.");
                            ruleApplied = true;
                            String targetCollectionId = smartRule.getTargetCollectionId();
                            LOG.trace("Checking for smart-rule's target collection id {}.", targetCollectionId);
                            Optional<CollectionObject> optTargetCollection = collectionDao.findById(targetCollectionId);
                            CollectionObject targetCollection = null;
                            if (!optTargetCollection.isPresent()) {
                                LOG.trace("Target collection does not exist. Creating new collection as member of collection {}.", collectionToAddTo.getCapabilities());
                                //create target collection as child of collectionToAdd
                                CollectionObject newCollection = new CollectionObject();
                                newCollection.setId(targetCollectionId);
                                newCollection.setProperties(new CollectionProperties());
                                newCollection.getProperties().getMemberOf().add(collectionToAddTo.getId());
                                newCollection.setDescription("Rule-based created collection.");
                                LOG.trace("Persisting new collection with id {}.", targetCollectionId);
                                targetCollection = doPersistCollection(newCollection);
                            } else {
                                LOG.trace("Target collection exists.");
                                targetCollection = optTargetCollection.get();
                            }
                            //target collection exists
                            LOG.trace("Persisting member item with id {} in smart-rule target collection with id {}.", item.getMid(), targetCollectionId);
                            persistItem(item, false, existingMembers, targetCollection);
                            existingMembers.put(item.getMid(), item);
                        }
                    } catch (SmartRuleParseException ex) {
                        LOG.error("Failed to evaluate smart rule for collection " + id + ".", ex);
                        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                }
            }

            if (!ruleApplied) {
                LOG.trace("No smart-rule applied to member with id {}. Adding member to adressed collection with id {}.", item.getMid(), collectionToAddTo.getId());
                persistItem(item, collectionItemIds.contains(item.getMid()), existingMembers, collectionToAddTo);
            }

//            LOG.trace("Obtaining collection item mapping metadata.");
//            CollectionItemMappingMetadata mappingMetadata = item.getMappings();
//            if (mappingMetadata == null) {
//                LOG.trace("No collection item metadata provided. Creating new metadata instance.");
//                mappingMetadata = CollectionItemMappingMetadata.getDefault();
//            }
//
//            MemberItem membershipMember;
//
//            if (!existingMembers.containsKey(item.getMid())) {
//                LOG.trace("Persisting new member item with id {}.", item.getId());
//                item.copyFrom(memberDao.save(item));
//            } else {
//                LOG.trace("Skip persisting existing member with id {}.", item.getId());
//                membershipMember = existingMembers.get(item.getMid());
//                item.copyFrom(membershipMember);
//            }
//
//            Membership m = new Membership();
//            m.setMember(item);
//            m.setMappings(mappingMetadata);
//
//            collectionToAddTo.getMembers().add(m);
//
//            LOG.trace("Persisting new membership between collection {} and member item {}.", id, item.getId());
//            collectionDao.save(collectionToAddTo);
//
//            Optional<CollectionObject> optionalCollection = collectionDao.findById(item.getMid());
//            if (optionalCollection.isPresent()) {
//                collectionRegistry.getCollectionGraph().addEdge(item.getMid(), id);
//            }
//            //set mapping metadata to item in order to make it available in the returned list
//            item.setMappings(mappingMetadata);
        }

        LOG.trace("Returning collection of created member items.");
        return new ResponseEntity<>(content, HttpStatus.CREATED);
    }

    private void persistItem(MemberItem item, boolean isCollection, Map<String, MemberItem> existingMembers, CollectionObject targetCollection) {
        LOG.trace("Obtaining collection item mapping metadata.");
        CollectionItemMappingMetadata mappingMetadata = item.getMappings();
        if (mappingMetadata == null) {
            LOG.trace("No collection item metadata provided. Creating new metadata instance.");
            mappingMetadata = CollectionItemMappingMetadata.getDefault();
        }

        MemberItem membershipMember;

        if (!existingMembers.containsKey(item.getMid())) {
            LOG.trace("Persisting new member item with id {}.", item.getId());
            item.copyFrom(memberDao.save(item));
        } else {
            LOG.trace("Skip persisting existing member with id {}.", item.getId());
            membershipMember = existingMembers.get(item.getMid());
            item.copyFrom(membershipMember);
        }

        Membership m = new Membership();
        m.setMember(item);
        m.setMappings(mappingMetadata);

        targetCollection.getMembers().add(m);

        LOG.trace("Persisting new membership between collection {} and member item {}.", targetCollection.getId(), item.getId());
        collectionDao.save(targetCollection);

        //Optional<CollectionObject> optionalCollection = collectionDao.findById(item.getMid());
        if (isCollection) {
            collectionRegistry.getCollectionGraph().addEdge(item.getMid(), targetCollection.getId());
        }
        //set mapping metadata to item in order to make it available in the returned list
        item.setMappings(mappingMetadata);
    }

    @Override
    public ResponseEntity<MemberResultSet> collectionsIdMembersGet(
            @PathVariable("id") String id,
            @Valid
            @RequestParam(value = "f_datatype", required = false) String fDatatype,
            @Valid
            @RequestParam(value = "f_role", required = false) String fRole,
            @Valid
            @RequestParam(value = "f_index", required = false) Integer fIndex,
            @Valid
            @RequestParam(value = "f_dateAdded", required = false) Instant fDateAdded,
            @Valid
            @RequestParam(value = "expandDepth", required = false) Integer expandDepth,
            final Pageable pgbl) {
        // String path = request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        /* if (path.contains("/collections/") && path.contains("/members")){
            id= getContentPath("/collections/","/members");
        }*/

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

        itemList.stream().map(membership -> {
            //we have to copy the item in case an item is in multiple collections, in that case the same item  pointer is returned multiple times pointing to the same memory location
            MemberItem item = membership.getMember();
            item.setMappings(membership.getMappings());
            return item;
        }).forEachOrdered(item -> {
            resultSet.addContentsItem(item);
        });

        LOG.trace("Obtaining total element count.");
        long totalElementCount = helper.getColletionsMembershipsCountByFilters(collectionIdList, fDatatype, fIndex, fRole, fDateAdded);

        // long totalPages = (totalElementCount > 0) ? (int) Math.rint(totalElementCount / pageSize) + ((totalElementCount % pageSize != 0) ? 1 : 0) : 0;
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
        // id= getContentPath("/collections/","/members/");
        // mid= getContentPath("/members/", null);
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
            @Valid
            @RequestBody MemberItem content) {
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
            mMetadata.setDateUpdated(Instant.now().truncatedTo(ChronoUnit.MILLIS));
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
        //id= getContentPath("/collections/","/members/");
        // mid= getContentPath("/members/", null);
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
            if (membershipDao.findByMember(item).isEmpty()) {
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
        // id= getContentPath("/collections/","/members/");
        // mid= getContentPath("/members/", "/properties/");
        // property= getContentPath("/properties/", null);
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
            @Valid
            @RequestBody String content) {
        // id= getContentPath("/collections/","/members/");
        // mid= getContentPath("/members/", "/properties/");
        // property= getContentPath("/properties/", null);
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
        //id= getContentPath("/collections/","/members/");
        // mid= getContentPath("/members/", "/properties/");
        // property= getContentPath("/properties/", null);
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
        //id= getContentPath("/collections/", "/ops/findMatch");
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
        //id=getContentPath("/collections/", "/ops/flatten");
        LOG.trace("Calling collectionsIdOpsFlattenGet({}, {}).", id, pgbl);
        return collectionsIdMembersGet(id, null, null, null, null, Integer.MAX_VALUE, pgbl);
    }

    @Override
    public ResponseEntity<MemberResultSet> collectionsIdOpsIntersectionOtherIdGet(
            @PathVariable("id") String id,
            @PathVariable("otherId") String otherId,
            final Pageable pgbl) {
        //id=getContentPath("/collections/", "/ops/intersection/");
        // otherId=getContentPath("/ops/intersection/", null);
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
        // id=getContentPath("/collections/", "/ops/union/");
        // otherId= getContentPath("/ops/union/", null);
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

    /*
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
        //id= getContentPath("/collections/", null);  
     */
    private String restorePid(String prefix, String suffix) {
        return prefix + "/" + suffix;
    }

    /**
     * Wrappers for PID Support
     */
    @Override
    public ResponseEntity<CollectionCapabilities> collectionsPidCapabilitiesGet(String prefix, String suffix) {
        return collectionsIdCapabilitiesGet(restorePid(prefix, suffix));
    }

    @Override
    public ResponseEntity<Void> collectionsPidDelete(String prefix, String suffix) {
        return collectionsIdDelete(restorePid(prefix, suffix));
    }

    @Override
    public ResponseEntity<CollectionObject> collectionsPidGet(String prefix, String suffix) {
        return collectionsIdGet(restorePid(prefix, suffix));
    }

    @Override
    public ResponseEntity<MemberResultSet> collectionsPidMembersGet(String prefix, String suffix, String fDatatype, String fRole, Integer fIndex, Instant fDateAdded, Integer expandDepth, Pageable pgbl) {
        return collectionsIdMembersGet(restorePid(prefix, suffix), fDatatype, fRole, fIndex, fDateAdded, expandDepth, pgbl);
    }

    @Override
    public ResponseEntity<Void> collectionsPidMembersMidDelete(String prefix, String suffix, String mid) {
        return collectionsIdMembersMidDelete(restorePid(prefix, suffix), mid);
    }

    @Override
    public ResponseEntity<MemberItem> collectionsPidMembersMidGet(String prefix, String suffix, String mid) {
        return collectionsIdMembersMidGet(restorePid(prefix, suffix), mid);
    }

    @Override
    public ResponseEntity<Void> collectionsPidMembersMidPropertiesPropertyDelete(String prefix, String suffix, String mid, String property) {
        return collectionsIdMembersMidPropertiesPropertyDelete(restorePid(prefix, suffix), mid, property);
    }

    @Override
    public ResponseEntity<String> collectionsPidMembersMidPropertiesPropertyGet(String prefix, String suffix, String mid, String property) {
        return collectionsIdMembersMidPropertiesPropertyGet(restorePid(prefix, suffix), mid, property);
    }

    @Override
    public ResponseEntity<String> collectionsPidMembersMidPropertiesPropertyPut(String prefix, String suffix, String mid, String property, String content) {
        return collectionsIdMembersMidPropertiesPropertyPut(restorePid(prefix, suffix), mid, property, content);
    }

    @Override
    public ResponseEntity<MemberItem> collectionsPidMembersMidPut(String prefix, String suffix, String mid, MemberItem content) {
        return collectionsIdMembersMidPut(restorePid(prefix, suffix), mid, content);
    }

    @Override
    public ResponseEntity<List<MemberItem>> collectionsPidMembersPost(String prefix, String suffix, List<MemberItem> content) {
        return collectionsIdMembersPost(restorePid(prefix, suffix), content);
    }

    @Override
    public ResponseEntity<MemberResultSet> collectionsPidOpsFindMatchPost(String prefix, String suffix, MemberItem memberProperties, Pageable pgbl) {
        return collectionsIdOpsFindMatchPost(restorePid(prefix, suffix), memberProperties, pgbl);
    }

    @Override
    public ResponseEntity<MemberResultSet> collectionsPidOpsFlattenGet(String prefix, String suffix, Pageable pgbl) {
        return collectionsIdOpsFlattenGet(restorePid(prefix, suffix), pgbl);
    }

    @Override
    public ResponseEntity<MemberResultSet> collectionsPidOpsIntersectionOtherIdGet(String prefix, String suffix, String otherId, Pageable pgbl) {
        return collectionsIdOpsIntersectionOtherIdGet(restorePid(prefix, suffix), otherId, pgbl);
    }

    @Override
    public ResponseEntity<MemberResultSet> collectionsIdOpsIntersectionOtherPidGet(String id, String prefix, String suffix, Pageable pgbl) {
        return collectionsIdOpsIntersectionOtherIdGet(id, restorePid(prefix, suffix), pgbl);
    }

    @Override
    public ResponseEntity<MemberResultSet> collectionsPidOpsIntersectionOtherPidGet(String prefix, String suffix, String otherPrefix, String otherSuffix, Pageable pgbl) {
        return collectionsIdOpsIntersectionOtherIdGet(restorePid(prefix, suffix), restorePid(otherPrefix, otherSuffix), pgbl);
    }

    @Override
    public ResponseEntity<MemberResultSet> collectionsPidOpsUnionOtherIdGet(String prefix, String suffix, String otherId, Pageable pgbl) {
        return collectionsIdOpsUnionOtherIdGet(restorePid(prefix, suffix), otherId, pgbl);
    }

    @Override
    public ResponseEntity<MemberResultSet> collectionsIdOpsUnionOtherPidGet(String id, String prefix, String suffix, Pageable pgbl) {
        return collectionsIdOpsUnionOtherIdGet(id, restorePid(prefix, suffix), pgbl);
    }

    @Override
    public ResponseEntity<MemberResultSet> collectionsPidOpsUnionOtherPidGet(String prefix, String suffix, String otherPrefix, String otherSuffix, Pageable pgbl) {
        return collectionsIdOpsUnionOtherIdGet(restorePid(prefix, suffix), restorePid(otherPrefix, otherSuffix), pgbl);
    }

    @Override
    public ResponseEntity<CollectionObject> collectionsPidPut(String prefix, String suffix, CollectionObject content) {
        return collectionsIdPut(restorePid(prefix, suffix), content);
    }

    @Override
    public ResponseEntity<Void> collectionsIdMembersPidDelete(String id, String prefix, String suffix) {
        return collectionsIdMembersMidDelete(id, restorePid(prefix, suffix));
    }

    @Override
    public ResponseEntity<Void> collectionsPidMembersPidDelete(String prefix, String suffix, String mPrefix, String mSuffix) {
        return collectionsIdMembersMidDelete(restorePid(prefix, suffix), restorePid(mPrefix, mSuffix));
    }

    @Override
    public ResponseEntity<MemberItem> collectionsIdMembersPidGet(String id, String prefix, String suffix) {
        return collectionsIdMembersMidGet(id, restorePid(prefix, suffix));
    }

    @Override
    public ResponseEntity<MemberItem> collectionsPidMembersPidGet(String prefix, String suffix, String mPrefix, String mSuffix) {
        return collectionsIdMembersMidGet(restorePid(prefix, suffix), restorePid(mPrefix, mSuffix));
    }

    @Override
    public ResponseEntity<Void> collectionsIdMembersPidPropertiesPropertyDelete(String id, String prefix, String suffix, String property) {
        return collectionsIdMembersMidPropertiesPropertyDelete(id, restorePid(prefix, suffix), property);
    }

    @Override
    public ResponseEntity<Void> collectionsPidMembersPidPropertiesPropertyDelete(String prefix, String suffix, String mPrefix, String mSuffix, String property) {
        return collectionsIdMembersMidPropertiesPropertyDelete(restorePid(prefix, suffix), restorePid(mPrefix, mSuffix), property);
    }

    @Override
    public ResponseEntity<String> collectionsIdMembersPidPropertiesPropertyGet(String id, String prefix, String suffix, String property) {
        return collectionsIdMembersMidPropertiesPropertyGet(id, restorePid(prefix, suffix), property);
    }

    @Override
    public ResponseEntity<String> collectionsPidMembersPidPropertiesPropertyGet(String prefix, String suffix, String mPrefix, String mSuffix, String property) {
        return collectionsIdMembersMidPropertiesPropertyGet(restorePid(prefix, suffix), restorePid(mPrefix, suffix), mSuffix);
    }

    @Override
    public ResponseEntity<String> collectionsIdMembersPidPropertiesPropertyPut(String id, String prefix, String suffix, String property, String content) {
        return collectionsIdMembersMidPropertiesPropertyPut(id, restorePid(prefix, suffix), property, content);
    }

    @Override
    public ResponseEntity<String> collectionsPidMembersPidPropertiesPropertyPut(String prefix, String suffix, String mPrefix, String mSuffix, String property, String content) {
        return collectionsIdMembersMidPropertiesPropertyPut(restorePid(prefix, suffix), restorePid(mPrefix, mSuffix), property, content);
    }

    @Override
    public ResponseEntity<MemberItem> collectionsIdMembersPidPut(String id, String prefix, String suffix, MemberItem content) {
        return collectionsIdMembersMidPut(id, restorePid(prefix, suffix), content);
    }

    @Override
    public ResponseEntity<MemberItem> collectionsPidMembersPidPut(String prefix, String suffix, String mPrefix, String mSuffix, MemberItem content) {
        return collectionsIdMembersMidPut(restorePid(prefix, suffix), restorePid(mPrefix, mSuffix), content);
    }

}
