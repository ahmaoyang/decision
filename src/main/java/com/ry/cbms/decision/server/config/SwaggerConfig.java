package com.ry.cbms.decision.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.async.DeferredResult;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * swagger文档
 *
 * @author maoyang
 * 2019年5月21日
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket ProductApi() {
        return new Docket (DocumentationType.SWAGGER_2)
                .genericModelSubstitutes (DeferredResult.class)
                .useDefaultResponseMessages (false)
                .forCodeGeneration (false)
                .pathMapping ("/")
                .select ()
                .build ()
                .apiInfo (productApiInfo ());
    }

    private ApiInfo productApiInfo() {
        ApiInfo apiInfo = new ApiInfo ("Decision系统数据接口文档",
                "Decision接口描述",
                "1.0.0",
                "",
                "融易科技",
                "",
                "");
        return apiInfo;
    }
}
