package com.ry.cbms.decision.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {
    public final Auth auth = new Auth();

    public Auth getAuth() {
        return auth;
    }

    public static class Auth {
        private String withoutUrls;

        public String getWithoutUrls() {
            return withoutUrls;
        }

        public void setWithoutUrls(String withoutUrls) {
            this.withoutUrls = withoutUrls;
        }
    }
}
