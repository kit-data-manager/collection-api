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
import edu.kit.datamanager.collection.domain.CollectionObject;
import edu.kit.datamanager.collection.domain.CollectionProperties;
import edu.kit.datamanager.collection.domain.CollectionResultSet;
import edu.kit.datamanager.collection.domain.MemberItem;
import edu.kit.datamanager.collection.domain.MemberResultSet;
import edu.kit.datamanager.collection.domain.d3.DataWrapper;
import edu.kit.datamanager.collection.util.TestDataCreationHelper;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.collections4.IterableUtils;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    collectionDao.deleteAll();
    membershipDao.deleteAll();
  }

  @Test
  public void testGetCollections() throws Exception{
    TestDataCreationHelper.initialize(collectionDao, memberDao).addCollection("1", CollectionProperties.getDefault()).addCollection("2", CollectionProperties.getDefault()).persist();

    MvcResult res = this.mockMvc.perform(get("/api/v1/collections/")).andDo(print()).andExpect(status().isOk()).andReturn();
    ObjectMapper map = new ObjectMapper();
    CollectionResultSet result = map.readValue(res.getResponse().getContentAsString(), CollectionResultSet.class);
    Assert.assertNotNull(result);
    Assert.assertNull(result.getNextCursor());
    Assert.assertNull(result.getPrevCursor());
    Assert.assertEquals(2, result.getContents().size());
  }

  @Test
  public void testGetCollectionsD3() throws Exception{
    TestDataCreationHelper.initialize(collectionDao, memberDao).addCollection("1", CollectionProperties.getDefault()).addCollection("2", CollectionProperties.getDefault()).addMemberItem("1", "1", "localhost").addMemberItem("1", "2", "lcalhost").persist();

    MvcResult res = this.mockMvc.perform(get("/api/v1/collections/").header("Accept", "application/vnd.datamanager.d3+json")).andDo(print()).andExpect(status().isOk()).andReturn();
    ObjectMapper map = new ObjectMapper();
    DataWrapper result = map.readValue(res.getResponse().getContentAsString(), DataWrapper.class);
    Assert.assertNotNull(result);
    Assert.assertEquals(2, result.getNodes().size());
    Assert.assertEquals(2, result.getLinks().size());
  }

  @Test
  public void testGetCollectionsPage() throws Exception{
    TestDataCreationHelper helper = TestDataCreationHelper.initialize(collectionDao, memberDao);
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

    TestDataCreationHelper.initialize(collectionDao, memberDao).
            addCollection("1", propsWithOwnership).
            addCollection("2", propsWithModelType).
            addCollection("3", CollectionProperties.getDefault()).
            addCollection("4", propsWithOwnershipAndModelType).
            addMemberItem("3", "m1", "image", "localhost").
            addMemberItem("4", "m2", "document", "somedoc").
            persist();

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

    TestDataCreationHelper.initialize(collectionDao, memberDao).addCollection("1", CollectionProperties.getDefault()).addCollection("2", "description", restricted, CollectionProperties.getDefault()).persist();
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
    TestDataCreationHelper.initialize(collectionDao, memberDao).addCollection("1", CollectionProperties.getDefault()).persist();

    //get collection with id 1
    MvcResult res = this.mockMvc.perform(get("/api/v1/collections/1")).andDo(print()).andExpect(status().isOk()).andReturn();
    String etag = res.getResponse().getHeader("ETag");

    //delete collection with id 1 with wrong etag
    this.mockMvc.perform(delete("/api/v1/collections/1").header("If-Match", "\test\"")).andDo(print()).andExpect(status().isPreconditionFailed()).andReturn();

    //delete collection with id 1 with no etag
    this.mockMvc.perform(delete("/api/v1/collections/1")).andDo(print()).andExpect(status().isPreconditionRequired()).andReturn();

    //delete collection with id 1
    this.mockMvc.perform(delete("/api/v1/collections/1").header("If-Match", etag)).andDo(print()).andExpect(status().isNoContent()).andReturn();

    //delete collection with id 1 a second time (should result in 204)
    this.mockMvc.perform(delete("/api/v1/collections/1").header("If-Match", etag)).andDo(print()).andExpect(status().isNoContent()).andReturn();

    //get collection with id 1 (should result in 404)
    this.mockMvc.perform(get("/api/v1/collections/1").header("If-Match", etag)).andDo(print()).andExpect(status().isNotFound()).andReturn();
  }

  @Test
  public void testGetCollectionById() throws Exception{
    TestDataCreationHelper.initialize(collectionDao, memberDao).addCollection("1", CollectionProperties.getDefault()).persist();

    //get collection with id 1
    this.mockMvc.perform(get("/api/v1/collections/1")).andDo(print()).andExpect(status().isOk()).andReturn();

    //get collection with id 2
    this.mockMvc.perform(get("/api/v1/collections/2")).andDo(print()).andExpect(status().isNotFound()).andReturn();
  }

  @Test
  public void testDeleteMembership() throws Exception{
    CollectionCapabilities immutable_caps = CollectionCapabilities.getDefault();
    immutable_caps.setMembershipIsMutable(Boolean.FALSE);

    CollectionProperties props = CollectionProperties.getDefault();
    props.getMemberOf().add("1");

    TestDataCreationHelper.initialize(collectionDao, memberDao).
            addCollection("1", CollectionProperties.getDefault()).
            addCollection("2", "Immutable collection", immutable_caps, props).
            addMemberItem("1", "m1", "localhost").
            addMemberItem("2", "m2", "localhost").
            addMemberItem("1", "2", "localhost").
            persist();
    ObjectMapper map = new ObjectMapper();

    //get membership of member 1 and collection 1
    MvcResult res = this.mockMvc.perform(get("/api/v1/collections/1/members/m1")).andDo(print()).andExpect(status().isOk()).andReturn();
    MemberItem result = map.readValue(res.getResponse().getContentAsString(), MemberItem.class);
    Assert.assertNotNull(result);
    Assert.assertEquals("m1", result.getMid());

    res = this.mockMvc.perform(get("/api/v1/collections/1/members/m1")).andDo(print()).andExpect(status().isOk()).andReturn();
    String etag = res.getResponse().getHeader("ETag");

    //delete membership of member 1 and collection 1
    this.mockMvc.perform(delete("/api/v1/collections/1/members/m1").header("If-Match", etag)).andDo(print()).andExpect(status().isNoContent()).andReturn();

    //delete membership of member 1 and collection 1 a second time
    this.mockMvc.perform(delete("/api/v1/collections/1/members/m1")).andDo(print()).andExpect(status().isNoContent()).andReturn();

    //membership should no longer be found
    this.mockMvc.perform(get("/api/v1/collections/1/members/m1")).andDo(print()).andExpect(status().isNotFound()).andReturn();

    res = this.mockMvc.perform(get("/api/v1/collections/2/members/m2")).andDo(print()).andExpect(status().isOk()).andReturn();
    etag = res.getResponse().getHeader("ETag");

    //delete membership from immutable collection -> return HTTP 403
    this.mockMvc.perform(delete("/api/v1/collections/2/members/m2").header("If-Match", etag)).andDo(print()).andExpect(status().isForbidden()).andReturn();

    res = this.mockMvc.perform(get("/api/v1/collections/1/members/2")).andDo(print()).andExpect(status().isOk()).andReturn();
    etag = res.getResponse().getHeader("ETag");

    //delete membership with collection from collection -> return with NO CONTENT
    this.mockMvc.perform(delete("/api/v1/collections/1/members/2").header("If-Match", etag)).andDo(print()).andExpect(status().isNoContent()).andReturn();

    res = this.mockMvc.perform(get("/api/v1/collections/2")).andDo(print()).andExpect(status().isOk()).andReturn();
    CollectionObject collection2 = map.readValue(res.getResponse().getContentAsString(), CollectionObject.class);

    //memberOf list should now be empty
    Assert.assertTrue(collection2.getProperties().getMemberOf().isEmpty());
  }

  @Test
  public void testGetMembership() throws Exception{
    TestDataCreationHelper.initialize(collectionDao, memberDao).addCollection("1", CollectionProperties.getDefault()).addMemberItem("1", "m1", "localhost").persist();
    ObjectMapper map = new ObjectMapper();

    //get membership of member 1 and collection 1
    MvcResult res = this.mockMvc.perform(get("/api/v1/collections/1/members/m1")).andDo(print()).andExpect(status().isOk()).andReturn();
    MemberItem result = map.readValue(res.getResponse().getContentAsString(), MemberItem.class);
    Assert.assertNotNull(result);
    Assert.assertEquals("m1", result.getMid());

    //collection 2 does not exist, HTTP 404 expected
    this.mockMvc.perform(get("/api/v1/collections/2/members/m1")).andDo(print()).andExpect(status().isNotFound()).andReturn();

    //member 2 does not exist, HTTP 404 expected
    this.mockMvc.perform(get("/api/v1/collections/1/members/m2")).andDo(print()).andExpect(status().isNotFound()).andReturn();
  }

  @Test
  public void testDeleteMembershipProperty() throws Exception{
    CollectionItemMappingMetadata props = new CollectionItemMappingMetadata();
    props.setMemberRole("member");
    props.setIndex(1);
    props.setDateAdded(Instant.now());
    props.setDateUpdated(Instant.now());
    TestDataCreationHelper.initialize(collectionDao, memberDao).addCollection("1", CollectionProperties.getDefault()).addCollection("2", CollectionProperties.getDefault()).addMemberItem("1", "m1", null, null, "localhost", null, props).persist();
    ObjectMapper map = new ObjectMapper();

    for(String element : new String[]{"index", "role", "dateAdded", "dateUpdated"}){
      MvcResult res = this.mockMvc.perform(get("/api/v1/collections/1/members/m1/properties/" + element)).andDo(print()).andExpect(status().isOk()).andReturn();
      String etag = res.getResponse().getHeader("ETag");
      byte[] result = res.getResponse().getContentAsByteArray();
      Assert.assertNotNull(result);
      Assert.assertNotEquals(0, result.length);

      //delete property
      this.mockMvc.perform(delete("/api/v1/collections/1/members/m1/properties/" + element).header("If-Match", etag)).andDo(print()).andExpect(status().isNoContent()).andReturn();

      res = this.mockMvc.perform(get("/api/v1/collections/1/members/m1/properties/" + element)).andDo(print()).andExpect(status().isOk()).andReturn();
      result = res.getResponse().getContentAsByteArray();
      Assert.assertEquals(0, result.length);
    }

    //get current etag
    MvcResult res = this.mockMvc.perform(get("/api/v1/collections/1/members/m1")).andDo(print()).andExpect(status().isOk()).andReturn();
    String etag = res.getResponse().getHeader("ETag");
    //get property for invalid membership -> return HTTP 404
    this.mockMvc.perform(get("/api/v1/collections/2/members/m1/properties/index")).andDo(print()).andExpect(status().isNotFound()).andReturn();

    //get invalid property for valid membership -> return http 404
    this.mockMvc.perform(get("/api/v1/collections/1/members/m1/properties/invalidProperty")).andDo(print()).andExpect(status().isNotFound()).andReturn();
    //delete invalid property for value membership -> return http 404
    this.mockMvc.perform(delete("/api/v1/collections/1/members/m1/properties/invalidProperty").header("If-Match", etag)).andDo(print()).andExpect(status().isNotFound()).andReturn();
    //delete property for invalid membership -> return http 404
    this.mockMvc.perform(delete("/api/v1/collections/2/members/m1/properties/index")).andDo(print()).andExpect(status().isNotFound()).andReturn();
  }

  @Test
  public void testPutMembershipProperty() throws Exception{
    CollectionItemMappingMetadata props = new CollectionItemMappingMetadata();
    props.setMemberRole("member");
    props.setIndex(1);
    props.setDateAdded(Instant.now());
    props.setDateUpdated(Instant.now());

    TestDataCreationHelper.initialize(collectionDao, memberDao).addCollection("1", CollectionProperties.getDefault()).addCollection("2", CollectionProperties.getDefault()).addMemberItem("1", "m1", null, null, "localhost", null, props).persist();
    ObjectMapper map = new ObjectMapper();

    String theDate = "\"2019-07-19T07:25:37.52Z\"";
    Map<String, String> propertiesAndValue = new HashMap<>();
    propertiesAndValue.put("role", "\"guest\"");
    propertiesAndValue.put("index", "\"2\"");
    propertiesAndValue.put("dateAdded", theDate);
    propertiesAndValue.put("dateUpdated", theDate);

    //test all valid properties -> should update and return
    Set<Entry<String, String>> entries = propertiesAndValue.entrySet();
    for(Entry<String, String> entry : entries){
      MvcResult res = this.mockMvc.perform(get("/api/v1/collections/1/members/m1")).andDo(print()).andExpect(status().isOk()).andReturn();
      String etag = res.getResponse().getHeader("ETag");

      //put property 'role'
      res = this.mockMvc.perform(put("/api/v1/collections/1/members/m1/properties/" + entry.getKey()).header("If-Match", etag).content(entry.getValue()).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andReturn();
      String result = res.getResponse().getContentAsString();
      Assert.assertEquals(entry.getValue(), result);
      //check by get operation
      res = this.mockMvc.perform(get("/api/v1/collections/1/members/m1/properties/" + entry.getKey())).andDo(print()).andExpect(status().isOk()).andReturn();
      result = res.getResponse().getContentAsString();
      Assert.assertEquals(entry.getValue(), result);
    }

    //put property for invalid membership -> return HTTP 404
    this.mockMvc.perform(put("/api/v1/collections/2/members/m1/properties/index").content("\"2\"").contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNotFound()).andReturn();

    MvcResult res = this.mockMvc.perform(get("/api/v1/collections/1/members/m1")).andDo(print()).andExpect(status().isOk()).andReturn();
    String etag = res.getResponse().getHeader("ETag");

    //put index property with invalid value -> return HTTP 400
    this.mockMvc.perform(put("/api/v1/collections/1/members/m1/properties/index").content("\"a\"").header("If-Match", etag).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isBadRequest()).andReturn();

    //put date property with invalid value -> return HTTP 400
    this.mockMvc.perform(put("/api/v1/collections/1/members/m1/properties/dateAdded").content("\"a\"").header("If-Match", etag).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isBadRequest()).andReturn();
    this.mockMvc.perform(put("/api/v1/collections/1/members/m1/properties/dateUpdated").content("\"a\"").header("If-Match", etag).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isBadRequest()).andReturn();

    //put invalid property  -> return HTTP 404
    this.mockMvc.perform(put("/api/v1/collections/1/members/m1/properties/invalid").content("\"a\"").header("If-Match", etag).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNotFound()).andReturn();
  }

  @Test
  public void testPostMembers() throws Exception{
    CollectionCapabilities caps_immutable = CollectionCapabilities.getDefault();
    caps_immutable.setMembershipIsMutable(Boolean.FALSE);

    CollectionCapabilities caps_zero_length = CollectionCapabilities.getDefault();
    caps_zero_length.setMaxLength(0);

    CollectionCapabilities caps_type_restriction = CollectionCapabilities.getDefault();
    caps_type_restriction.setRestrictedToType("special");

    TestDataCreationHelper.initialize(collectionDao, memberDao).
            addCollection("1", CollectionProperties.getDefault()).
            addCollection("2", CollectionProperties.getDefault()).
            addCollection("immutable", "Immutable collection", caps_immutable, CollectionProperties.getDefault()).
            addCollection("zero_length", "Zero-length collection", caps_zero_length, CollectionProperties.getDefault()).
            addCollection("type_restricted", "Type-restricted collection", caps_type_restriction, CollectionProperties.getDefault()).
            persist();

    ObjectMapper map = new ObjectMapper();
    CollectionItemMappingMetadata props = new CollectionItemMappingMetadata();
    props.setMemberRole("member");
    props.setIndex(1);
    props.setDateAdded(Instant.now());
    props.setDateUpdated(Instant.now());
    MemberItem item = new MemberItem();
    item.setMid("m1");
    item.setDescription("First member");
    item.setLocation("localhost");
    item.setMappings(props);

    //post to collection which does not exist -> should fail with HTTP 404
    this.mockMvc.perform(post("/api/v1/collections/999/members").content(map.writeValueAsBytes(new MemberItem[]{item})).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNotFound()).andReturn();

    //post to immutable collection -> should fail with HTTP 403
    this.mockMvc.perform(post("/api/v1/collections/immutable/members").content(map.writeValueAsBytes(new MemberItem[]{item})).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isForbidden()).andReturn();

    //post to zero-length collection -> should fail with HTTP 403
    this.mockMvc.perform(post("/api/v1/collections/zero_length/members").content(map.writeValueAsBytes(new MemberItem[]{item})).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isForbidden()).andReturn();

    //post to type-restricted collection -> should fail with HTTP 400
    this.mockMvc.perform(post("/api/v1/collections/type_restricted/members").content(map.writeValueAsBytes(new MemberItem[]{item})).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isBadRequest()).andReturn();

    //post to real collection -> should add single member
    MvcResult res = this.mockMvc.perform(post("/api/v1/collections/1/members").content(map.writeValueAsBytes(new MemberItem[]{item})).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isCreated()).andReturn();
    String result = res.getResponse().getContentAsString();

    MemberItem[] members = map.readValue(result, MemberItem[].class);
    Assert.assertNotNull(members);
    Assert.assertEquals(1, members.length);
    Assert.assertEquals("m1", members[0].getMid());

    //post member a second time -> should fail with HTTP Conflict
    this.mockMvc.perform(post("/api/v1/collections/1/members").content(map.writeValueAsBytes(new MemberItem[]{item})).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isConflict()).andReturn();

    //post member without mapping metadata -> should be created server side
    item = new MemberItem();
    item.setMid("m2");
    item.setDescription("First member");
    item.setLocation("localhost");
    //post to real collection
    res = this.mockMvc.perform(post("/api/v1/collections/1/members").content(map.writeValueAsBytes(new MemberItem[]{item})).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isCreated()).andReturn();
    result = res.getResponse().getContentAsString();

    members = map.readValue(result, MemberItem[].class);
    Assert.assertNotNull(members);
    Assert.assertEquals(1, members.length);
    Assert.assertEquals("m2", members[0].getMid());
    Assert.assertNotNull(members[0].getMappings());

    item = new MemberItem();
    item.setDescription("Member w/o mid");
    item.setLocation("localhost");

    //post member w/o mid to collection -> assign UUID as mid and create
    res = this.mockMvc.perform(post("/api/v1/collections/1/members").content(map.writeValueAsBytes(new MemberItem[]{item})).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isCreated()).andReturn();
    result = res.getResponse().getContentAsString();

    members = map.readValue(result, MemberItem[].class);
    Assert.assertNotNull(members);
    Assert.assertEquals(1, members.length);
    Assert.assertNotNull(members[0].getMid());
    //assigned mid is expected to be a UUID (might change later!?)
    UUID memberId = UUID.fromString(members[0].getMid());
    Assert.assertNotNull(memberId);

    item = new MemberItem();
    item.setMid("immutable");
    item.setDescription("Immutable collection");
    item.setLocation("http://localhost/api/v1/immutable");

    //post member w/o mid to collection -> assign UUID as mid and create
    res = this.mockMvc.perform(post("/api/v1/collections/1/members").content(map.writeValueAsBytes(new MemberItem[]{item})).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isCreated()).andReturn();
    result = res.getResponse().getContentAsString();

    members = map.readValue(result, MemberItem[].class);
    Assert.assertNotNull(members);
    Assert.assertEquals(1, members.length);
    Assert.assertEquals("immutable", members[0].getMid());

    //post member which is a collection to another collection -> member should be added, child collection should receive parent id as property 'memberOf'
    res = this.mockMvc.perform(get("/api/v1/collections/immutable").content(map.writeValueAsBytes(new MemberItem[]{item})).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andReturn();
    result = res.getResponse().getContentAsString();

    CollectionObject collection = map.readValue(result, CollectionObject.class);
    Assert.assertNotNull(collection);
    Assert.assertArrayEquals(new String[]{"1"}, collection.getProperties().getMemberOf().toArray(new String[]{}));

    //setting the mid should be enough here
    item = new MemberItem();
    item.setMid("m1");
    //add an existing member to another collection -> new member in new collection created, but should refer to same entity
    res = this.mockMvc.perform(post("/api/v1/collections/2/members").content(map.writeValueAsBytes(new MemberItem[]{item})).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isCreated()).andReturn();
    result = res.getResponse().getContentAsString();

    members = map.readValue(result, MemberItem[].class);
    Assert.assertNotNull(members);
    Assert.assertEquals(1, members.length);
    Assert.assertEquals("m1", members[0].getMid());
    //check description to prove that it's the same member
    Assert.assertEquals("First member", members[0].getDescription());
  }

  @Test
  public void testGetCollectionMembers() throws Exception{
    CollectionProperties collection2_props = CollectionProperties.getDefault();
    collection2_props.getMemberOf().add("1");

    CollectionItemMappingMetadata m1Mapping = new CollectionItemMappingMetadata();
    m1Mapping.setIndex(2);
    m1Mapping.setMemberRole("default");
    m1Mapping.setDateAdded(Instant.ofEpochMilli(0));

    CollectionItemMappingMetadata m2Mapping = new CollectionItemMappingMetadata();
    m2Mapping.setIndex(1);
    m2Mapping.setMemberRole("special");
    m2Mapping.setDateAdded(Instant.ofEpochMilli(0));

    CollectionCapabilities c1Caps = CollectionCapabilities.getDefault();
    c1Caps.setIsOrdered(Boolean.TRUE);

    TestDataCreationHelper.initialize(collectionDao, memberDao).
            addCollection("1", "Description", c1Caps, CollectionProperties.getDefault()).
            addCollection("2", collection2_props).
            addCollection("3", CollectionProperties.getDefault()).
            addMemberItem("1", "m1", "description", "customType", "localhost", "o1", m1Mapping).
            addMemberItem("1", "m2", "description", "customType", "localhost", "o2", m2Mapping).
            addMemberItem("1", "2", "localhost").
            addMemberItem("2", "m3", "localhost").
            persist();

    ObjectMapper map = new ObjectMapper();

    //get members of collection which does not exist -> should fail with HTTP 404
    this.mockMvc.perform(get("/api/v1/collections/999/members")).andDo(print()).andExpect(status().isNotFound()).andReturn();

    //get members from collection -> should return memberResultSet
    MvcResult res = this.mockMvc.perform(get("/api/v1/collections/1/members")).andDo(print()).andExpect(status().isOk()).andReturn();
    String result = res.getResponse().getContentAsString();
    MemberResultSet resultSet = map.readValue(result, MemberResultSet.class);
    Assert.assertNotNull(resultSet);
    Assert.assertEquals(3, resultSet.getContents().size());
    Assert.assertNotNull(IterableUtils.find(resultSet.getContents(), (m) -> {
      return m.getMid().equals("m1");
    }));
    Assert.assertNotNull(IterableUtils.find(resultSet.getContents(), (m) -> {
      return m.getMid().equals("m2");
    }));
    Assert.assertNotNull(IterableUtils.find(resultSet.getContents(), (m) -> {
      return m.getMid().equals("2");
    }));

    Assert.assertNull(resultSet.getNextCursor());
    Assert.assertNull(resultSet.getPrevCursor());

    ///
    //Filter Tests
    ///
    //find by member type, role and idnex -> expect 1 result
    res = this.mockMvc.perform(get("/api/v1/collections/1/members").param("f_datatype", "customType").param("f_role", "special").param("f_index", "1")).andDo(print()).andExpect(status().isOk()).andReturn();
    result = res.getResponse().getContentAsString();
    resultSet = map.readValue(result, MemberResultSet.class);
    Assert.assertNotNull(resultSet);
    Assert.assertEquals(1, resultSet.getContents().size());

    Assert.assertNotNull(IterableUtils.find(resultSet.getContents(), (m) -> {
      return m.getMid().equals("m2");
    }));

    //find by dateAdded OR anyRole
    res = this.mockMvc.perform(get("/api/v1/collections/1/members").param("f_dateAdded", DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC).format(Instant.ofEpochMilli(0))).param("f_role", "anyRole")).andDo(print()).andExpect(status().isOk()).andReturn();
    result = res.getResponse().getContentAsString();
    resultSet = map.readValue(result, MemberResultSet.class);
    Assert.assertNotNull(resultSet);
    Assert.assertEquals(2, resultSet.getContents().size());

    Assert.assertNotNull(IterableUtils.find(resultSet.getContents(), (m) -> {
      return m.getMid().equals("m1");
    }));

    Assert.assertNotNull(IterableUtils.find(resultSet.getContents(), (m) -> {
      return m.getMid().equals("m2");
    }));

    //return ordered
    res = this.mockMvc.perform(get("/api/v1/collections/1/members")).andDo(print()).andExpect(status().isOk()).andReturn();
    result = res.getResponse().getContentAsString();
    resultSet = map.readValue(result, MemberResultSet.class);
    Assert.assertNotNull(resultSet);
    Assert.assertEquals(3, resultSet.getContents().size());
    Assert.assertEquals("2", resultSet.getContents().get(0).getMid());
    Assert.assertEquals("m2", resultSet.getContents().get(1).getMid());
    Assert.assertEquals("m1", resultSet.getContents().get(2).getMid());

    ///
    //Pagination Tests
    ///
    //check page 0 with page size one -> return one element including next page link
    res = this.mockMvc.perform(get("/api/v1/collections/1/members").param("size", "1")).andDo(print()).andExpect(status().isOk()).andReturn();
    result = res.getResponse().getContentAsString();
    resultSet = map.readValue(result, MemberResultSet.class);
    Assert.assertNotNull(resultSet);
    Assert.assertEquals(1, resultSet.getContents().size());
    Assert.assertNotNull(resultSet.getNextCursor());
    Assert.assertNull(resultSet.getPrevCursor());

    //check page 0 with page size one -> return one element including next and prev page link
    res = this.mockMvc.perform(get("/api/v1/collections/1/members").param("page", "1").param("size", "1")).andDo(print()).andExpect(status().isOk()).andReturn();
    result = res.getResponse().getContentAsString();
    resultSet = map.readValue(result, MemberResultSet.class);
    Assert.assertNotNull(resultSet);
    Assert.assertEquals(1, resultSet.getContents().size());
    Assert.assertNotNull(resultSet.getNextCursor());
    Assert.assertNotNull(resultSet.getPrevCursor());

    //check page 1 with page size 2 -> return one element including prev page link
    res = this.mockMvc.perform(get("/api/v1/collections/1/members").param("page", "1").param("size", "2")).andDo(print()).andExpect(status().isOk()).andReturn();
    result = res.getResponse().getContentAsString();
    resultSet = map.readValue(result, MemberResultSet.class);
    Assert.assertNotNull(resultSet);
    Assert.assertEquals(1, resultSet.getContents().size());
    Assert.assertNull(resultSet.getNextCursor());
    Assert.assertNotNull(resultSet.getPrevCursor());

    //check page 0 of empty collection -> return no elements and no page links
    res = this.mockMvc.perform(get("/api/v1/collections/3/members")).andDo(print()).andExpect(status().isOk()).andReturn();
    result = res.getResponse().getContentAsString();
    resultSet = map.readValue(result, MemberResultSet.class);
    Assert.assertNotNull(resultSet);
    Assert.assertTrue(resultSet.getContents().isEmpty());
    Assert.assertNull(resultSet.getNextCursor());
    Assert.assertNull(resultSet.getPrevCursor());

    //get members from collection with expandDepth-> should return memberResultSet with members from collection 1 and 2
    res = this.mockMvc.perform(get("/api/v1/collections/1/members").param("expandDepth", "3")).andDo(print()).andExpect(status().isOk()).andReturn();
    result = res.getResponse().getContentAsString();
    resultSet = map.readValue(result, MemberResultSet.class);
    Assert.assertNotNull(resultSet);
    Assert.assertEquals(4, resultSet.getContents().size());
    Assert.assertNotNull(IterableUtils.find(resultSet.getContents(), (m) -> {
      return m.getMid().equals("m1");
    }));
    Assert.assertNotNull(IterableUtils.find(resultSet.getContents(), (m) -> {
      return m.getMid().equals("m2");
    }));
    Assert.assertNotNull(IterableUtils.find(resultSet.getContents(), (m) -> {
      return m.getMid().equals("2");
    }));
    Assert.assertNotNull(IterableUtils.find(resultSet.getContents(), (m) -> {
      return m.getMid().equals("m3");
    }));

    Assert.assertNull(resultSet.getNextCursor());
    Assert.assertNull(resultSet.getPrevCursor());

  }

  @Test
  public void testPostCollection() throws Exception{
    ObjectMapper map = new ObjectMapper();
    CollectionObject collection = new CollectionObject();
    collection.setId("c1");
    collection.setDescription("This is the first collection");
    collection.setCapabilities(CollectionCapabilities.getDefault());
    collection.setProperties(CollectionProperties.getDefault());

    //get members of collection which does not exist -> should fail with HTTP 404
    MvcResult res = this.mockMvc.perform(post("/api/v1/collections/").content(map.writeValueAsBytes(new CollectionObject[]{collection})).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isCreated()).andReturn();
    String result = res.getResponse().getContentAsString();
    CollectionObject[] collections = map.readValue(result, CollectionObject[].class);
    Assert.assertNotNull(collections);
    Assert.assertEquals(1, collections.length);
    Assert.assertEquals("c1", collections[0].getId());

    //post same collection id a second time -> returns HTTP 409
    this.mockMvc.perform(post("/api/v1/collections/").content(map.writeValueAsBytes(new CollectionObject[]{collection})).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isConflict()).andReturn();

    collection = new CollectionObject();
    collection.setDescription("This is a collection w/o id");
    collection.setCapabilities(CollectionCapabilities.getDefault());
    collection.setProperties(CollectionProperties.getDefault());

    //post collection w/o id -> should be created with local UUID
    res = this.mockMvc.perform(post("/api/v1/collections/").content(map.writeValueAsBytes(new CollectionObject[]{collection})).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isCreated()).andReturn();
    result = res.getResponse().getContentAsString();
    collections = map.readValue(result, CollectionObject[].class);
    Assert.assertNotNull(collections);
    Assert.assertEquals(1, collections.length);
    UUID id = UUID.fromString(collections[0].getId());
    Assert.assertNotNull(id);

    collection = new CollectionObject();
    collection.setId("2");
    collection.setDescription("This is a collection w/o id");

    //post collection w/o properties and capabilities -> should be created with default properties and capabilities
    res = this.mockMvc.perform(post("/api/v1/collections/").content(map.writeValueAsBytes(new CollectionObject[]{collection})).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isCreated()).andReturn();
    result = res.getResponse().getContentAsString();
    collections = map.readValue(result, CollectionObject[].class);
    Assert.assertNotNull(collections);
    Assert.assertEquals("2", collections[0].getId());
    Assert.assertEquals(1, collections.length);
    Assert.assertNotNull(collections[0].getProperties());
    Assert.assertNotNull(collections[0].getCapabilities());
  }

  @Test
  public void testPutCollection() throws Exception{
    CollectionCapabilities immutable_caps = CollectionCapabilities.getDefault();
    immutable_caps.setPropertiesAreMutable(Boolean.FALSE);

    TestDataCreationHelper.initialize(collectionDao, memberDao).
            addCollection("1", "This is the decription", CollectionCapabilities.getDefault(), CollectionProperties.getDefault()).
            addCollection("2", "This is the decription", immutable_caps, CollectionProperties.getDefault()).
            addCollection("3", "This is an invalid collection", null, CollectionProperties.getDefault()).
            persist();

    CollectionCapabilities newCaps = CollectionCapabilities.getDefault();
    newCaps.setIsOrdered(true);
    newCaps.setAppendsToEnd(false);
    newCaps.setSupportsRoles(true);
    newCaps.setMembershipIsMutable(false);
    newCaps.setPropertiesAreMutable(false);
    newCaps.setRestrictedToType("myType");
    newCaps.setMaxLength(10);

    CollectionProperties newProps = CollectionProperties.getDefault();
    newProps.setDateCreated(Instant.now());
    newProps.setOwnership("Tester");
    newProps.setLicense("Apache 2.0");
    newProps.setModelType("custom");
    newProps.setHasAccessRestrictions(true);
    newProps.getMemberOf().add("anotherCollection");
    newProps.setDescriptionOntology("myOntology");

    CollectionObject collectionTemplate = new CollectionObject();
    collectionTemplate.setId("1");
    collectionTemplate.setDescription("This is the first collection");
    collectionTemplate.setCapabilities(newCaps);
    collectionTemplate.setProperties(newProps);

    ObjectMapper map = new ObjectMapper();

    //put with wrong collection id -> returns HTTP 404
    this.mockMvc.perform(put("/api/v1/collections/999").content(map.writeValueAsBytes(collectionTemplate)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNotFound()).andReturn();

    MvcResult res = this.mockMvc.perform(get("/api/v1/collections/3").content(map.writeValueAsBytes(collectionTemplate)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andReturn();
    String etag = res.getResponse().getHeader("ETag");

    //put collection caps to collection which has no capabilities yet
    res = this.mockMvc.perform(put("/api/v1/collections/3").content(map.writeValueAsBytes(collectionTemplate)).header("If-Match", etag).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andReturn();
    String result = res.getResponse().getContentAsString();
    CollectionObject collection = map.readValue(result, CollectionObject.class);
    Assert.assertNotNull(collection.getCapabilities());

    res = this.mockMvc.perform(get("/api/v1/collections/1").content(map.writeValueAsBytes(collectionTemplate)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andReturn();
    etag = res.getResponse().getHeader("ETag");
    //put collection -> replace properties with provided values
    res = this.mockMvc.perform(put("/api/v1/collections/1").content(map.writeValueAsBytes(collectionTemplate)).header("If-Match", etag).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andReturn();
    result = res.getResponse().getContentAsString();
    collection = map.readValue(result, CollectionObject.class);
    Assert.assertNotNull(collection);
    Assert.assertEquals("1", collection.getId());
    Assert.assertEquals("This is the first collection", collection.getDescription());

    Assert.assertEquals(Boolean.TRUE, collection.getCapabilities().getIsOrdered());
    Assert.assertEquals(Boolean.FALSE, collection.getCapabilities().getAppendsToEnd());
    Assert.assertEquals(Boolean.TRUE, collection.getCapabilities().getSupportsRoles());
    Assert.assertEquals(Boolean.FALSE, collection.getCapabilities().getMembershipIsMutable());
    Assert.assertEquals(Boolean.FALSE, collection.getCapabilities().getPropertiesAreMutable());
    Assert.assertEquals("myType", collection.getCapabilities().getRestrictedToType());
    Assert.assertEquals(Integer.valueOf(10), collection.getCapabilities().getMaxLength());

    Assert.assertNotNull(collection.getProperties().getDateCreated());
    Assert.assertEquals("Tester", collection.getProperties().getOwnership());
    Assert.assertEquals("Apache 2.0", collection.getProperties().getLicense());
    Assert.assertEquals("custom", collection.getProperties().getModelType());
    Assert.assertEquals(Boolean.TRUE, collection.getCapabilities().getIsOrdered());
    Assert.assertTrue(collection.getProperties().getMemberOf().contains("anotherCollection"));
    Assert.assertEquals("myOntology", collection.getProperties().getDescriptionOntology());

    res = this.mockMvc.perform(get("/api/v1/collections/2").content(map.writeValueAsBytes(collectionTemplate)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andReturn();
    etag = res.getResponse().getHeader("ETag");
    //update on forbidden caps -> returns HTTP 403
    collectionTemplate.setId("2");
    this.mockMvc.perform(put("/api/v1/collections/2").content(map.writeValueAsBytes(collectionTemplate)).header("If-Match", etag).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isForbidden()).andReturn();
  }

  @Test
  public void testPutMember() throws Exception{
    TestDataCreationHelper.initialize(collectionDao, memberDao).
            addCollection("1", CollectionProperties.getDefault()).
            addMemberItem("1", "m1", "localhost").
            addMemberItem("1", "m2", "This is the decription", "customType", "localhost", "ontology", new CollectionItemMappingMetadata()).
            addMemberItem("1", "m3", "This is the decription", "customType", "localhost", "ontology", null).
            persist();

    CollectionItemMappingMetadata mappingMetadata = new CollectionItemMappingMetadata();
    mappingMetadata.setDateAdded(Instant.now());
    mappingMetadata.setDateUpdated(Instant.now());
    mappingMetadata.setIndex(5);
    mappingMetadata.setMemberRole("guest");

    ObjectMapper map = new ObjectMapper();

    MemberItem memberTemplate = new MemberItem();
    memberTemplate.setMid("m1");
    memberTemplate.setDescription("This is a member.");
    memberTemplate.setLocation("somewhere");
    memberTemplate.setOntology("o1");
    memberTemplate.setDatatype("type");
    memberTemplate.setMappings(mappingMetadata);

    //put with wrong collection id -> returns HTTP 404
    this.mockMvc.perform(put("/api/v1/collections/2/members/m1").content(map.writeValueAsBytes(memberTemplate)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNotFound()).andReturn();

    MvcResult res = this.mockMvc.perform(get("/api/v1/collections/1/members/m3")).andDo(print()).andExpect(status().isOk()).andReturn();
    String etag = res.getResponse().getHeader("ETag");

    res = this.mockMvc.perform(put("/api/v1/collections/1/members/m3").content(map.writeValueAsBytes(memberTemplate)).header("If-Match", etag).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andReturn();
    String result = res.getResponse().getContentAsString();
    MemberItem member = map.readValue(result, MemberItem.class);
    Assert.assertNotNull(member);
    Assert.assertNotNull(member.getMappings());

    res = this.mockMvc.perform(get("/api/v1/collections/1/members/m1")).andDo(print()).andExpect(status().isOk()).andReturn();
    etag = res.getResponse().getHeader("ETag");

    //put correct member and set membership metadata directly (membership 1-m1 contains no metadata at the beginning) -> returns HTTP 200 and member
    res = this.mockMvc.perform(put("/api/v1/collections/1/members/m1").content(map.writeValueAsBytes(memberTemplate)).header("If-Match", etag).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andReturn();
    result = res.getResponse().getContentAsString();
    member = map.readValue(result, MemberItem.class);

    Assert.assertNotNull(member);
    Assert.assertEquals("m1", member.getMid());
    Assert.assertEquals("This is a member.", member.getDescription());
    Assert.assertEquals("type", member.getDatatype());
    Assert.assertEquals("o1", member.getOntology());
    Assert.assertEquals("somewhere", member.getLocation());

    Assert.assertNotNull(member.getMappings().getDateAdded());
    Assert.assertNotNull(member.getMappings().getDateUpdated());

    Assert.assertEquals(Integer.valueOf(5), member.getMappings().getIndex());
    Assert.assertEquals("guest", member.getMappings().getMemberRole());

    memberTemplate.setMid("m2");

    res = this.mockMvc.perform(get("/api/v1/collections/1/members/m2")).andDo(print()).andExpect(status().isOk()).andReturn();
    etag = res.getResponse().getHeader("ETag");

    //put correct member and copy membership metadata attributes -> returns HTTP 200 and member
    res = this.mockMvc.perform(put("/api/v1/collections/1/members/m2").content(map.writeValueAsBytes(memberTemplate)).header("If-Match", etag).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andReturn();
    result = res.getResponse().getContentAsString();
    member = map.readValue(result, MemberItem.class);

    Assert.assertNotNull(member);
    Assert.assertEquals("m2", member.getMid());
    Assert.assertEquals("This is a member.", member.getDescription());
    Assert.assertEquals("type", member.getDatatype());
    Assert.assertEquals("o1", member.getOntology());
    Assert.assertEquals("somewhere", member.getLocation());

    Assert.assertNotNull(member.getMappings().getDateAdded());
    Assert.assertNotNull(member.getMappings().getDateUpdated());

    Assert.assertEquals(Integer.valueOf(5), member.getMappings().getIndex());
    Assert.assertEquals("guest", member.getMappings().getMemberRole());

  }

  @Test
  public void testOpsFindMatch() throws Exception{

    CollectionItemMappingMetadata m1Mapping = new CollectionItemMappingMetadata();
    m1Mapping.setIndex(2);
    CollectionItemMappingMetadata m2Mapping = new CollectionItemMappingMetadata();
    m2Mapping.setIndex(1);

    CollectionCapabilities c1caps = CollectionCapabilities.getDefault();
    c1caps.setIsOrdered(Boolean.TRUE);
    CollectionCapabilities c2caps = CollectionCapabilities.getDefault();
    c2caps.setIsOrdered(Boolean.FALSE);

    TestDataCreationHelper.initialize(collectionDao, memberDao).
            addCollection("1", "description", c1caps, CollectionProperties.getDefault()).
            addCollection("2", "description", c2caps, CollectionProperties.getDefault()).
            addMemberItem("1", "m1", "This is member one", "customType", "localhost", "ontology", m1Mapping).
            addMemberItem("1", "m2", "This is member two", "customType", "localhost", "ontology", m2Mapping).
            addMemberItem("1", "m3", "localhost").
            addMemberItem("2", "m1", "localhost").
            addMemberItem("2", "m3", "localhost").
            persist();
    ObjectMapper map = new ObjectMapper();

    MemberItem example = new MemberItem();
    example.setDatatype("customType");

    //search in invalid collection -> returns HTTP 404
    this.mockMvc.perform(post("/api/v1/collections/666/ops/findMatch").content(map.writeValueAsBytes(example)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNotFound()).andReturn();

    //search for type and return result ordered
    MvcResult res = this.mockMvc.perform(post("/api/v1/collections/1/ops/findMatch").content(map.writeValueAsBytes(example)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andReturn();
    String result = res.getResponse().getContentAsString();
    MemberResultSet members = map.readValue(result, MemberResultSet.class);

    Assert.assertNotNull(members);
    Assert.assertEquals(2, members.getContents().size());

    Assert.assertEquals("m2", members.getContents().get(0).getMid());
    Assert.assertEquals("m1", members.getContents().get(1).getMid());

    example.setDatatype(null);
    example.setLocation("localhost");

    //search for type and return result not ordered
    res = this.mockMvc.perform(post("/api/v1/collections/2/ops/findMatch").content(map.writeValueAsBytes(example)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andReturn();
    result = res.getResponse().getContentAsString();
    members = map.readValue(result, MemberResultSet.class);

    Assert.assertNotNull(members);
    Assert.assertEquals(2, members.getContents().size());

    Assert.assertEquals("m1", members.getContents().get(0).getMid());
    Assert.assertEquals("m3", members.getContents().get(1).getMid());

    example.setDatatype(null);
    example.setDescription("This is member two");

    //search for description
    res = this.mockMvc.perform(post("/api/v1/collections/1/ops/findMatch").content(map.writeValueAsBytes(example)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andReturn();
    result = res.getResponse().getContentAsString();
    members = map.readValue(result, MemberResultSet.class);

    Assert.assertNotNull(members);
    Assert.assertEquals(1, members.getContents().size());

    example.setDescription(null);
    example.setOntology("ontology");

    //search for ontology
    res = this.mockMvc.perform(post("/api/v1/collections/1/ops/findMatch").content(map.writeValueAsBytes(example)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andReturn();
    result = res.getResponse().getContentAsString();
    members = map.readValue(result, MemberResultSet.class);

    Assert.assertNotNull(members);
    Assert.assertEquals(2, members.getContents().size());

    example.setOntology(null);
    example.setLocation("localhost");

    //search for location
    res = this.mockMvc.perform(post("/api/v1/collections/1/ops/findMatch").content(map.writeValueAsBytes(example)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andReturn();
    result = res.getResponse().getContentAsString();
    members = map.readValue(result, MemberResultSet.class);

    Assert.assertNotNull(members);
    Assert.assertEquals(3, members.getContents().size());

    example.setLocation(null);
    example.setMid("m2");

    //search for mid
    res = this.mockMvc.perform(post("/api/v1/collections/1/ops/findMatch").content(map.writeValueAsBytes(example)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andReturn();
    result = res.getResponse().getContentAsString();
    members = map.readValue(result, MemberResultSet.class);

    Assert.assertNotNull(members);
    Assert.assertEquals(1, members.getContents().size());

    example.setMid("666");

    //search for mid with no match
    res = this.mockMvc.perform(post("/api/v1/collections/1/ops/findMatch").content(map.writeValueAsBytes(example)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andReturn();
    result = res.getResponse().getContentAsString();
    members = map.readValue(result, MemberResultSet.class);

    Assert.assertNotNull(members);
    Assert.assertTrue(members.getContents().isEmpty());

  }

  @Test
  public void testOpsFlatten() throws Exception{
    CollectionProperties collection2_props = CollectionProperties.getDefault();
    collection2_props.getMemberOf().add("1");

    TestDataCreationHelper.initialize(collectionDao, memberDao).
            addCollection("1", CollectionProperties.getDefault()).
            addCollection("2", collection2_props).
            addCollection("3", CollectionProperties.getDefault()).
            addMemberItem("1", "m1", "localhost").
            addMemberItem("1", "m2", "localhost").
            addMemberItem("1", "2", "localhost").
            addMemberItem("2", "m3", "localhost").
            persist();
    ObjectMapper map = new ObjectMapper();

    MvcResult res = this.mockMvc.perform(get("/api/v1/collections/1/ops/flatten").contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andReturn();
    String result = res.getResponse().getContentAsString();
    MemberResultSet members = map.readValue(result, MemberResultSet.class);

    Assert.assertNotNull(members);
    Assert.assertEquals(4, members.getContents().size());
  }

  @Test
  public void testOpsUnion() throws Exception{
    TestDataCreationHelper.initialize(collectionDao, memberDao).
            addCollection("1", CollectionProperties.getDefault()).
            addCollection("2", CollectionProperties.getDefault()).
            addMemberItem("1", "m1", "localhost").
            addMemberItem("1", "m2", "localhost").
            addMemberItem("2", "m3", "localhost").
            addMemberItem("2", "m1", "localhost").
            persist();

    ObjectMapper map = new ObjectMapper();

    MvcResult res = this.mockMvc.perform(get("/api/v1/collections/1/ops/union/2").contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andReturn();
    String result = res.getResponse().getContentAsString();
    MemberResultSet members = map.readValue(result, MemberResultSet.class);

    Assert.assertNotNull(members);
    Assert.assertEquals(4, members.getContents().size());

    //test left collection not found
    this.mockMvc.perform(get("/api/v1/collections/666/ops/union/2").contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNotFound()).andReturn();
    //test right collection not found
    this.mockMvc.perform(get("/api/v1/collections/1/ops/union/666").contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNotFound()).andReturn();

  }

  @Test
  public void testOpsIntersection() throws Exception{
    CollectionItemMappingMetadata m1mapping = CollectionItemMappingMetadata.getDefault();
    m1mapping.setIndex(3);
    CollectionItemMappingMetadata m2mapping = CollectionItemMappingMetadata.getDefault();
    m2mapping.setIndex(1);
    CollectionItemMappingMetadata m3mapping = CollectionItemMappingMetadata.getDefault();
    m3mapping.setIndex(2);
    CollectionItemMappingMetadata m12mapping = CollectionItemMappingMetadata.getDefault();
    m12mapping.setIndex(4);

    CollectionCapabilities c1caps = CollectionCapabilities.getDefault();
    c1caps.setIsOrdered(Boolean.TRUE);
    CollectionCapabilities c2caps = CollectionCapabilities.getDefault();
    c2caps.setIsOrdered(Boolean.TRUE);

    TestDataCreationHelper.initialize(collectionDao, memberDao).
            addCollection("1", "description", c1caps, CollectionProperties.getDefault()).
            addCollection("2", "description", c2caps, CollectionProperties.getDefault()).
            addCollection("3", CollectionProperties.getDefault()).
            addMemberItem("1", "m2", "localhost").
            addMemberItem("1", "m1", "description", "type", "localhost", "o1", m1mapping).
            addMemberItem("2", "m3", "description", "type", "localhost", "o1", m3mapping).
            addMemberItem("2", "m1", "description", "type", "localhost", "o1", m12mapping).
            addMemberItem("2", "m2", "description", "type", "localhost", "o1", m2mapping).
            addMemberItem("3", "m1", "localhost").
            addMemberItem("3", "m2", "localhost").
            addMemberItem("3", "m3", "localhost").
            persist();

    ObjectMapper map = new ObjectMapper();

    MvcResult res = this.mockMvc.perform(get("/api/v1/collections/1/ops/intersection/2").contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andReturn();
    String result = res.getResponse().getContentAsString();
    MemberResultSet members = map.readValue(result, MemberResultSet.class);

    Assert.assertNotNull(members);
    Assert.assertEquals(2, members.getContents().size());
    Assert.assertEquals("m2", members.getContents().get(0).getMid());
    Assert.assertEquals("m1", members.getContents().get(1).getMid());

    //get members of intersection of 1 and 3...should be two members m1 and m2 returned in the order they were added, not ordered by index 
    res = this.mockMvc.perform(get("/api/v1/collections/1/ops/intersection/3").contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andReturn();
    result = res.getResponse().getContentAsString();
    members = map.readValue(result, MemberResultSet.class);

    Assert.assertNotNull(members);
    Assert.assertEquals(2, members.getContents().size());
    Assert.assertNotNull(IterableUtils.find(members.getContents(), (m) -> {
      return m.getMid().equals("m1");
    }));
    Assert.assertNotNull(IterableUtils.find(members.getContents(), (m) -> {
      return m.getMid().equals("m2");
    }));
    //test left collection not found
    this.mockMvc.perform(get("/api/v1/collections/666/ops/intersection/2").contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNotFound()).andReturn();
    //test right collection not found
    this.mockMvc.perform(get("/api/v1/collections/1/ops/intersection/666").contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNotFound()).andReturn();

  }

}
