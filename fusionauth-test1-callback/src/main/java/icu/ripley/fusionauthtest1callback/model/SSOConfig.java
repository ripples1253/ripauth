package icu.ripley.fusionauthtest1callback.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "sso")
@Configuration("ssoConfig")
@Data
public class SSOConfig {

    private String callbackUrl;
    private String clientId;
    private String clientSecret;
    private String apiKey;
    private String oauthLoginUrl;
    private String baseUrl;

}
