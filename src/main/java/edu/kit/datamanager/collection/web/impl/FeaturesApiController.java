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

import edu.kit.datamanager.collection.domain.ServiceFeatures;
import edu.kit.datamanager.collection.web.FeaturesApi;
import io.swagger.v3.oas.annotations.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-07-09T15:21:24.632+02:00")
@RestController
@Schema(title = "features", description = "the features API")
@RequestMapping(value = "/api/v1")
public class FeaturesApiController implements FeaturesApi {

    private static final Logger LOG = LoggerFactory.getLogger(FeaturesApiController.class);

    @org.springframework.beans.factory.annotation.Autowired
    public FeaturesApiController() {
    }

    @Override
    public ResponseEntity<ServiceFeatures> featuresGet() {
        LOG.trace("Calling featureGet().");
        return new ResponseEntity<>(ServiceFeatures.getDefault(), HttpStatus.OK);
    }

}
