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
package edu.kit.datamanager.collection.dao;

import edu.kit.datamanager.collection.domain.Membership;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author jejkal
 */
public interface IMembershipDao extends JpaRepository<Membership, String>{

//  public Optional<Membership> findByCollectionIdEqualsAndMemberIdEquals(String collectionId, String memberId);
//
//  public List<Membership> findByCollectionIdIn(List<String> collectionIds);
//  
//  public Long countByCollectionId(String collectionIds);
//
//  public Page<Membership> findByMemberIdIn(List<String> memberIds, Pageable pgbl);
//
//  public List<Membership> findByMemberIdIn(List<String> memberIds);
//
//  public Page<Membership> findByMemberIdInAndCollectionIdIn(List<String> memberIds, List<String> collectionIds, Pageable pgbl);
//
//  public Page<Membership> findByIdInAndMemberIdIn(List<Long> ids, List<String> memberIds, Pageable pgbl);
//
//  public Page<Membership> findByIdInAndCollectionIdIn(List<Long> ids, List<String> collectionIds, Pageable pgbl);
//  
//  
//  
  Long countByIdIn(String... ids);

}
