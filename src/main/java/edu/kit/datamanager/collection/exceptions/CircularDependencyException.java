/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.datamanager.collection.exceptions;

/**
 *
 * @author chelbi
 */
public class CircularDependencyException extends RuntimeException {
    
    public CircularDependencyException(String message){
        super(message);
  }
}
