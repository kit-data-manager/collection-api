/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.datamanager.collection.web;

import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author chelbi
 */
public interface CollectionApiUI {

    public ModelAndView createNewCollection();
    public ModelAndView updateCollection(String id);
     public ModelAndView deleteCollection(String id);
}
