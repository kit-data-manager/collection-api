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
package edu.kit.datamanager.collection.dao.spec;

import edu.kit.datamanager.collection.domain.CollectionItemMappingMetadata;
import edu.kit.datamanager.collection.domain.CollectionObject;
import edu.kit.datamanager.collection.domain.CollectionProperties;
import edu.kit.datamanager.collection.domain.MemberItem;
import edu.kit.datamanager.collection.domain.Membership;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 *
 * @author jejkal
 */
public class MembershipQueryHelper{

  private final EntityManager entityManager;

  public MembershipQueryHelper(EntityManager entityManager){
    this.entityManager = entityManager;
  }

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
