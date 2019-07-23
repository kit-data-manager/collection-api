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

import edu.kit.datamanager.collection.domain.CollectionItemMappingMetadata;
import edu.kit.datamanager.collection.domain.MemberItem;
import java.time.Instant;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jejkal
 */
public class MemberItemTest{

  @Test
  public void testCopyFrom(){
    MemberItem item = new MemberItem();
    item.setId(1l);
    item.setMid("m1");
    item.setDescription("Item 1");
    item.setLocation("localhost");
    item.setOntology("o1");
    item.setDatatype("type1");
    CollectionItemMappingMetadata md = new CollectionItemMappingMetadata();
    md.setDateAdded(Instant.now());
    md.setDateUpdated(Instant.now());
    md.setId(1l);
    md.setIndex(1);
    md.setMemberRole("member");
    item.setMappings(md);

    MemberItem copy = new MemberItem();
    copy.copyFrom(item);

    Assert.assertEquals(item.getId(), copy.getId());
    Assert.assertEquals(item.getMid(), copy.getMid());
    Assert.assertEquals(item.getLocation(), copy.getLocation());
    Assert.assertEquals(item.getDescription(), copy.getDescription());
    Assert.assertEquals(item.getDatatype(), copy.getDatatype());
    Assert.assertEquals(item.getOntology(), copy.getOntology());
    Assert.assertEquals(item.getMappings().getId(), copy.getMappings().getId());
    Assert.assertEquals(item.getMappings().getIndex(), copy.getMappings().getIndex());
    Assert.assertEquals(item.getMappings().getMemberRole(), copy.getMappings().getMemberRole());
    Assert.assertEquals(item.getMappings().getDateAdded(), copy.getMappings().getDateAdded());
    Assert.assertEquals(item.getMappings().getDateUpdated(), copy.getMappings().getDateUpdated());

    Assert.assertEquals(item, copy);
  }

  @Test
  public void testCopy(){
    MemberItem item = new MemberItem();
    item.setId(1l);
    item.setMid("m1");
    item.setDescription("Item 1");
    item.setLocation("localhost");
    item.setOntology("o1");
    item.setDatatype("type1");
    CollectionItemMappingMetadata md = new CollectionItemMappingMetadata();
    md.setDateAdded(Instant.now());
    md.setDateUpdated(Instant.now());
    md.setId(1l);
    md.setIndex(1);
    md.setMemberRole("member");
    item.setMappings(md);

    MemberItem copy = MemberItem.copy(item);

    Assert.assertEquals(item.getId(), copy.getId());
    Assert.assertEquals(item.getMid(), copy.getMid());
    Assert.assertEquals(item.getLocation(), copy.getLocation());
    Assert.assertEquals(item.getDescription(), copy.getDescription());
    Assert.assertEquals(item.getDatatype(), copy.getDatatype());
    Assert.assertEquals(item.getOntology(), copy.getOntology());
    Assert.assertEquals(item.getMappings().getId(), copy.getMappings().getId());
    Assert.assertEquals(item.getMappings().getIndex(), copy.getMappings().getIndex());
    Assert.assertEquals(item.getMappings().getMemberRole(), copy.getMappings().getMemberRole());
    Assert.assertEquals(item.getMappings().getDateAdded(), copy.getMappings().getDateAdded());
    Assert.assertEquals(item.getMappings().getDateUpdated(), copy.getMappings().getDateUpdated());

    Assert.assertEquals(item, copy);

  }

}
