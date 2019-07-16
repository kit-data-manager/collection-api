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
package edu.kit.datamanager.collection.test.util;

import edu.kit.datamanager.collection.dao.ICollectionObjectDao;
import edu.kit.datamanager.collection.dao.IMemberItemDao;
import edu.kit.datamanager.collection.dao.IMembershipDao;
import edu.kit.datamanager.collection.domain.CollectionCapabilities;
import edu.kit.datamanager.collection.domain.CollectionItemMappingMetadata;
import edu.kit.datamanager.collection.domain.CollectionObject;
import edu.kit.datamanager.collection.domain.CollectionProperties;
import edu.kit.datamanager.collection.domain.MemberItem;
import edu.kit.datamanager.collection.domain.Membership;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import net.minidev.json.writer.CollectionMapper;

/**
 *
 * @author jejkal
 */
public class TestDataCreationHelper{

  private IMemberItemDao memberDao;
  private ICollectionObjectDao collectionDao;
  private IMembershipDao membershipDao;

  Map<String, CollectionObject> collections = new HashMap<>();
  Map<String, MemberItem> members = new HashMap<>();
  List<Membership> memberships = new ArrayList<>();

  public static TestDataCreationHelper initialize(IMemberItemDao memberDao, ICollectionObjectDao collectionDao, IMembershipDao membershipDao){
    TestDataCreationHelper helper = new TestDataCreationHelper();
    helper.memberDao = memberDao;
    helper.collectionDao = collectionDao;
    helper.membershipDao = membershipDao;
    return helper;
  }

  public TestDataCreationHelper addCollection(String id, String description, CollectionCapabilities caps, CollectionProperties props){
    CollectionObject o = new CollectionObject();
    o.setId(id);
    o.setDescription(description);
    o.setCapabilities(caps);
    o.setProperties(props);
    collections.put(id, o);
    return this;
  }

  public TestDataCreationHelper addCollection(String id, String description, CollectionProperties props){
    return this.addCollection(id, description, CollectionCapabilities.getDefault(), props);
  }

  public TestDataCreationHelper addCollection(String id, CollectionProperties props){
    return this.addCollection(id, null, CollectionCapabilities.getDefault(), props);
  }

  public TestDataCreationHelper addMemberItem(String id, String description, String dataType, String location, String ontology, CollectionItemMappingMetadata mappings){
    MemberItem o = new MemberItem();
    o.setId(id);
    o.setDescription(description);
    o.setDatatype(dataType);
    o.setLocation(location);
    o.setOntology(ontology);
    o.setMappings(mappings);
    members.put(id, o);
    return this;
  }

  public TestDataCreationHelper addMemberItem(String id, String description, String dataType, String location, String ontology){
    return this.addMemberItem(id, description, dataType, location, ontology, null);
  }

  public TestDataCreationHelper addMemberItem(String id, String description, String dataType, String location){
    return this.addMemberItem(id, description, dataType, location, null, null);
  }

  public TestDataCreationHelper addMemberItem(String id, String location){
    return this.addMemberItem(id, null, null, location, null, null);
  }

  public TestDataCreationHelper addMemberItem(String id, String dataType, String location){
    return this.addMemberItem(id, null, dataType, location, null, null);
  }

  public TestDataCreationHelper addMembership(String collectionId, String memberId, CollectionItemMappingMetadata metadata){
    Membership m = new Membership();
    m.setCollection(collections.get(collectionId));
    m.setMember(members.get(memberId));
    m.setMappings(metadata);
    memberships.add(m);
    return this;
  }

  public TestDataCreationHelper addMembership(String collectionId, String memberId){
    return this.addMembership(collectionId, memberId, new CollectionItemMappingMetadata());
  }

  public TestDataCreationHelper addCollectionToCollection(String parentCollectionId, String childCollectionId){
    CollectionObject child = collections.get(childCollectionId);
    if(child.getProperties() == null){
      child.setProperties(CollectionProperties.getDefault());
    }
    child.getProperties().addMemberOfItem(parentCollectionId);
    return this;
  }

  public void persist(){
    Set<Entry<String, CollectionObject>> collectionEntries = collections.entrySet();
    collectionEntries.forEach((entry) -> {
      collectionDao.save(entry.getValue());
    });
    Set<Entry<String, MemberItem>> memberEntries = members.entrySet();
    memberEntries.forEach((entry) -> {
      memberDao.save(entry.getValue());
    });

    memberships.forEach((membership) -> {
      membershipDao.save(membership);
    });
  }
}
