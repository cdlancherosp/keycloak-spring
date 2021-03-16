package com.globant.chatbot.auth.annotation;

import com.globant.chatbot.auth.config.EmbeddedKeycloakConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enable the Keycloak server.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(EmbeddedKeycloakConfig.class)
public @interface EnableKeycloakServer {
}
