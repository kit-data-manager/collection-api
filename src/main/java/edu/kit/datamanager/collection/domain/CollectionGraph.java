/*
 * Copyright 2018 Karlsruhe Institute of Technology.
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
package edu.kit.datamanager.collection.domain;

import edu.kit.datamanager.collection.exceptions.CircularDependencyException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author chelbi
 */
@ToString
@Getter
public class CollectionGraph {
 
    private  Map<String,Set<String>> collectionGraph; 
  
    private static final Logger LOG = LoggerFactory.getLogger(CollectionGraph.class);
    public CollectionGraph(int nodesNumber){
        collectionGraph = new HashMap<>(nodesNumber); 
    }
    
    public void addEdge(String collection, Set<String> parents) { 
        LOG.trace("Structure of collection graph before adding the parents {} to the collection {}: {}", parents, collection, collectionGraph.toString());
        Set<String> edges = collectionGraph.get(collection);
        if(edges == null){
            edges = new HashSet<>();
            collectionGraph.put(collection, edges);
        }
        edges.addAll(parents);
         LOG.trace("Structure of collection graph after adding the parents {} to the collection {}: {}", parents, collection, collectionGraph.toString());
    } 
     public void addEdge(String collection, String parent) { 
         Set<String> parentList = new HashSet<>();
         parentList.add(parent);
         addEdge(collection, parentList);
    }
    
    public void removeCollection(String collectionId){
        LOG.trace("Structure of collection graph before deleting a collection with id {}: {}", collectionId, collectionGraph.toString());
        collectionGraph.remove(collectionId);
        for (Map.Entry<String,Set<String>> entries : collectionGraph.entrySet()) {
            if (entries.getValue().contains(collectionId)) {
                entries.getValue().remove(collectionId);
            }
        }
         LOG.trace("Structure of collection graph after deleting a collection with id {}: {}", collectionId, collectionGraph.toString());
    }
    
    public void removeParentFromChild(String parentId, String childId){
        LOG.trace("Structure of Collection graph before deleting the relationship between {} and {}: {} ", parentId, childId, collectionGraph.toString());
        if (collectionGraph.get(childId) != null){
            collectionGraph.get(childId).remove(parentId);
        }
        LOG.trace("Structure of Collection graph after deleting the relationship between {} and {}: {} ", parentId, childId, collectionGraph.toString());
    }
    
   public void isCircular(String parentId, String childId){
    Set<String> parents= collectionGraph.get(parentId);
    for(String itemId: parents){
         if (itemId.equals(childId)){
             throw new CircularDependencyException("Circular dependency found");
         }
         isCircular(itemId, childId);
    }
   }
}
