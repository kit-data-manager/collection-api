/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.datamanager.collection.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author chelbi
 */
@Configuration
@Getter
public class ApplicationProperties {

    public static Integer maxExpansionDepth;
    
     @Value("${collection.serviceFeature.maxExpansionDepth}")
     public void setMaxExpansionDepth(Integer maxExpansionDepth){
         this.maxExpansionDepth=maxExpansionDepth;
     }
}
