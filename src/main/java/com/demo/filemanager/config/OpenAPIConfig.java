package com.demo.filemanager.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Value("${openapi.dev-url}")
    private String devUrl;

    @Bean
    public OpenAPI myOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl(devUrl);
        devServer.setDescription("Server URL in Development environment");

        Contact contact = new Contact();
        contact.setEmail("r.erdog46@gmail.com");
        contact.setName("Recep Erdogan");
        contact.setUrl("https://www.example.com"); // Burayı sizin kişisel veya iş web sitenizle değiştirebilirsiniz.

        License mitLicense = new License().name("MIT License").url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("Filemanager API")
                .version("1.0")
                .contact(contact)
                .description("This API is designed to manage files, allowing users to upload, download, and organize their documents.")
                .termsOfService("https://www.example.com/terms") // Eğer özel bir hizmet şartları sayfanız varsa, bu URL'yi onunla değiştirebilirsiniz.
                .license(mitLicense);

        OpenAPI openAPI = new OpenAPI();

        openAPI.addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components().addSecuritySchemes("Bearer Authentication", createAPIKeyScheme())).info(info).servers(List.of(devServer));

        return openAPI;
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme().type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }
}
