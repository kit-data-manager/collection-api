/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.datamanager.collection.domain;

import edu.kit.datamanager.collection.exceptions.CircularDependencyException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author chelbi
 */
@ToString
public class CollectionGraph {
 
    private  Map<String,Set<String>> collectionGraph; 
  
    private static final Logger LOG = LoggerFactory.getLogger(CollectionGraph.class);
    public CollectionGraph(int nodesNumber){
        collectionGraph = new HashMap<>(nodesNumber); 
    }
    
    public void addEdge(String collection, Set<String> parents) { 
        LOG.trace("Structure of collection graph before adding the parents {} to the collection {}: {}", parents, collection, collectionGraph.toString());
        if (collectionGraph.containsKey(collection)){
            parents.addAll(collectionGraph.get(collection));
            collectionGraph.put(collection, parents);
        }
        collectionGraph.put(collection, parents); 
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
        collectionGraph.get(childId).remove(parentId);
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
