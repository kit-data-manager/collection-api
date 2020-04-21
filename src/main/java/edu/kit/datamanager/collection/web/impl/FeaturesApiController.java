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
