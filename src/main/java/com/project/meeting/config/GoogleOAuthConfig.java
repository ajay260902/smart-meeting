package com.project.meeting.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
public class GoogleOAuthConfig {

    @Value("${google.client.id}")
    public String clientId;

    @Value("${google.client.secret}")
    public String clientSecret;

    @Value("${google.redirect.uri}")
    public String redirectUri;

    public String getAuthUrl() {
        String scope = String.join(" ",
                "https://www.googleapis.com/auth/userinfo.profile",
                "https://www.googleapis.com/auth/userinfo.email",
                "https://www.googleapis.com/auth/drive",
                "https://www.googleapis.com/auth/calendar"
        );


        return "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code"
                + "&scope=" + scope
                + "&access_type=offline"
                + "&prompt=consent";
    }

}
