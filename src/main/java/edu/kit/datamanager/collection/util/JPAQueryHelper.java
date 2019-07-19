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

import edu.kit.datamanager.collection.domain.CollectionItemMappingMetadata;
import edu.kit.datamanager.collection.domain.CollectionObject;
import edu.kit.datamanager.collection.domain.CollectionProperties;
import edu.kit.datamanager.collection.domain.MemberItem;
import edu.kit.datamanager.collection.domain.Membership;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 *
 * @author jejkal
 */
public class JPAQueryHelper{

  private final EntityManager entityManager;

  public JPAQueryHelper(EntityManager entityManager){
    this.entityManager = entityManager;
  }

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

  public List<Membership> getColletionsMembershipsByFilters(String collectionId, String memberType, Integer index, String role, Instant dateAdded, int offset, int maxResults){
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Membership> query = builder.createQuery(Membership.class);

    Root<CollectionObject> fromCollections = query.from(CollectionObject.class);

    Join<CollectionObject, Membership> collectionMember = fromCollections.join("members");

    Predicate here = builder.equal(fromCollections.get("id"), collectionId);

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

    TypedQuery<Membership> typedQuery = entityManager.createQuery(query
            .select(collectionMember)
            .where(here).distinct(true)
    );

    if(offset < 0){
      return typedQuery.getResultList();
    }

    return typedQuery.setFirstResult(offset).setMaxResults(maxResults).getResultList();
  }

  public List<Membership> getColletionsMembershipsByFilters(String collectionId, String memberType, Integer index, String role, Instant dateAdded){
    return getColletionsMembershipsByFilters(collectionId, memberType, index, role, dateAdded, -1, -1);
  }

  public Long getColletionsMembershipsCountByFilters(String collectionId, String memberType, Integer index, String role, Instant dateAdded){
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> query = builder.createQuery(Long.class);

    Root<CollectionObject> fromCollections = query.from(CollectionObject.class);

    Join<CollectionObject, Membership> collectionMember = fromCollections.join("members");

    Predicate here = builder.equal(fromCollections.get("id"), collectionId);

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

  //////////
  public List<String> getCollectionsIdsByFilter(List<String> memberIds, List<String> collectionIds, int offset, int maxResults){
    if(memberIds == null || memberIds.isEmpty() || collectionIds == null || collectionIds.isEmpty()){
      return new ArrayList<>();
    }
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<String> query = builder.createQuery(String.class);

    Root<Membership> fromMemberships = query.from(Membership.class);

    Join<Membership, CollectionObject> collectionMember = fromMemberships.join("collection");
    Join<Membership, MemberItem> memberMember = fromMemberships.join("member");
    TypedQuery<String> typedQuery = entityManager.createQuery(query
            .select(fromMemberships.get("collection").get("id"))
            .where(builder.and(
                    collectionMember.get("id").in(collectionIds),
                    memberMember.get("id").in(memberIds))
            ).distinct(true)
    );

    return typedQuery.setFirstResult(offset).setMaxResults(maxResults).getResultList();
  }

  public List<Membership> getMemberIdsByFilter(List<String> memberIds, List<String> collectionIds, String role, Integer index, Instant dateAdded, int offset, int maxResults){
    if(collectionIds == null || collectionIds.isEmpty()){
      return new ArrayList<>();
    }
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Membership> query = builder.createQuery(Membership.class);

    Root<Membership> fromMemberships = query.from(Membership.class);

    Join<Membership, CollectionObject> collectionMember = fromMemberships.join("collection");
    Join<Membership, CollectionItemMappingMetadata> itemMetadata = fromMemberships.join("mappings");
    Join<Membership, MemberItem> memberMember = fromMemberships.join("member");

    Predicate p;
    if(memberIds.isEmpty()){
      p = collectionMember.get("id").in(collectionIds);
    } else{
      p = builder.and(collectionMember.get("id").in(collectionIds), memberMember.get("id").in(memberIds));
    }

    if(role != null){
      p = builder.and(p, builder.equal(itemMetadata.get("role"), role));
    }

    if(index != null){
      p = builder.and(p, builder.equal(itemMetadata.get("index"), index));
    }

    if(dateAdded != null){
      p = builder.and(p, builder.equal(itemMetadata.get("dateAdded"), dateAdded));
    }

    TypedQuery<Membership> typedQuery = entityManager.createQuery(query
            .select(fromMemberships.alias("membership"))
            .where(p)
            .distinct(true).orderBy(builder.asc(fromMemberships.get("mappings").get("index")))
    );

    return typedQuery.setFirstResult(offset).setMaxResults(maxResults).getResultList();
  }

  public Long getCollectionsIdsCountByFilter(List<String> memberIds, List<String> collectionIds){
    if(memberIds == null || memberIds.isEmpty() || collectionIds == null || collectionIds.isEmpty()){
      return 0l;
    }
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> query = builder.createQuery(Long.class);

    Root<Membership> fromMemberships = query.from(Membership.class);

    Join<Membership, CollectionObject> collectionMember = fromMemberships.join("collection");
    Join<Membership, MemberItem> memberMember = fromMemberships.join("member");
    TypedQuery<Long> typedQuery = entityManager.createQuery(query
            .select(builder.countDistinct(fromMemberships.get("collection").get("id")))
            .where(builder.and(
                    collectionMember.get("id").in(collectionIds),
                    memberMember.get("id").in(memberIds))
            ).distinct(true)
    );

    return typedQuery.getSingleResult();
  }

  public Long getMemberIdsCountByFilter(List<String> memberIds, List<String> collectionIds, String role, Integer index, Instant dateAdded, int offset, int maxResults){
    if(collectionIds == null || collectionIds.isEmpty()){
      return 0l;
    }
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> query = builder.createQuery(Long.class);

    Root<Membership> fromMemberships = query.from(Membership.class);

    Join<Membership, CollectionObject> collectionMember = fromMemberships.join("collection");
    Join<Membership, CollectionItemMappingMetadata> itemMetadata = fromMemberships.join("mappings");
    Join<Membership, MemberItem> memberMember = fromMemberships.join("member");

    Predicate p;
    if(memberIds.isEmpty()){
      p = collectionMember.get("id").in(collectionIds);
    } else{
      p = builder.and(collectionMember.get("id").in(collectionIds), memberMember.get("id").in(memberIds));

    }

    if(role != null){
      p = builder.and(p, builder.equal(itemMetadata.get("role"), role));
    }

    if(index != null){
      p = builder.and(p, builder.equal(itemMetadata.get("index"), index));
    }

    if(dateAdded != null){
      p = builder.and(p, builder.equal(itemMetadata.get("dateAdded"), dateAdded));

    }

    TypedQuery<Long> typedQuery = entityManager.createQuery(query
            .select(builder.countDistinct(fromMemberships.get("member").get("id")))
            .where(p)
            .distinct(true)
    );

    return typedQuery.getSingleResult();
  }

  public List<CollectionObject> getMemberCollections(String collectionId, int maxDepth){
    return getMemberCollections(collectionId, new ArrayList<>(), 0, maxDepth);
  }

  private List<CollectionObject> getMemberCollections(String collectionId, List<CollectionObject> currentCollectionList, int currentDepth, int maxDepth){
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<CollectionObject> query = builder.createQuery(CollectionObject.class);

    Root<CollectionObject> fromCollections = query.from(CollectionObject.class);

    Join<CollectionObject, CollectionProperties> collectionMember = fromCollections.join("properties");

    TypedQuery<CollectionObject> typedQuery = entityManager.createQuery(query
            .select(fromCollections)
            .where(builder.isMember(collectionId, collectionMember.get("memberOf"))));

    List<CollectionObject> objects = typedQuery.getResultList();

    objects.forEach((o) -> {
      currentCollectionList.add(o);
    });

    currentDepth++;
    if(currentDepth <= maxDepth && !objects.isEmpty()){
      for(CollectionObject o : objects){
        getMemberCollections(o.getId(), currentCollectionList, currentDepth, maxDepth);
      }

    }

    return currentCollectionList;
  }

}
