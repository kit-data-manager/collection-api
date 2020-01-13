package edu.kit.datamanager.collection.configuration;

import javax.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.util.UriComponentsBuilder;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-07-09T15:21:24.632+02:00")

@Configuration
public class SwaggerDocumentationConfig{

  ApiInfo apiInfo(){
    return new ApiInfoBuilder()
            .title("RDA Collections API")
            .description("The RDA Collections API Recommendation is a unified model and interface specification for CRUD operations on data collections, with particular observance of persistent identification and typing aspects. The recommendation allows building collections within diverse domains and then sharing or expanding them across disciplines. This recommendation has first been released in September 2017 for the 10th RDA Plenary in Montreal, Canada.")
            .license("APACHE LICENSE, VERSION 2.0")
            .licenseUrl("https://www.apache.org/licenses/LICENSE-2.0")
            .termsOfServiceUrl("")
            .version("1.0.0")
            .contact(new Contact("Thomas Jejkal", "", "thomas.jejkal@kit.edu"))
            .build();
  }

  @Bean
  public Docket customImplementation(){
    return new Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.basePackage("edu.kit.datamanager.collection.web"))
            .build()
            .ignoredParameterTypes(Pageable.class, WebRequest.class, HttpServletResponse.class, UriComponentsBuilder.class)
            // .directModelSubstitute(org.threeten.bp.LocalDate.class, java.sql.Date.class)
            // .directModelSubstitute(org.threeten.bp.OffsetDateTime.class, java.util.Date.class)
            .apiInfo(apiInfo());
  }

}
