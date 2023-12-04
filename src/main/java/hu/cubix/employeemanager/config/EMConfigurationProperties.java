package hu.cubix.employeemanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "em")
@Component
public class EMConfigurationProperties {
    private String secret;
    private int expiryInterval;
    private String issuer;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public int getExpiryInterval() {
        return expiryInterval;
    }

    public void setExpiryInterval(int expiryInterval) {
        this.expiryInterval = expiryInterval;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}