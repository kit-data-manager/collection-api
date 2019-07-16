package edu.kit.datamanager.collection.domain;

import com.fasterxml.jackson.annotation.JsonValue;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Valid operation names.
 */
public enum CollectionOperations {
  
  FINDMATCH("findMatch"),
  
  INTERSECTION("intersection"),
  
  UNION("union"),
  
  FLATTEN("flatten");

  private String value;

  CollectionOperations(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static CollectionOperations fromValue(String text) {
    for (CollectionOperations b : CollectionOperations.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}

