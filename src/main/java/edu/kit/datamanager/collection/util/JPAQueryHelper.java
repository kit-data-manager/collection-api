/*
 * Copyright 2019 Karlsruhe Institute of Technology.
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
package edu.kit.datamanager.collection.util;

import edu.kit.datamanager.collection.domain.CollectionObject;
import edu.kit.datamanager.collection.domain.MemberItem;
import edu.kit.datamanager.collection.domain.Membership;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

/**
 * Query helper in order to manage complex database queries not possible
 * directly the the different Dao components.
 *
 * @author jejkal
 */
public class JPAQueryHelper{

  private final EntityManager entityManager;

  /**
   * Default constructor.
   *
   * @param entityManager The entityManager in order to perform queries.
   */
  public JPAQueryHelper(EntityManager entityManager){
    this.entityManager = entityManager;
  }

  /**
   * Returns a list of collections matching the provided collection properties
   * and containing at least one member with the provided memberType. For the
   * database query, a collection is selected if (properties.ownership OR
   * properties.modelType) AND members.member.modelType are matching. If one
   * property is null it is omitted from the query. offset and maxResults allow
   * to return results pagewise. If no property is provided, e.g. all are null,
   * all collections are returned pagewise.
   *
   * @param ownership The ownership property of the collection.
   * @param modelType The modelType property of the collection.
   * @param memberType The memberType property of any member item.
   * @param offset The first index of the result list.
   * @param maxResults The max. number of results returned.
   *
   * @return A list of collection objects.
   */
  public List<CollectionObject> getCollectionsByFilters(String ownership, String modelType, String memberType, int offset, int maxResults){
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<CollectionObject> query = builder.createQuery(CollectionObject.class);

    Root<CollectionObject> fromCollections = query.from(CollectionObject.class);

    Predicate memberPredicate = null;
    Predicate collectionPredicate = null;

    if(memberType != null){
      Join<CollectionObject, Membership> collectionMember = fromCollections.join("members");
      memberPredicate = builder.equal(collectionMember.get("member").get("datatype"), memberType);
    }

    if(ownership != null && modelType != null){
      collectionPredicate = builder.or(builder.equal(fromCollections.get("properties").get("ownership"), ownership),
              builder.equal(fromCollections.get("properties").get("modelType"), modelType));
    } else if(ownership != null && modelType == null){
      collectionPredicate = builder.equal(fromCollections.get("properties").get("ownership"), ownership);
    } else if(ownership == null && modelType != null){
      collectionPredicate = builder.equal(fromCollections.get("properties").get("modelType"), modelType);
    }

    TypedQuery<CollectionObject> typedQuery;

    if(memberPredicate == null && collectionPredicate == null){
      typedQuery = entityManager.createQuery(query.select(fromCollections));
    } else if(memberPredicate != null && collectionPredicate == null){
      typedQuery = entityManager.createQuery(query.select(fromCollections).where(memberPredicate));
    } else if(memberPredicate == null && collectionPredicate != null){
      typedQuery = entityManager.createQuery(query.select(fromCollections).where(collectionPredicate));
    } else{
      typedQuery = entityManager.createQuery(query.select(fromCollections).where(builder.and(memberPredicate, collectionPredicate)));
    }

    return typedQuery.setFirstResult(offset).setMaxResults(maxResults).getResultList();
  }

  /**
   * The count query for {@link #getCollectionsByFilters(java.lang.String, java.lang.String, java.lang.String, int, int)
   * } for pagination support.
   *
   * @param ownership The ownership property of the collection.
   * @param modelType The modelType property of the collection.
   * @param memberType The memberType property of any member item.
   *
   * @return The total number of elements matching the query.
   */
  public Long getCollectionsCountByFilters(String ownership, String modelType, String memberType){
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> query = builder.createQuery(Long.class);

    Root<CollectionObject> fromCollections = query.from(CollectionObject.class);

    Predicate memberPredicate = null;
    Predicate collectionPredicate = null;
    if(memberType != null){
      Join<CollectionObject, Membership> collectionMember = fromCollections.join("members");
      memberPredicate = builder.equal(collectionMember.get("member").get("datatype"), memberType);
    }

    if(ownership != null && modelType != null){
      collectionPredicate = builder.or(builder.equal(fromCollections.get("properties").get("ownership"), ownership),
              builder.equal(fromCollections.get("properties").get("modelType"), modelType));
    } else if(ownership != null && modelType == null){
      collectionPredicate = builder.equal(fromCollections.get("properties").get("ownership"), ownership);
    } else if(ownership == null && modelType != null){
      collectionPredicate = builder.equal(fromCollections.get("properties").get("modelType"), modelType);
    }

    TypedQuery<Long> typedQuery;

    if(memberPredicate == null && collectionPredicate == null){
      typedQuery = entityManager.createQuery(query.select(builder.count(fromCollections)));
    } else if(memberPredicate != null && collectionPredicate == null){
      typedQuery = entityManager.createQuery(query.select(builder.count(fromCollections)).where(memberPredicate));
    } else if(memberPredicate == null && collectionPredicate != null){
      typedQuery = entityManager.createQuery(query.select(builder.count(fromCollections)).where(collectionPredicate));
    } else{
      typedQuery = entityManager.createQuery(query.select(builder.count(fromCollections)).where(builder.and(memberPredicate, collectionPredicate)));
    }

    return typedQuery.getSingleResult();
  }

  /**
   * Returns a list of memberships matching the provided membership properties
   * and containing at least one member with the provided memberType. For the
   * database query, a membership is selected if (mappings.index OR
   * mappings.role OR mappings.dateAdded) AND member.modelType are matching. If
   * one property is null it is omitted from the query. offset and maxResults
   * allow to return results pagewise. If no property is provided, e.g. all are
   * null, all collections are returned pagewise.
   *
   * @param collectionIds A list of collectionsIds to obtain all members from.
   * @param memberType The memberType property of any member item.
   * @param index The index of the member in the collection.
   * @param role The role of the member in the collection.
   * @param dateAdded The date at which the member was added to the collection.
   * @param ordered TRUE if the collection is ordered, FALSE otherwise.
   * @param offset The first index of the result list.
   * @param maxResults The max. number of results returned.
   *
   * @return A list of memberships.
   */
  public List<Membership> getColletionsMembershipsByFilters(List<String> collectionIds, String memberType, Integer index, String role, Instant dateAdded, boolean ordered, int offset, int maxResults){
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Membership> query = builder.createQuery(Membership.class);

    Root<CollectionObject> fromCollections = query.from(CollectionObject.class);

    Join<CollectionObject, Membership> collectionMember = fromCollections.join("members");

    Predicate here = fromCollections.get("id").in(collectionIds);

    if(memberType != null){
      here = builder.and(here, builder.equal(collectionMember.get("member").get("datatype"), memberType));
    }

    List<Predicate> mappingPredicated = new ArrayList<>();

    if(index != null){
      mappingPredicated.add(builder.equal(collectionMember.get("mappings").get("index"), index));
    }
    if(role != null){
      mappingPredicated.add(builder.equal(collectionMember.get("mappings").get("role"), role));
    }
    if(dateAdded != null){
      mappingPredicated.add(builder.equal(collectionMember.get("mappings").get("dateAdded"), dateAdded));
    }

    if(!mappingPredicated.isEmpty()){
      here = builder.and(here, builder.or(mappingPredicated.toArray(new Predicate[]{})));
    }
    TypedQuery<Membership> typedQuery;
    if(ordered){
      typedQuery = entityManager.createQuery(query
              .select(collectionMember)
              .where(here).orderBy(builder.asc(collectionMember.get("mappings").get("index")))
      );
    } else{
      typedQuery = entityManager.createQuery(query
              .select(collectionMember)
              .where(here));
    }

    if(offset < 0){
      return typedQuery.getResultList();
    }

    return typedQuery.setFirstResult(offset).setMaxResults(maxResults).getResultList();
  }

  /**
   * A helper method calling
   * {@link #getColletionsMembershipsByFilters(java.lang.String, java.lang.String, java.lang.Integer, java.lang.String, java.time.Instant, int, int)}
   * without offset and maxResults.
   *
   * @param collectionIds A list of collectionsIds to obtain all members from.
   * @param memberType The memberType property of any member item.
   * @param index The index of the member in the collection.
   * @param role The role of the member in the collection.
   * @param dateAdded The date at which the member was added to the collection.
   * @param ordered TRUE if the collection is ordered, FALSE otherwise.
   *
   * @return A list of memberships.
   */
  public List<Membership> getColletionsMembershipsByFilters(List<String> collectionIds, String memberType, Integer index, String role, Instant dateAdded, boolean ordered){
    return getColletionsMembershipsByFilters(collectionIds, memberType, index, role, dateAdded, ordered, -1, -1);
  }

  /**
   * The count query for
   * {@link #getColletionsMembershipsByFilters(java.lang.String, java.lang.String, java.lang.Integer, java.lang.String, java.time.Instant, int, int)}
   * for pagination support.
   *
   * @param collectionIds A list of collectionsIds to obtain all members from.
   * @param memberType The memberType property of any member item.
   * @param index The index of the member in the collection.
   * @param role The role of the member in the collection.
   * @param dateAdded The date at which the member was added to the collection.
   *
   * @return The total number of elements matching the query.
   */
  public Long getColletionsMembershipsCountByFilters(List<String> collectionIds, String memberType, Integer index, String role, Instant dateAdded){
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> query = builder.createQuery(Long.class);

    Root<CollectionObject> fromCollections = query.from(CollectionObject.class);

    Join<CollectionObject, Membership> collectionMember = fromCollections.join("members");

    Predicate here = fromCollections.get("id").in(collectionIds);

    if(memberType != null){
      here = builder.and(here, builder.equal(collectionMember.get("member").get("datatype"), memberType));
    }

    List<Predicate> mappingPredicated = new ArrayList<>();

    if(index != null){
      mappingPredicated.add(builder.equal(collectionMember.get("mappings").get("index"), index));
    }
    if(role != null){
      mappingPredicated.add(builder.equal(collectionMember.get("mappings").get("role"), role));
    }
    if(dateAdded != null){
      mappingPredicated.add(builder.equal(collectionMember.get("mappings").get("dateAdded"), dateAdded));
    }

    if(!mappingPredicated.isEmpty()){
      here = builder.and(here, builder.or(mappingPredicated.toArray(new Predicate[]{})));
    }

    TypedQuery<Long> typedQuery = entityManager.createQuery(query
            .select(builder.countDistinct(collectionMember.get("member")))
            .where(here).distinct(true)
    );

    return typedQuery.getSingleResult();
  }

  /**
   * Find a list of collection members matching the assigned attributed of the
   * provided member item.
   *
   * @param collectionId The id of the parent collection.
   * @param member The member example providing all attributes to search for.
   * @param ordered TRUE if the collection is ordered.
   * @param offset The first index of the result list.
   * @param maxResults The max. number of results returned.
   *
   * @return A list of memberships.
   */
  public List<Membership> findCollectionMembershipsByExample(String collectionId, MemberItem member, boolean ordered, int offset, int maxResults){
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Membership> query = builder.createQuery(Membership.class);

    Root<CollectionObject> fromCollections = query.from(CollectionObject.class);

    Join<CollectionObject, Membership> collectionMember = fromCollections.join("members");

    Predicate here = fromCollections.get("id").in(collectionId);

    if(member.getDatatype() != null){
      here = builder.and(here, builder.equal(collectionMember.get("member").get("datatype"), member.getDatatype()));
    }
    if(member.getDescription() != null){
      here = builder.and(here, builder.equal(collectionMember.get("member").get("description"), member.getDescription()));
    }
    if(member.getLocation() != null){
      here = builder.and(here, builder.equal(collectionMember.get("member").get("location"), member.getLocation()));
    }
    if(member.getMid() != null){
      here = builder.and(here, builder.equal(collectionMember.get("member").get("mid"), member.getMid()));
    }
    if(member.getOntology() != null){
      here = builder.and(here, builder.equal(collectionMember.get("member").get("ontology"), member.getOntology()));
    }

    TypedQuery<Membership> typedQuery;
    if(ordered){
      typedQuery = entityManager.createQuery(query
              .select(collectionMember)
              .where(here).orderBy(builder.asc(collectionMember.get("mappings").get("index")))
      );
    } else{
      typedQuery = entityManager.createQuery(query
              .select(collectionMember)
              .where(here));
    }

    if(offset < 0){
      return typedQuery.getResultList();
    }

    return typedQuery.setFirstResult(offset).setMaxResults(maxResults).getResultList();

  }

  /**
   * The count query for {@link #findCollectionMembershipsByExample(java.lang.String, edu.kit.datamanager.collection.domain.MemberItem, boolean, int, int)
   * }.
   *
   * @param collectionId The id of the parent collection.
   * @param member The member example providing all attributes to search for.
   *
   * @return The number of matching memberships.
   */
  public Long findCollectionMembershipsCountByExample(String collectionId, MemberItem member){
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> query = builder.createQuery(Long.class);

    Root<CollectionObject> fromCollections = query.from(CollectionObject.class);

    Join<CollectionObject, Membership> collectionMember = fromCollections.join("members");

    Predicate here = fromCollections.get("id").in(collectionId);

    if(member.getDatatype() != null){
      here = builder.and(here, builder.equal(collectionMember.get("member").get("datatype"), member.getDatatype()));
    }
    if(member.getDescription() != null){
      here = builder.and(here, builder.equal(collectionMember.get("member").get("description"), member.getDescription()));
    }
    if(member.getLocation() != null){
      here = builder.and(here, builder.equal(collectionMember.get("member").get("location"), member.getLocation()));
    }
    if(member.getMid() != null){
      here = builder.and(here, builder.equal(collectionMember.get("member").get("mid"), member.getMid()));
    }
    if(member.getOntology() != null){
      here = builder.and(here, builder.equal(collectionMember.get("member").get("ontology"), member.getOntology()));
    }

    TypedQuery<Long> typedQuery = entityManager.createQuery(query
            .select(builder.countDistinct(collectionMember))
            .where(here));

    return typedQuery.getSingleResult();
  }

  /**
   * Get the intersection of members of collections with id collectionId and
   * otherCollectionId.
   *
   * @param collectionId The id of the one collection.
   * @param otherCollectionId The id of the other collection.
   * @param ordered TRUE if the collection is ordered.
   * @param offset The first index of the result list.
   * @param maxResults The max. number of results returned.
   *
   * @return A list of memberships.
   */
  public List<Membership> getCollectionIntersection(String collectionId, String otherCollectionId, boolean ordered, int offset, int maxResults){
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Membership> query = builder.createQuery(Membership.class);

    Root<CollectionObject> left = query.from(CollectionObject.class);
    Root<CollectionObject> right = query.from(CollectionObject.class);
    Predicate matchLeftId = builder.equal(left.get("id"), collectionId);
    Predicate matchRightId = builder.equal(right.get("id"), otherCollectionId);

    Join<CollectionObject, Membership> leftJoin = left.join("members");
    Join<CollectionObject, Membership> rightJoin = right.join("members");

    TypedQuery<Membership> typedQuery;
    if(ordered){
      typedQuery = entityManager.createQuery(query
              .select(leftJoin)
              .where(builder.and(matchLeftId, matchRightId, builder.equal(leftJoin.get("member").get("mid"), rightJoin.get("member").get("mid")))).orderBy(builder.asc(leftJoin.get("mappings").get("index")))
      );
    } else{
      typedQuery = entityManager.createQuery(query
              .select(leftJoin)
              .where(builder.and(matchLeftId, matchRightId, builder.equal(leftJoin.get("member").get("mid"), rightJoin.get("member").get("mid"))))
      );
    }

    return typedQuery.setFirstResult(offset).setMaxResults(maxResults).getResultList();
  }

  /**
   * The count query for {@link #getCollectionIntersection(java.lang.String, java.lang.String, int, int)
   * }.
   *
   * @param collectionId The id of the one collection.
   * @param otherCollectionId The id of the other collection.
   *
   * @return The number of matching memberships.
   */
  public Long getCollectionIntersectionCount(String collectionId, String otherCollectionId){
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> query = builder.createQuery(Long.class);

    Root<CollectionObject> left = query.from(CollectionObject.class);
    Root<CollectionObject> right = query.from(CollectionObject.class);
    Predicate matchLeftId = builder.equal(left.get("id"), collectionId);
    Predicate matchRightId = builder.equal(right.get("id"), otherCollectionId);

    Join<CollectionObject, Membership> leftJoin = left.join("members");
    Join<CollectionObject, Membership> rightJoin = right.join("members");

    TypedQuery<Long> typedQuery = entityManager.createQuery(query
            .select(builder.countDistinct(leftJoin))
            .where(builder.and(matchLeftId, matchRightId, builder.equal(leftJoin.get("member").get("mid"), rightJoin.get("member").get("mid")))).orderBy(builder.asc(leftJoin.get("mappings").get("index")))
    );

    return typedQuery.getSingleResult();
  }

  /**
   * Get a single membership of a member identified by its memberId in a
   * collection identified by its collectionId.
   *
   * @param collectionId The collection id.
   * @param memberId The member item id.
   *
   * @return An optional membership which might be empty.
   */
  public Optional<Membership> getMembershipByMid(String collectionId, String memberId){
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Membership> query = builder.createQuery(Membership.class);

    Root<CollectionObject> fromCollections = query.from(CollectionObject.class);

    Join<CollectionObject, Membership> collectionMember = fromCollections.join("members");

    TypedQuery<Membership> typedQuery = entityManager.createQuery(query
            .select(collectionMember)
            .where(builder.and(builder.equal(fromCollections.get("id"), collectionId), builder.equal(collectionMember.get("member").get("mid"), memberId))).distinct(true)
    );
    try{
      return Optional.of(typedQuery.getSingleResult());
    } catch(NoResultException ex){
      return Optional.empty();
    }
  }

  /**
   * Check if a member identifier by its memberId is part of the collection
   * identified by its collectionId.
   *
   * @param collectionId The collection id.
   * @param memberId The member item id.
   *
   * @return TRUE if the member is part of the collection, FALSE otherwise.
   */
  public boolean isMemberPartOfCollection(String collectionId, String memberId){
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> query = builder.createQuery(Long.class);

    Root<CollectionObject> fromCollections = query.from(CollectionObject.class);

    Join<CollectionObject, Membership> collectionMember = fromCollections.join("members");

    TypedQuery<Long> typedQuery = entityManager.createQuery(query
            .select(builder.count(collectionMember.get("member")))
            .where(builder.and(builder.equal(fromCollections.get("id"), collectionId), builder.equal(collectionMember.get("member").get("mid"), memberId))).distinct(true)
    );

    return typedQuery.getSingleResult() > 0;
  }

  /**
   * Get all sub collections of the provided collection id up to the provided
   * expand depth. If expand depth less than 1, an empty set is returned.
   * Otherwise, all child collections will be obtained recursively up to the
   * maximum depth of 'expandDepth'. Due to holding the result in a set, all
   * elements in the result set will be unique. Note that the final result does
   * not necessarily contain the provided collectionId. Basically, the only
   * situation where it is part of the result set is, if it is a child
   * collection of an own child or one of their children.
   *
   * @param collectionId The id of the upper most collection.
   * @param expandDepth The depth up to which child collections are determined.
   *
   * @return A set of collection ids.
   *
   */
  public Set<String> getSubCollections(String collectionId, int expandDepth){
    Set<String> result = new HashSet<>();
    if(expandDepth > 0){
      getSubCollection(collectionId, result, expandDepth);
    }
    return result;
  }

  /**
   * Helper method in order to implement recursion through all collections.
   *
   * @param collectionId The id of the upper most collection.
   * @param expandDepth The depth up to which child collections are determined.
   * @param resultList List containing all results of all iterations.
   *
   */
  private void getSubCollection(String collectionId, Set<String> resultList, int expandDepth){
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<String> query = builder.createQuery(String.class);

    Root<CollectionObject> fromCollections = query.from(CollectionObject.class);

    TypedQuery<String> typedQuery = entityManager.createQuery(query
            .select(fromCollections.get("id"))
            .where(builder.isMember(collectionId, fromCollections.get("properties").get("memberOf"))));

    List<String> subCollections = typedQuery.getResultList();

    expandDepth -= 1;

    if(!subCollections.isEmpty() && expandDepth > 0){
      for(String item : subCollections){
        getSubCollection(item, resultList, expandDepth);
      }
    }
    resultList.addAll(typedQuery.getResultList());
  }

}
