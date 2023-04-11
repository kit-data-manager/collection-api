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
package edu.kit.datamanager.collection.web.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.datamanager.collection.dao.ICollectionObjectDao;
import edu.kit.datamanager.collection.domain.CollectionObject;
import edu.kit.datamanager.collection.domain.MemberItem;
import edu.kit.datamanager.collection.domain.MemberResultSet;
import edu.kit.datamanager.collection.dto.EditorRequestCollection;
import edu.kit.datamanager.collection.dto.EditorRequestMember;
import edu.kit.datamanager.collection.dto.TabulatorItems;
import edu.kit.datamanager.collection.web.CollectionApiUI;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author chelbi
 */
@Controller
public class CollectionApiUIImpl implements CollectionApiUI {

    private final static Resource DATAMODEL_COLLECTION= new ClassPathResource("/static/jsonSchemas/dataModelCollection.json");
    private final static Resource UIFORM_COLLECTION= new ClassPathResource("/static/jsonSchemas/uiFormCollection.json");
    private final static Resource ITEMS_COLLECTION= new ClassPathResource("/static/jsonSchemas/itemsCollection.json");

    private final static Resource DATAMODEL_MEMBER = new ClassPathResource("/static/jsonSchemas/dataModelMember.json");
    private final static Resource UIFORM_MEMBER= new ClassPathResource("/static/jsonSchemas/uiFormMember.json");
    private final static Resource ITEMS_MEMBER= new ClassPathResource("/static/jsonSchemas/itemsMember.json");

    @Autowired
    private ICollectionObjectDao collectionDao;

    @Autowired
    private CollectionsApiController collectionController;
    
    private final HttpServletRequest request;
    
    @org.springframework.beans.factory.annotation.Autowired
    public CollectionApiUIImpl(HttpServletRequest request) {
        this.request = request;
    }

    @RequestMapping("/collections")
    @Override
    public ModelAndView collections() {
        List<CollectionObject> collections = collectionDao.findAll();

        EditorRequestCollection request = EditorRequestCollection.builder()
                .dataModel(getJsonObject(DATAMODEL_COLLECTION))
                .uiForm(getJsonObject(UIFORM_COLLECTION))
                .collections(collections)
                .items(getJsonArrayOfItems(ITEMS_COLLECTION)).build();

        ModelAndView model = new ModelAndView("collections");
        model.addObject("request", request);
        return model;
    }

    @RequestMapping("/collections/{id}/members")
    @Override
    public ModelAndView members(@PathVariable(value = "id", required = true) String id) {
        Pageable pgbl = PageRequest.of(0, 20, Sort.unsorted());
        
       // String path = request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        //id= path.substring(path.indexOf("/collections/")+("/collections/").length(), path.lastIndexOf("/members"));
 
        ResponseEntity<MemberResultSet> memberResultSet = collectionController.collectionsIdMembersGet(id, null, null, null, null, null, pgbl);

        List<MemberItem> memberItems = memberResultSet.getBody().getContents();

        EditorRequestMember memberRequest = EditorRequestMember.builder()
                .dataModel(getJsonObject(DATAMODEL_MEMBER))
                .uiForm(getJsonObject(UIFORM_MEMBER))
                .members(memberItems)
                .items(getJsonArrayOfItems(ITEMS_MEMBER))
                .collectionId(id).build();

        ModelAndView model = new ModelAndView("members");
        model.addObject("request", memberRequest);
        return model;
    }

    /**
     * gets a JSON object from a file.
     *
     * @param resource resource file.
     * @return JSON object.
     */
    private JSONObject getJsonObject(Resource resource) {
        JSONParser parser = new JSONParser();
        JSONObject obj = null;
        try {
            obj = (JSONObject) parser.parse(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    /**
     * gets an array of TabulatorItems from a file.
     *
     * @param resource resource file.
     * @return array of TabulatorItems.
     */
    private TabulatorItems[] getJsonArrayOfItems(Resource resource) {
        ObjectMapper mapper = new ObjectMapper();
        TabulatorItems[] items = null;
        try {
            items = mapper.readValue(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8), TabulatorItems[].class);
        } catch (IOException ex) {
            Logger.getLogger(CollectionApiUIImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return items;
    }

}
