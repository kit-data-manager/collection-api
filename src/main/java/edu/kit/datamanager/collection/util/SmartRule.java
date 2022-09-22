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
package edu.kit.datamanager.collection.util;

import edu.kit.datamanager.collection.domain.MemberItem;
import edu.kit.datamanager.collection.exceptions.SmartRuleParseException;
import java.util.Arrays;
import lombok.Data;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import static org.hamcrest.Matchers.hasProperty;

/**
 *
 * @author jejkal
 */
@Data
public class SmartRule {

    public enum COMPARATOR {
        STARTS_WITH,
        ENDS_WITH,
        CONTAINS,
        NOT_CONTAINS,
        EQUALS,
        NOT_EQUALS,
        EQUALS_IGNORE_CASE,
        MATCHES
    }

    public enum FIELD {
        MEMBER_ID("mid"),
        DATA_TYPE("datatype"),
        DESCRIPTION("description"),
        LOCATION("location"),
        ONTOLOGY("ontology"),
        MEMBER_ROLE("mappings.memberRole");

        private String fieldName = null;

        FIELD(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getFieldName() {
            return fieldName;
        }
    }

    private FIELD field = null;
    private COMPARATOR comparator = null;
    private String comparatorTerm = null;
    private String targetCollectionId = null;

    SmartRule() {
    }

    public static SmartRule fromString(String ruleAsString) throws SmartRuleParseException {
        SmartRule result = new SmartRule();
        int firstSpace = ruleAsString.indexOf(" ");
        try {
            result.field = FIELD.valueOf(ruleAsString.substring(0, firstSpace));
        } catch (IllegalArgumentException e) {
            throw new SmartRuleParseException("Invalid field value in rule string \"" + ruleAsString + "\". Allowed values are: " + Arrays.asList(FIELD.values()));
        }
        int nextSpace = ruleAsString.indexOf(" ", firstSpace + 1);
        try {
            result.comparator = COMPARATOR.valueOf(ruleAsString.substring(firstSpace + 1, nextSpace));
        } catch (IllegalArgumentException e) {
            throw new SmartRuleParseException("Invalid comparator value in rule string \"" + ruleAsString + "\". Allowed values are: " + Arrays.asList(COMPARATOR.values()));
        }
        int firstQuoteIndex = ruleAsString.indexOf("'");
        int lastQuoteIndex = ruleAsString.lastIndexOf("'");

        if (firstQuoteIndex <= 0 || lastQuoteIndex <= 0 || firstQuoteIndex == lastQuoteIndex || firstQuoteIndex < nextSpace) {
            throw new SmartRuleParseException("Invalid comparator term in rule string \"" + ruleAsString + "\". Term should be provided in quotes after comparator and before > .");
        }
        result.comparatorTerm = ruleAsString.substring(firstQuoteIndex + 1, lastQuoteIndex);

        int redirectChar = ruleAsString.lastIndexOf(">");
        if (redirectChar <= 0 || redirectChar < lastQuoteIndex) {
            throw new SmartRuleParseException("Invalid/missing target collection assignment in rule string \"" + ruleAsString + "\". Target collection id should be separated by > from the comparator term.");
        }
        result.targetCollectionId = ruleAsString.substring(redirectChar + 1).trim();
        if (result.targetCollectionId == null || result.targetCollectionId.length() < 1) {
            throw new SmartRuleParseException("Invalid/missing target collection in rule string \"" + ruleAsString + "\". Target collection id length must be at least 1.");
        }
        return result;
    }

    public boolean matches(MemberItem item) {
        Matcher<String> valueMatcher = null;
        switch (comparator) {
            case CONTAINS:
                valueMatcher = Matchers.containsString(comparatorTerm);
                break;
            case NOT_CONTAINS:
                valueMatcher = Matchers.not(Matchers.containsString(comparatorTerm));
                break;
            case STARTS_WITH:
                valueMatcher = Matchers.startsWith(comparatorTerm);
                break;
            case ENDS_WITH:
                valueMatcher = Matchers.endsWith(comparatorTerm);
                break;
            case EQUALS:
                valueMatcher = Matchers.equalTo(comparatorTerm);
                break;
            case EQUALS_IGNORE_CASE:
                valueMatcher = Matchers.equalToIgnoringCase(comparatorTerm);
                break;
            case NOT_EQUALS:
                valueMatcher = Matchers.not(Matchers.equalTo(comparatorTerm));
                break;
            case MATCHES:
                valueMatcher = Matchers.matchesRegex(comparatorTerm);
                break;
        }
        Matcher<MemberItem> matcher;
        switch (field) {
            case MEMBER_ROLE:
                matcher = hasProperty("mappings", hasProperty("memberRole", valueMatcher));
                break;
            default:
                matcher = hasProperty(field.getFieldName(), valueMatcher);
                break;
        }

        return matcher.matches(item);
    }

    @Override
    public String toString() {
        return field + " " + comparator + " '" + comparatorTerm + "' > " + targetCollectionId;
    }
}
