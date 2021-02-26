package com.globant.chatbot.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "keycloak.custom")
public class KeycloakCustomProperties {

    private String contextPath = "/auth";
    private AdminUser adminUser = new AdminUser();
    private Infinispan infinispan = new Infinispan();
    private Migration migration = new Migration();

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public AdminUser getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(AdminUser adminUser) {
        this.adminUser = adminUser;
    }

    public Infinispan getInfinispan() {
        return infinispan;
    }

    public void setInfinispan(Infinispan infinispan) {
        this.infinispan = infinispan;
    }

    public Migration getMigration() {
        return migration;
    }

    public void setMigration(Migration migration) {
        this.migration = migration;
    }

    public static class AdminUser {
        //Default user
        private String username = "admin";
        private String password = "admin";
        boolean createAdminUserEnabled = true;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public boolean isCreateAdminUserEnabled() {
            return createAdminUserEnabled;
        }

        public void setCreateAdminUserEnabled(boolean createAdminUserEnabled) {
            this.createAdminUserEnabled = createAdminUserEnabled;
        }
    }

    public static class Migration {

        private Resource importLocation = new FileSystemResource("keycloak-realm-config.json");
        private String importProvider = "singleFile";

        public Resource getImportLocation() {
            return importLocation;
        }

        public void setImportLocation(Resource importLocation) {
            this.importLocation = importLocation;
        }

        public String getImportProvider() {
            return importProvider;
        }

        public void setImportProvider(String importProvider) {
            this.importProvider = importProvider;
        }
    }

    public static class Infinispan {

        private Resource configLocation = new ClassPathResource("infinispan.xml");
        public Resource getConfigLocation() {
            return configLocation;
        }

        public void setConfigLocation(Resource configLocation) {
            this.configLocation = configLocation;
        }
    }
}
