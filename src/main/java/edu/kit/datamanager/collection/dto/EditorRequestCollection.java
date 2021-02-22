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
package edu.kit.datamanager.collection.dto;

import edu.kit.datamanager.collection.domain.CollectionObject;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.json.simple.JSONObject;

/**
 * It contains information of a collection, which should be sent to the metadata editor.
 * @author chelbi
 */
@Builder
@Getter
public class EditorRequestCollection {
    
    /**
     * JSON schema, which describes the structure of the data model. 
     */
    private JSONObject dataModel;
    
    /**
     * JSON user interface form, which describes the structure of the form layout.
     */
    private JSONObject uiForm;
    
    /**
     * array of collections.
     */
    private List <CollectionObject> collections;
    
    /**
     * array, which includes the table’s column definitions. 
     */
    private TabulatorItems[] items;
}
