/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.datamanager.collection.dto;

import edu.kit.datamanager.collection.domain.CollectionObject;
import lombok.Builder;
import lombok.Getter;
import org.json.simple.JSONObject;

/**
 *
 * @author chelbi
 */
@Builder
@Getter
public class EditorRequest {
    private RenderType renderType;
    private Operation operation;
    private JSONObject dataModel;
    private JSONObject uiForm;
    private CollectionObject resource;
    private String etag;
}
