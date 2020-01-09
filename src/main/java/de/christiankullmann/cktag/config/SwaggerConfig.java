package de.christiankullmann.cktag.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;

/**
 * Configuration to enable the Swagger UI for this Service
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

  private static final String CONTROLLER_BASE_PACKAGE = "de.christiankullmann.cktag.controller";

  /**
   * Create the swagger Documentationn for all controllers in {@link #CONTROLLER_BASE_PACKAGE}
   *
   * @return a new Bean {@link Docket}
   */
  @Bean
  public Docket api() {
    return new Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(RequestHandlerSelectors.basePackage(CONTROLLER_BASE_PACKAGE))
        .paths(PathSelectors.any())
        .build()
        .apiInfo(getApiInfo());
  }

  /**
   * Generate the ApiInfo for this Service
   *
   * @return the {@link ApiInfo}
   */
  private ApiInfo getApiInfo() {
    return new ApiInfo("CKTag Api",
        "Allows adding tags to dropbox entries and saving these to an Apache Solr",
        "v1",
        "Terms of Service",
        new Contact("Christian Kullmann", "", "dev@christiankullmann.de"),
        "Apache 2.0", "http://www.apache.org/licenses/LICENSE-2.0", new ArrayList<>());
  }
}