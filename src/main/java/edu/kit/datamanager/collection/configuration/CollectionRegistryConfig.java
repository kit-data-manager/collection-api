/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.datamanager.collection.configuration;

import edu.kit.datamanager.collection.dao.ICollectionObjectDao;
import edu.kit.datamanager.collection.domain.CollectionGraph;
import edu.kit.datamanager.collection.domain.CollectionObject;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author chelbi
 */
@Configuration
@Getter
@Setter
public class CollectionRegistryConfig {
    
    @Autowired
    private ICollectionObjectDao collectionDao;

    private CollectionGraph collectionGraph;
    
    private static final Logger LOG = LoggerFactory.getLogger(CollectionRegistryConfig.class);
   
    @Bean
    public void configure(){ 
        List<CollectionObject> existingCollections = collectionDao.findAll();
         collectionGraph = new CollectionGraph(existingCollections.size());
        for (CollectionObject collectionObject : existingCollections) {
            Set<String> collectionMemberOfs = collectionObject.getProperties().getMemberOf();
            collectionGraph.addEdge(collectionObject.getId(), collectionMemberOfs);  
        } 
         LOG.trace("Configuration of the structure of Collection Graph {}", collectionGraph.toString());
    } 
}
