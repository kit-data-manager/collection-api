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

  /**
   * Initialize the builder.
   *
   * @param collectionDao DAO interface to store collection.
   * @param memberDao DAO interface to store members.
   */
  public static TestDataCreationHelper initialize(ICollectionObjectDao collectionDao, IMemberItemDao memberDao){
    TestDataCreationHelper helper = new TestDataCreationHelper();
    helper.collectionDao = collectionDao;
    helper.memberDao = memberDao;
    return helper;
  }

  /**
   * Add a new collection.
   *
   * @param id The collectionId.
   * @param description The collection description.
   * @param caps The CollectionCapabilities.
   * @param props The CollectionProperties.
   *
   * @return this
   */
  public TestDataCreationHelper addCollection(String id, String description, CollectionCapabilities caps, CollectionProperties props){
    CollectionObject o = new CollectionObject();
    o.setId(id);
    o.setDescription(description);
    o.setCapabilities(caps);
    o.setProperties(props);
    collections.put(id, o);
    return this;
  }

  /**
   * Add a new collection.
   *
   * @param id The collectionId.
   * @param props The CollectionProperties.
   *
   * @return this
   */
  public TestDataCreationHelper addCollection(String id, CollectionProperties props){
    return this.addCollection(id, null, CollectionCapabilities.getDefault(), props);
  }

  /**
   * Add a new collection member item. In contrast to collections, members are
   * already persisted at this point in order to obtain a valid database key in
   * case a member should be part in two collections.
   *
   * @param collectionId The collectionId.
   * @param id The memberId.
   * @param description The member description.
   * @param dataType The member datatype.
   * @param location The member location.
   * @param ontology The member ontology.
   * @param mappings The CollectionItemMappingsMetadata.
   *
   * @return this
   */
  public TestDataCreationHelper addMemberItem(String collectionId, String id, String description, String dataType, String location, String ontology, CollectionItemMappingMetadata mappings){
    MemberItem o;
    if(members.containsKey(id)){
      o = members.get(id);
    } else{
      o = new MemberItem();
      o.setMid(id);
      o.setDescription(description);
      o.setDatatype(dataType);
      o.setLocation(location);
      o.setOntology(ontology);
      o.setMappings((mappings != null) ? mappings : CollectionItemMappingMetadata.getDefault());
      o = memberDao.save(o);
      members.put(id, o);
    }
    Membership m = new Membership();
    m.setMember(o);
    m.setMappings(o.getMappings());
    collections.get(collectionId).getMembers().add(m);

    return this;
  }

  /**
   * Add a new collection member item.
   *
   * @param collectionId The collectionId.
   * @param id The memberId.
   * @param location The member location.
   *
   * @return this
   */
  public TestDataCreationHelper addMemberItem(String collectionId, String id, String location){
    return this.addMemberItem(collectionId, id, null, null, location, null, null);
  }

  /**
   * Add a new collection member item.
   *
   * @param collectionId The collectionId.
   * @param id The memberId.
   * @param dataType The member datatype.
   * @param location The member location.
   *
   * @return this
   */
  public TestDataCreationHelper addMemberItem(String collectionId, String id, String dataType, String location){
    return this.addMemberItem(collectionId, id, null, dataType, location, null, null);
  }

  /**
   * Finalize this builder by persisting all added collections and members..
   */
  public void persist(){

    Set<Entry<String, CollectionObject>> collectionEntries = collections.entrySet();
    collectionEntries.forEach((entry) -> {
      collectionDao.save(entry.getValue());
    });
  }
}
