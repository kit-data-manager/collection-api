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

import edu.kit.datamanager.collection.dao.ICollectionObjectDao;
import edu.kit.datamanager.collection.dao.IMemberItemDao;
import edu.kit.datamanager.collection.domain.CollectionCapabilities;
import edu.kit.datamanager.collection.domain.CollectionItemMappingMetadata;
import edu.kit.datamanager.collection.domain.CollectionObject;
import edu.kit.datamanager.collection.domain.CollectionProperties;
import edu.kit.datamanager.collection.domain.MemberItem;
import edu.kit.datamanager.collection.domain.Membership;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author jejkal
 */
public class TestDataCreationHelper{

  private ICollectionObjectDao collectionDao;
  private IMemberItemDao memberDao;

  Map<String, CollectionObject> collections = new HashMap<>();
  Map<String, MemberItem> members = new HashMap<>();

  public static TestDataCreationHelper initialize(ICollectionObjectDao collectionDao, IMemberItemDao memberDao){
    TestDataCreationHelper helper = new TestDataCreationHelper();
    helper.collectionDao = collectionDao;
    helper.memberDao = memberDao;
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

  public TestDataCreationHelper addMemberItem(String collectionId, String id, String description, String dataType, String location, String ontology, CollectionItemMappingMetadata mappings){
    MemberItem o = new MemberItem();
    o.setMid(id);
    o.setDescription(description);
    o.setDatatype(dataType);
    o.setLocation(location);
    o.setOntology(ontology);
    o.setMappings(mappings);
    Membership m = new Membership();
    m.setMember(o);
    m.setMappings(mappings);
    members.put(id, o);
    collections.get(collectionId).getMembers().add(m);

    return this;
  }

  public TestDataCreationHelper addMemberItem(String collectionId, String id, String description, String dataType, String location, String ontology){
    return this.addMemberItem(collectionId, id, description, dataType, location, ontology, null);
  }

  public TestDataCreationHelper addMemberItem(String collectionId, String id, String description, String dataType, String location){
    return this.addMemberItem(collectionId, id, description, dataType, location, null, null);
  }

  public TestDataCreationHelper addMemberItem(String collectionId, String id, String location){
    return this.addMemberItem(collectionId, id, null, null, location, null, null);
  }

  public TestDataCreationHelper addMemberItem(String collectionId, String id, String dataType, String location){
    return this.addMemberItem(collectionId, id, null, dataType, location, null, null);
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
    Set<Entry<String, MemberItem>> memberEntries = members.entrySet();
    memberEntries.forEach((entry) -> {
      memberDao.save(entry.getValue());
    });

    Set<Entry<String, CollectionObject>> collectionEntries = collections.entrySet();
    collectionEntries.forEach((entry) -> {
      collectionDao.save(entry.getValue());
    });
  }
}
