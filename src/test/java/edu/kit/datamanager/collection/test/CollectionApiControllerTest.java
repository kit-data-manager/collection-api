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
package edu.kit.datamanager.collection.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.datamanager.collection.dao.ICollectionObjectDao;
import edu.kit.datamanager.collection.dao.IMemberItemDao;
import edu.kit.datamanager.collection.dao.IMembershipDao;
import edu.kit.datamanager.collection.domain.CollectionCapabilities;
import edu.kit.datamanager.collection.domain.CollectionItemMappingMetadata;
import edu.kit.datamanager.collection.domain.CollectionProperties;
import edu.kit.datamanager.collection.domain.CollectionResultSet;
import edu.kit.datamanager.collection.domain.MemberItem;
import edu.kit.datamanager.collection.test.util.TestDataCreationHelper;
import java.time.Instant;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 *
 * @author jejkal
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestExecutionListeners(listeners = {ServletTestExecutionListener.class,
  DependencyInjectionTestExecutionListener.class,
  DirtiesContextTestExecutionListener.class,
  TransactionalTestExecutionListener.class,
  WithSecurityContextTestExecutionListener.class})
@ActiveProfiles("test")
public class CollectionApiControllerTest{

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private IMemberItemDao memberDao;
  @Autowired
  private ICollectionObjectDao collectionDao;
  @Autowired
  private IMembershipDao membershipDao;

  @Before
  public void setUp() throws JsonProcessingException{
    membershipDao.deleteAll();
    memberDao.deleteAll();
    collectionDao.deleteAll();
//
//    CollectionObject collection = new CollectionObject();
//    collection.setId("3");
//    collection.setDescription("This is collection 3");
//    CollectionProperties props = new CollectionProperties();
//    props.setDateCreated(Instant.now());
//    props.setOwnership("me");
//    props.setLicense("Apache 2.0");
//    props.setModelType("anotherType");
//    props.setDescriptionOntology("custom");
//    collection.setProperties(props);
//    CollectionCapabilities caps = new CollectionCapabilities();
//    collection.setCapabilities(caps);
//    collection = collectionDao.save(collection);
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

  @Test
  public void testGetCollections() throws Exception{
    TestDataCreationHelper.initialize(memberDao, collectionDao, membershipDao).addCollection("1", CollectionProperties.getDefault()).addCollection("2", CollectionProperties.getDefault()).persist();

    MvcResult res = this.mockMvc.perform(get("/api/v1/collections/")).andDo(print()).andExpect(status().isOk()).andReturn();
    ObjectMapper map = new ObjectMapper();
    CollectionResultSet result = map.readValue(res.getResponse().getContentAsString(), CollectionResultSet.class);
    Assert.assertNotNull(result);
    Assert.assertNull(result.getNextCursor());
    Assert.assertNull(result.getPrevCursor());
    Assert.assertEquals(2, result.getContents().size());
  }

  @Test
  public void testGetCollectionsPage() throws Exception{
    TestDataCreationHelper helper = TestDataCreationHelper.initialize(memberDao, collectionDao, membershipDao);
    for(int i = 0; i < 19; i++){
      helper = helper.addCollection(Integer.toString(i), CollectionProperties.getDefault());
    }

    helper.persist();

    ObjectMapper map = new ObjectMapper();
    //obtain page 0 with size 10
    MvcResult res = this.mockMvc.perform(get("/api/v1/collections/").param("size", "10")).andDo(print()).andExpect(status().isOk()).andReturn();
    CollectionResultSet result = map.readValue(res.getResponse().getContentAsString(), CollectionResultSet.class);
    Assert.assertNotNull(result);
    Assert.assertNotNull(result.getNextCursor());
    Assert.assertNull(result.getPrevCursor());
    Assert.assertEquals(10, result.getContents().size());

    //obtain second page with size 10 containing 9 elements
    res = this.mockMvc.perform(get("/api/v1/collections/").param("page", "1").param("size", "10")).andDo(print()).andExpect(status().isOk()).andReturn();
    result = map.readValue(res.getResponse().getContentAsString(), CollectionResultSet.class);
    Assert.assertNotNull(result);
    Assert.assertNull(result.getNextCursor());
    Assert.assertNotNull(result.getPrevCursor());
    Assert.assertEquals(9, result.getContents().size());
  }

  @Test
  public void testGetCollectionsPageWithFilter() throws Exception{
    CollectionProperties propsWithOwnership = CollectionProperties.getDefault();
    propsWithOwnership.setOwnership("test");
    CollectionProperties propsWithModelType = CollectionProperties.getDefault();
    propsWithModelType.setModelType("custom");
    CollectionProperties propsWithOwnershipAndModelType = CollectionProperties.getDefault();
    propsWithOwnershipAndModelType.setOwnership("tester");
    propsWithOwnershipAndModelType.setModelType("default");

    MemberItem item = new MemberItem();
    item.setId("1");
    item.setLocation("localhost");
    item.setDatatype("image");

    TestDataCreationHelper.
            initialize(memberDao, collectionDao, membershipDao).
            addCollection("1", propsWithOwnership).
            addCollection("2", propsWithModelType).
            addCollection("3", CollectionProperties.getDefault()).
            addCollection("4", propsWithOwnershipAndModelType).
            addMemberItem("1", "image", "localhost").
            addMemberItem("2", "document", "somedoc").
            addMembership("3", "1").
            addMembership("4", "2").persist();

    ObjectMapper map = new ObjectMapper();

    //get collection with ownership 'test' -> should be collection #1
    MvcResult res = this.mockMvc.perform(get("/api/v1/collections/").param("f_ownership", "test")).andDo(print()).andExpect(status().isOk()).andReturn();
    CollectionResultSet result = map.readValue(res.getResponse().getContentAsString(), CollectionResultSet.class);
    Assert.assertNotNull(result);
    Assert.assertEquals(1, result.getContents().size());
    Assert.assertEquals("1", result.getContents().get(0).getId());

    //get collection with modelType 'custom' -> should be collection #2
    res = this.mockMvc.perform(get("/api/v1/collections/").param("f_modelType", "custom")).andDo(print()).andExpect(status().isOk()).andReturn();
    result = map.readValue(res.getResponse().getContentAsString(), CollectionResultSet.class);
    Assert.assertNotNull(result);
    Assert.assertEquals(1, result.getContents().size());
    Assert.assertEquals("2", result.getContents().get(0).getId());

    //get collection with member type 'image' -> should be collection #3
    res = this.mockMvc.perform(get("/api/v1/collections/").param("f_memberType", "image")).andDo(print()).andExpect(status().isOk()).andReturn();
    result = map.readValue(res.getResponse().getContentAsString(), CollectionResultSet.class);
    Assert.assertNotNull(result);
    Assert.assertEquals(1, result.getContents().size());
    Assert.assertEquals("3", result.getContents().get(0).getId());

    //get collection with member type ownership, modelType and member type' -> should be collection #4
    res = this.mockMvc.perform(get("/api/v1/collections/").param("f_modelType", "default").param("f_ownership", "tester").param("f_memberType", "document")).andDo(print()).andExpect(status().isOk()).andReturn();
    result = map.readValue(res.getResponse().getContentAsString(), CollectionResultSet.class);
    Assert.assertNotNull(result);
    Assert.assertEquals(1, result.getContents().size());
    Assert.assertEquals("4", result.getContents().get(0).getId());

    //no collection with provided modelType and ownership should be found
    res = this.mockMvc.perform(get("/api/v1/collections/").param("f_modelType", "7'er").param("f_ownership", "me").param("f_memberType", "giraffe")).andDo(print()).andExpect(status().isOk()).andReturn();
    result = map.readValue(res.getResponse().getContentAsString(), CollectionResultSet.class);
    Assert.assertNotNull(result);
    Assert.assertTrue(result.getContents().isEmpty());

    //no member with provided modelType and ownership should be found
    res = this.mockMvc.perform(get("/api/v1/collections/").param("f_modelType", "default").param("f_memberType", "giraffe")).andDo(print()).andExpect(status().isOk()).andReturn();
    result = map.readValue(res.getResponse().getContentAsString(), CollectionResultSet.class);
    Assert.assertNotNull(result);
    Assert.assertTrue(result.getContents().isEmpty());
  }

  @Test
  public void testGetCollectionsCapabilities() throws Exception{
    CollectionCapabilities restricted = CollectionCapabilities.getDefault();
    restricted.setMembershipIsMutable(Boolean.FALSE);
    restricted.setPropertiesAreMutable(Boolean.FALSE);

    TestDataCreationHelper.initialize(memberDao, collectionDao, membershipDao).addCollection("1", CollectionProperties.getDefault()).addCollection("2", "description", restricted, CollectionProperties.getDefault()).persist();
    ObjectMapper map = new ObjectMapper();

    //get default collection capabilities
    MvcResult res = this.mockMvc.perform(get("/api/v1/collections/1/capabilities/")).andDo(print()).andExpect(status().isOk()).andReturn();
    CollectionCapabilities result = map.readValue(res.getResponse().getContentAsString(), CollectionCapabilities.class);
    Assert.assertNotNull(result);
    Assert.assertEquals(Integer.valueOf(-1), result.getMaxLength());
    Assert.assertTrue(result.getMembershipIsMutable());
    Assert.assertTrue(result.getPropertiesAreMutable());

    //get restricted collection capabilities
    res = this.mockMvc.perform(get("/api/v1/collections/2/capabilities/")).andDo(print()).andExpect(status().isOk()).andReturn();
    result = map.readValue(res.getResponse().getContentAsString(), CollectionCapabilities.class);
    Assert.assertNotNull(result);
    Assert.assertEquals(Integer.valueOf(-1), result.getMaxLength());
    Assert.assertFalse(result.getMembershipIsMutable());
    Assert.assertFalse(result.getPropertiesAreMutable());

    //get collection capabilities for invalid collection
    this.mockMvc.perform(get("/api/v1/collections/3/capabilities/")).andDo(print()).andExpect(status().isNotFound()).andReturn();

  }

  @Test
  public void testDeleteCollection() throws Exception{
    TestDataCreationHelper.initialize(memberDao, collectionDao, membershipDao).addCollection("1", CollectionProperties.getDefault()).persist();

    //get collection with id 1
    this.mockMvc.perform(get("/api/v1/collections/1")).andDo(print()).andExpect(status().isOk()).andReturn();

    //delete collection with id 1
    this.mockMvc.perform(delete("/api/v1/collections/1")).andDo(print()).andExpect(status().isOk()).andReturn();

    //delete collection with id 1 a second time (should result in 404)
    this.mockMvc.perform(delete("/api/v1/collections/1")).andDo(print()).andExpect(status().isNotFound()).andReturn();

    //get collection with id 1 should fail now
    this.mockMvc.perform(get("/api/v1/collections/1")).andDo(print()).andExpect(status().isNotFound()).andReturn();
  }

  @Test
  public void testGetCollectionById() throws Exception{
    TestDataCreationHelper.initialize(memberDao, collectionDao, membershipDao).addCollection("1", CollectionProperties.getDefault()).persist();

    //get collection with id 1
    this.mockMvc.perform(get("/api/v1/collections/1")).andDo(print()).andExpect(status().isOk()).andReturn();

    //get collection with id 2
    this.mockMvc.perform(get("/api/v1/collections/2")).andDo(print()).andExpect(status().isNotFound()).andReturn();
  }

  @Test
  public void testDeleteMembership() throws Exception{
    TestDataCreationHelper.initialize(memberDao, collectionDao, membershipDao).addCollection("1", CollectionProperties.getDefault()).addMemberItem("1", "localhost").addMembership("1", "1").persist();
    ObjectMapper map = new ObjectMapper();

    //get membership of member 1 and collection 1
    MvcResult res = this.mockMvc.perform(get("/api/v1/collections/1/members/1")).andDo(print()).andExpect(status().isOk()).andReturn();
    MemberItem result = map.readValue(res.getResponse().getContentAsString(), MemberItem.class);
    Assert.assertNotNull(result);
    Assert.assertEquals("1", result.getId());

    //delete membership of member 1 and collection 1
    this.mockMvc.perform(delete("/api/v1/collections/1/members/1")).andDo(print()).andExpect(status().isOk()).andReturn();

    //delete membership of member 1 and collection 1 a second time
    this.mockMvc.perform(delete("/api/v1/collections/1/members/1")).andDo(print()).andExpect(status().isNotFound()).andReturn();

    //membership should no longer be found
    this.mockMvc.perform(get("/api/v1/collections/1/members/1")).andDo(print()).andExpect(status().isNotFound()).andReturn();
  }

  @Test
  public void testGetMembership() throws Exception{
    TestDataCreationHelper.initialize(memberDao, collectionDao, membershipDao).addCollection("1", CollectionProperties.getDefault()).addMemberItem("1", "localhost").addMembership("1", "1").persist();
    ObjectMapper map = new ObjectMapper();

    //get membership of member 1 and collection 1
    MvcResult res = this.mockMvc.perform(get("/api/v1/collections/1/members/1")).andDo(print()).andExpect(status().isOk()).andReturn();
    MemberItem result = map.readValue(res.getResponse().getContentAsString(), MemberItem.class);
    Assert.assertNotNull(result);
    Assert.assertEquals("1", result.getId());

    //collection 2 does not exist, HTTP 404 expected
    this.mockMvc.perform(get("/api/v1/collections/2/members/1")).andDo(print()).andExpect(status().isNotFound()).andReturn();

    //member 2 does not exist, HTTP 404 expected
    this.mockMvc.perform(get("/api/v1/collections/1/members/2")).andDo(print()).andExpect(status().isNotFound()).andReturn();
  }

  @Test
  public void testDeleteMembershipProperty() throws Exception{
    CollectionItemMappingMetadata props = new CollectionItemMappingMetadata();
    props.setRole("member");
    props.setIndex(1);
    props.setDateAdded(Instant.now());
    props.setDateUpdated(Instant.now());
    TestDataCreationHelper.initialize(memberDao, collectionDao, membershipDao).addCollection("1", CollectionProperties.getDefault()).addMemberItem("1", "localhost").addMembership("1", "1", props).persist();
    ObjectMapper map = new ObjectMapper();

    for(String element : new String[]{"index", "role", "dateAdded", "dateUpdated"}){
      MvcResult res = this.mockMvc.perform(get("/api/v1/collections/1/members/1/properties/" + element)).andDo(print()).andExpect(status().isOk()).andReturn();
      byte[] result = res.getResponse().getContentAsByteArray();
      Assert.assertNotNull(result);
      Assert.assertNotEquals(0, result.length);

      //delete index property
      this.mockMvc.perform(delete("/api/v1/collections/1/members/1/properties/" + element)).andDo(print()).andExpect(status().isOk()).andReturn();

      res = this.mockMvc.perform(get("/api/v1/collections/1/members/1/properties/" + element)).andDo(print()).andExpect(status().isOk()).andReturn();
      result = res.getResponse().getContentAsByteArray();
      Assert.assertEquals(0, result.length);
    }

    this.mockMvc.perform(get("/api/v1/collections/1/members/1/properties/invalidProperty")).andDo(print()).andExpect(status().isNotFound()).andReturn();
    this.mockMvc.perform(delete("/api/v1/collections/1/members/1/properties/invalidProperty")).andDo(print()).andExpect(status().isNotFound()).andReturn();
  }

  @Test
  public void testPutMembershipProperty() throws Exception{
    CollectionItemMappingMetadata props = new CollectionItemMappingMetadata();
    props.setRole("member");
    props.setIndex(1);
    props.setDateAdded(Instant.now());
    props.setDateUpdated(Instant.now());

    TestDataCreationHelper.initialize(memberDao, collectionDao, membershipDao).addCollection("1", CollectionProperties.getDefault()).addMemberItem("1", "localhost").addMembership("1", "1", props).persist();
    ObjectMapper map = new ObjectMapper();

    this.mockMvc.perform(put("/api/v1/collections/1/members/1/properties/role").content("\"guest\"").contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andReturn();
    
    MvcResult res = this.mockMvc.perform(get("/api/v1/collections/1/members/1/properties/role")).andDo(print()).andExpect(status().isOk()).andReturn();
    String result = res.getResponse().getContentAsString();
    Assert.assertEquals("\"guest\"", result);

//    this.mockMvc.perform(get("/api/v1/collections/1/members/1/properties/invalidProperty")).andDo(print()).andExpect(status().isNotFound()).andReturn();
//    this.mockMvc.perform(delete("/api/v1/collections/1/members/1/properties/invalidProperty")).andDo(print()).andExpect(status().isNotFound()).andReturn();
  }

}
