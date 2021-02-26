package com.globant.chatbot.auth;

import com.globant.chatbot.auth.config.KeycloakProperties;
import com.globant.chatbot.auth.config.KeycloakCustomProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(exclude = LiquibaseAutoConfiguration.class)
@EnableConfigurationProperties({ KeycloakProperties.class, KeycloakCustomProperties.class })
public class AuthorizationServerApp {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(AuthorizationServerApp.class, args);
    }
}
