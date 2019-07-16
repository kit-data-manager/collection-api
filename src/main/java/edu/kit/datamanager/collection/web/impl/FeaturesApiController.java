package edu.kit.datamanager.collection.web.impl;

import edu.kit.datamanager.collection.domain.ServiceFeatures;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.datamanager.collection.web.FeaturesApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-07-09T15:21:24.632+02:00")
@Controller
public class FeaturesApiController implements FeaturesApi{

  private static final Logger log = LoggerFactory.getLogger(FeaturesApiController.class);

  private final ObjectMapper objectMapper;

  private final HttpServletRequest request;

  @org.springframework.beans.factory.annotation.Autowired
  public FeaturesApiController(ObjectMapper objectMapper, HttpServletRequest request){
    this.objectMapper = objectMapper;
    this.request = request;
  }

  public ResponseEntity<ServiceFeatures> featuresGet(){
    return new ResponseEntity<ServiceFeatures>(ServiceFeatures.getDefault(), HttpStatus.OK);
  }

}
