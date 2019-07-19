package edu.kit.datamanager.collection.web.impl;

import edu.kit.datamanager.collection.domain.ServiceFeatures;
import edu.kit.datamanager.collection.web.FeaturesApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-07-09T15:21:24.632+02:00")
@Controller
public class FeaturesApiController implements FeaturesApi{

  private static final Logger LOG = LoggerFactory.getLogger(FeaturesApiController.class);

  @org.springframework.beans.factory.annotation.Autowired
  public FeaturesApiController(){
  }

  public ResponseEntity<ServiceFeatures> featuresGet(){
    LOG.trace("Calling featureGet().");
    return new ResponseEntity<ServiceFeatures>(ServiceFeatures.getDefault(), HttpStatus.OK);
  }

}
