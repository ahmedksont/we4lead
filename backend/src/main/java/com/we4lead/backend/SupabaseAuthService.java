package com.we4lead.backend;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

// --- Step 1: Configuration class for Supabase properties ---
@Configuration
@ConfigurationProperties(prefix = "supabase")
class SupabaseConfig {
    private String url;
    private String serviceKey;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getServiceKey() {
        return serviceKey;
    }

    public void setServiceKey(String serviceKey) {
        this.serviceKey = serviceKey;
    }
}

// --- Step 2: Service class ---
@Service
public class SupabaseAuthService {

    private final SupabaseConfig config;
    private final RestTemplate restTemplate;

    // Inject configuration and RestTemplate via constructor
    public SupabaseAuthService(SupabaseConfig config, RestTemplate restTemplate) {
        this.config = config;
        this.restTemplate = restTemplate;
    }

    public void inviteUser(String email) {
        if (config.getUrl() == null || config.getServiceKey() == null) {
            throw new IllegalStateException("Supabase URL or service key is not configured!");
        }

        String url = config.getUrl() ;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", config.getServiceKey());
        headers.set("Authorization", "Bearer " + config.getServiceKey());

        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("redirect_to", config.getUrl() + "/auth/callback");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(url, entity, String.class);
            System.out.println("Invite sent successfully to " + email);
        } catch (RestClientException e) {
            System.err.println("Failed to send invite: " + e.getMessage());
            throw e;
        }
    }
}
