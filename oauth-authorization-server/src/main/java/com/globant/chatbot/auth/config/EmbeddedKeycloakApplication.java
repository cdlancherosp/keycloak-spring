package com.globant.chatbot.auth.config;

import com.globant.chatbot.auth.support.SpringBootConfigProvider;
import org.apache.logging.log4j.util.Strings;
import org.keycloak.Config;
import org.keycloak.exportimport.ExportImportManager;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakTransactionManager;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.managers.ApplianceBootstrap;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.resources.KeycloakApplication;
import org.keycloak.util.JsonSerialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.IOException;
import java.util.UUID;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

public class EmbeddedKeycloakApplication extends KeycloakApplication {

    private static final Logger LOG = LoggerFactory.getLogger(EmbeddedKeycloakApplication.class);
    private final KeycloakCustomProperties keycloakCustomProperties;

    protected void loadConfig() {
        Config.init(SpringBootConfigProvider.getInstance());
    }

    public EmbeddedKeycloakApplication(@Context ServletContext context) {
        this.keycloakCustomProperties = WebApplicationContextUtils.getRequiredWebApplicationContext(context).getBean(KeycloakCustomProperties.class);
    }

    @Override
    protected ExportImportManager migrateAndBootstrap() {

        ExportImportManager exportImportManager = super.migrateAndBootstrap();

        tryCreateMasterRealmAdminUser();
        tryImportRealm();

        return exportImportManager;
    }

    protected void tryCreateMasterRealmAdminUser() {

        if (!keycloakCustomProperties.getAdminUser().isCreateAdminUserEnabled()) {
            LOG.warn("Skipping creation of keycloak master adminUser.");
            return;
        }

        KeycloakCustomProperties.AdminUser adminUser = keycloakCustomProperties.getAdminUser();

        String username = adminUser.getUsername();
        if (StringUtils.isEmpty(username)) {
            return;
        }

        KeycloakSession session = getSessionFactory().create();
        KeycloakTransactionManager transaction = session.getTransactionManager();
        try {
            transaction.begin();

            boolean randomPassword = false;
            String password = adminUser.getPassword();
            if (StringUtils.isEmpty(adminUser.getPassword())) {
                password = UUID.randomUUID().toString();
                randomPassword = true;
            }
            new ApplianceBootstrap(session).createMasterRealmUser(username, password);
            if (randomPassword) {
                LOG.info("Generated admin password: {}", password);
            }
            ServicesLogger.LOGGER.addUserSuccess(username, Config.getAdminRealm());

            transaction.commit();
        } catch (IllegalStateException e) {
            transaction.rollback();
            ServicesLogger.LOGGER.addUserFailedUserExists(username, Config.getAdminRealm());
        } catch (Throwable t) {
            transaction.rollback();
            ServicesLogger.LOGGER.addUserFailed(t, username, Config.getAdminRealm());
        } finally {
            session.close();
        }
    }

    protected void tryImportRealm() {

        String realmPath = keycloakCustomProperties.getImportRealmPath();

        if (Strings.isEmpty(realmPath)) {
            LOG.info("Could not find keycloak import file");
            return;
        }

        LOG.info("Starting Keycloak realm configuration import from location: {}", realmPath);

        KeycloakSession session = getSessionFactory().create();

        try {
            session.getTransactionManager().begin();
            RealmManager manager = new RealmManager(session);
            Resource importLocation = new ClassPathResource(realmPath);

            manager.importRealm(JsonSerialization.readValue(importLocation.getInputStream(), RealmRepresentation.class));
            session.getTransactionManager().commit();
        } catch (IOException ex) {
            LOG.warn("Failed to import Realm json file: {}", ex.getMessage());
            session.getTransactionManager().rollback();
            return;
        }

        session.close();

        LOG.info("Keycloak realm configuration import finished.");
    }
}