package com.globant.chatbot.auth.config;

import com.globant.chatbot.auth.support.DynamicJndiContextFactoryBuilder;
import com.globant.chatbot.auth.support.KeycloakUndertowRequestFilter;
import com.globant.chatbot.auth.support.SpringBootConfigProvider;
import com.globant.chatbot.auth.support.SpringBootPlatform;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.configuration.parsing.ParserRegistry;
import org.infinispan.manager.DefaultCacheManager;
import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher;
import org.jboss.resteasy.plugins.server.servlet.ResteasyContextParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.servlet.Filter;
import javax.sql.DataSource;

@Configuration
public class EmbeddedKeycloakConfig {

    private static final Logger LOG = LoggerFactory.getLogger(EmbeddedKeycloakConfig.class);

    @Bean
    ApplicationListener<ApplicationReadyEvent> onApplicationReadyEventListener(ServerProperties serverProperties,
                                                                               KeycloakCustomProperties keycloakCustomProperties) {
        return (evt) -> {
            Integer port = serverProperties.getPort();
            String keycloakContextPath = keycloakCustomProperties.getContextPath();
            LOG.info("Embedded Keycloak started: http://localhost:{}{} to use keycloak", port, keycloakContextPath);
        };
    }

    @Bean
    @ConditionalOnMissingBean(name = "springBootConfigProvider")
    protected SpringBootConfigProvider springBootConfigProvider(KeycloakProperties keycloakProperties) {
        return new SpringBootConfigProvider(keycloakProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "springBootPlatform")
    protected SpringBootPlatform springBootPlatform() {
        return new SpringBootPlatform();
    }

    @Bean
    @ConditionalOnMissingBean(name = "springBeansJndiContextFactory")
    protected DynamicJndiContextFactoryBuilder springBeansJndiContextFactory(DataSource dataSource, DefaultCacheManager cacheManager) {
        return new DynamicJndiContextFactoryBuilder(dataSource, cacheManager);
    }

    @Bean
    @ConditionalOnMissingBean(name = "keycloakInfinispanCacheManager")
    protected DefaultCacheManager keycloakInfinispanCacheManager(KeycloakCustomProperties customProperties) throws Exception {

        KeycloakCustomProperties.Infinispan infinispan = customProperties.getInfinispan();
        Resource configLocation = infinispan.getConfigLocation();
        LOG.info("Using infinispan configuration from {}", configLocation.getURI());

        ConfigurationBuilderHolder configBuilder = new ParserRegistry().parse(configLocation.getURL());
        DefaultCacheManager defaultCacheManager = new DefaultCacheManager(configBuilder, false);
        defaultCacheManager.start();
        return defaultCacheManager;
    }

    @Bean
    ServletRegistrationBean<HttpServlet30Dispatcher> keycloakJaxRsApplication(
            KeycloakCustomProperties keycloakCustomProperties) throws Exception {

        initKeycloakEnvironmentFromProfiles();

        ServletRegistrationBean<HttpServlet30Dispatcher> servlet = new ServletRegistrationBean<>(new HttpServlet30Dispatcher());
        servlet.addInitParameter("javax.ws.rs.Application", EmbeddedKeycloakApplication.class.getName());

        servlet.addInitParameter("resteasy.allowGzip", "true");
        servlet.addInitParameter("keycloak.embedded", "true");
        servlet.addInitParameter(ResteasyContextParameters.RESTEASY_EXPAND_ENTITY_REFERENCES, "false");
        servlet.addInitParameter(ResteasyContextParameters.RESTEASY_SECURE_PROCESSING_FEATURE, "true");
        servlet.addInitParameter(ResteasyContextParameters.RESTEASY_DISABLE_DTDS, "true");
        servlet.addInitParameter(ResteasyContextParameters.RESTEASY_SERVLET_MAPPING_PREFIX, keycloakCustomProperties.getContextPath());
        servlet.addInitParameter(ResteasyContextParameters.RESTEASY_USE_CONTAINER_FORM_PARAMS, "false");
        servlet.addInitParameter(ResteasyContextParameters.RESTEASY_DISABLE_HTML_SANITIZER, "true");
        servlet.addUrlMappings(keycloakCustomProperties.getContextPath() + "/*");

        servlet.setLoadOnStartup(2);
        servlet.setAsyncSupported(true);

        return servlet;
    }

    @Bean
    @ConditionalOnMissingBean(name = "keycloakSessionManagement")
    protected FilterRegistrationBean<Filter> keycloakSessionManagement(KeycloakCustomProperties keycloakCustomProperties) {

        FilterRegistrationBean<Filter> filter = new FilterRegistrationBean<>();
        filter.setName("Keycloak Session Management");
        filter.setFilter(new KeycloakUndertowRequestFilter());
        filter.addUrlPatterns(keycloakCustomProperties.getContextPath() + "/*");

        return filter;
    }

    private void initKeycloakEnvironmentFromProfiles() {

        try (InputStream in = getClass().getClassLoader().getResourceAsStream("profile.properties")) {

            if (in == null) {
                LOG.info("Could not find profile.properties on classpath.");
                return;
            }

            Properties profile = new Properties();
            profile.load(in);

            LOG.info("Found profile.properties on classpath.");
            String profilePrefix = "keycloak.profile.";
            for (Object key : profile.keySet()) {
                String value = (String) profile.get(key);
                String featureName = key.toString().toLowerCase();
                String currentValue = System.getProperty(profilePrefix + featureName);
                if (currentValue == null) {
                    System.setProperty(profilePrefix + featureName, value);
                }
            }
        } catch (IOException ioe) {
            LOG.warn("Could not read profile.properties.", ioe);
        }
    }
}
