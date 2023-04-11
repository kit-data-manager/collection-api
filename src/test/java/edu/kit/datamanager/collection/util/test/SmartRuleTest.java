/*
 * Copyright 2022 Karlsruhe Institute of Technology.
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
package edu.kit.datamanager.collection.util.test;

import edu.kit.datamanager.collection.domain.CollectionItemMappingMetadata;
import edu.kit.datamanager.collection.domain.MemberItem;
import edu.kit.datamanager.collection.exceptions.SmartRuleParseException;
import edu.kit.datamanager.collection.util.SmartRule;
import org.hamcrest.MatcherAssert;
import static org.hamcrest.Matchers.hasProperty;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jejkal
 */
public class SmartRuleTest {

    @Test
    public void testFieldNames() {
        MemberItem item = new MemberItem();
        item.setMid("m1");
        item.setLocation("file:///tmp/data.file");
        item.setDatatype("application/json");
        CollectionItemMappingMetadata md = new CollectionItemMappingMetadata();
        md.setMemberRole("read");
        item.setMappings(md);
        for (SmartRule.FIELD field : SmartRule.FIELD.values()) {
            if (!SmartRule.FIELD.MEMBER_ROLE.equals(field)) {
                MatcherAssert.assertThat(item, hasProperty(field.getFieldName()));
            } else {
                MatcherAssert.assertThat(item, hasProperty(field.getFieldName().split("\\.")[0], hasProperty(field.getFieldName().split("\\.")[1])));
            }
        }
    }

    @Test
    public void testWrongFieldError() {
        parseInvalidRuleString("MEMBER_ID_Invalid EQUALS 'm1' > test_collection");
    }

    @Test
    public void testMissingFieldError() {
        parseInvalidRuleString("EQUALS 'm1' > test_collection");
    }

    @Test
    public void testWrongComparatorError() {
        parseInvalidRuleString("MEMBER_ID EQUALLY 'm1' > test_collection");
    }

    @Test
    public void testMissingComparatorError() {
        parseInvalidRuleString("MEMBER_ID 'm1' > test_collection");
    }

    @Test
    public void testWrongComparatorTermError() {
        parseInvalidRuleString("MEMBER_ID EQUALS 'm1 > test_collection");
        parseInvalidRuleString("MEMBER_ID EQUALS m1 > test_collection");
        parseInvalidRuleString("MEMBER_ID EQUALS m1' > test_collection");
        parseInvalidRuleString("MEMBER_ID EQUALS > test_collection");
    }

    @Test
    public void testWrongTargetCollectionError() {
        parseInvalidRuleString("MEMBER_ID EQUALS 'm1' test_collection");
        parseInvalidRuleString("MEMBER_ID EQUALS 'm1' > ");
        parseInvalidRuleString("MEMBER_ID EQUALS 'm1'");
    }

    @Test
    public void testFieldRuleCreation() {
        for (SmartRule.FIELD field : SmartRule.FIELD.values()) {
            String s = field.toString() + " EQUALS 'm1' > test_collection";
            SmartRule r = parseValidRuleString(s);
            Assert.assertEquals(s, r.toString());
        }
    }

    @Test
    public void testComparatorRuleCreation() {
        for (SmartRule.COMPARATOR comparator : SmartRule.COMPARATOR.values()) {
            String s = "MEMBER_ID " + comparator.toString() + " 'm1' > test_collection";
            SmartRule r = parseValidRuleString(s);
            Assert.assertEquals(s, r.toString());
        }
    }

    @Test
    public void testSmartRuleMatching() throws SmartRuleParseException {
        MemberItem item = new MemberItem();
        item.setMid("m1");
        item.setLocation("file:///tmp/data.file");
        item.setDatatype("application/json");
        CollectionItemMappingMetadata md = new CollectionItemMappingMetadata();
        md.setMemberRole("read");
        item.setMappings(md);

        Assert.assertTrue(SmartRule.fromString("MEMBER_ID STARTS_WITH 'm' > test_collection").matches(item));
        Assert.assertTrue(SmartRule.fromString("MEMBER_ID ENDS_WITH '1' > test_collection").matches(item));
        Assert.assertTrue(SmartRule.fromString("LOCATION CONTAINS 'file://' > test_collection").matches(item));
        Assert.assertTrue(SmartRule.fromString("LOCATION NOT_CONTAINS 'http://' > test_collection").matches(item));
        Assert.assertTrue(SmartRule.fromString("DATA_TYPE EQUALS 'application/json' > test_collection").matches(item));
        Assert.assertTrue(SmartRule.fromString("DATA_TYPE NOT_EQUALS 'application/xml' > test_collection").matches(item));
        Assert.assertTrue(SmartRule.fromString("DATA_TYPE EQUALS_IGNORE_CASE 'ApPlIcAtIoN/jSoN' > test_collection").matches(item));
        Assert.assertTrue(SmartRule.fromString("DATA_TYPE MATCHES '^[-\\w.]+/[-\\w.]+$' > test_collection").matches(item));
        //additional tests for member role as its handling is different from the other fields
        Assert.assertTrue(SmartRule.fromString("MEMBER_ROLE EQUALS 'read' > test_collection").matches(item));
        Assert.assertTrue(SmartRule.fromString("MEMBER_ROLE NOT_EQUALS 'write' > test_collection").matches(item));
    }

    private SmartRule parseValidRuleString(String ruleString) {
        try {
            return SmartRule.fromString(ruleString);
        } catch (SmartRuleParseException ex) {
            Assert.fail("Failed to parse smart rule. Message: " + ex.getMessage());
        }
        return null;
    }

    private void parseInvalidRuleString(String ruleString) {
        try {
            SmartRule.fromString(ruleString);
            Assert.fail("Expected SmartRuleParseException while parsing SmartRule " + ruleString + ".");
        } catch (SmartRuleParseException ex) {
            //successfully failed
        }
    }

    /*MEMBER_ID("mid"),
        DATA_TYPE("datatype"),
        DESCRIPTION("description"),
        LOCATION("location"),
        ONTOLOGY("ontology"),
        MEMBER_ROLE("mappings.memberRole");*/
}
