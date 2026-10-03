package com.example.system.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Swagger API 文档配置
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Value("${spring.servlet.context-path:/api}")
    private String contextPath;

    /**
     * 创建 Swagger Docket（扫描 com.example.system.controller 包）
     */
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                // 扫描 controller 包（根据你的实际包结构调整）
                .apis(RequestHandlerSelectors.basePackage("com.example.system.controller"))
                .paths(PathSelectors.any())
                .build()
                .pathMapping(contextPath);  // 设置上下文路径
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("智慧校园体育场馆管理系统 API 文档")
                .description("提供体育场馆预约、器材管理、活动管理等接口")
                .contact(new Contact("开发团队", "http://campus.sports.com", "sports@campus.com"))
                .version("1.0.0")
                .license("版权所有 © 智慧校园")
                .build();
    }
}