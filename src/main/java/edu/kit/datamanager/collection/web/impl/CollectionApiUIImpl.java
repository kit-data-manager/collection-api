/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.datamanager.collection.web.impl;

import edu.kit.datamanager.collection.domain.CollectionObject;
import edu.kit.datamanager.collection.dto.EditorRequest;
import edu.kit.datamanager.collection.dto.Operation;
import edu.kit.datamanager.collection.dto.RenderType;
import edu.kit.datamanager.collection.web.CollectionApiUI;
import edu.kit.datamanager.collection.web.CollectionsApi;
import java.io.InputStreamReader;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author chelbi
 */
@Controller
public class CollectionApiUIImpl implements CollectionApiUI {

    private final static String DATAMODEL = "/static/jsonSchemas/dataModel.json";
    private final static String UIFORM = "/static/jsonSchemas/uiForm.json";

    @Autowired
    private CollectionsApi collectionsApi;

    @RequestMapping("/collection/create")
    public ModelAndView createNewCollection() {
        EditorRequest request = EditorRequest.builder()
                .renderType(RenderType.FORM)
                .operation(Operation.CREATE)
                .dataModel(getJsonObject(DATAMODEL))
                .uiForm(getJsonObject(UIFORM)).build();
        ModelAndView model = new ModelAndView("collection");
        model.addObject("request", request);
        return model;
    }

    @RequestMapping("/collection/update/{id}")
    public ModelAndView updateCollection(@PathVariable String id) {
        ResponseEntity<CollectionObject> collection = collectionsApi.collectionsIdGet(id);
        EditorRequest request = EditorRequest.builder()
                .renderType(RenderType.FORM)
                .operation(Operation.UPDATE)
                .dataModel(getJsonObject(DATAMODEL))
                .uiForm(getJsonObject(UIFORM))
                .resource(collection.getBody())
                .etag(collection.getHeaders().getETag())
                .build();

        ModelAndView model = new ModelAndView("collection");
        model.addObject("request", request);
        return model;
    }
    
    @RequestMapping("/collection/delete/{id}")
    public ModelAndView deleteCollection(@PathVariable String id) {
        ResponseEntity<CollectionObject> collection = collectionsApi.collectionsIdGet(id);
        EditorRequest request = EditorRequest.builder()
                .renderType(RenderType.FORM)
                .operation(Operation.DELETE)
                .dataModel(getJsonObject(DATAMODEL))
                .uiForm(getJsonObject(UIFORM))
                .resource(collection.getBody())
                .etag(collection.getHeaders().getETag())
                .build();

        ModelAndView model = new ModelAndView("collection");
        model.addObject("request", request);
        return model;
    }

    private JSONObject getJsonObject(String path) {
        Resource resource = new ClassPathResource(path);
        JSONParser parser = new JSONParser();
        JSONObject obj = null;
        try {
            obj = (JSONObject) parser.parse(
                    new InputStreamReader(resource.getInputStream(), "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }
}
