package com.project.meeting.service;

import com.project.meeting.model.GoogleTokenResponse;
import com.project.meeting.config.GoogleOAuthConfig;
import com.project.meeting.model.UserToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
public class TokenRefreshService {

    @Autowired
    private GoogleOAuthConfig googleOAuthConfig;

    @Autowired
    private UserTokenService userTokenService;

    private final RestTemplate restTemplate = new RestTemplate();

    public String refreshAccessTokenAndUpdate(String email) {
        // Step 1: Get user token from DB
        UserToken userToken = userTokenService.getLatestTokenByEmail(email);

        String refreshToken = userToken.getRefreshToken();

        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new RuntimeException("No refresh token available for this user.");
        }

        // Step 2: Prepare request
        String url = "https://oauth2.googleapis.com/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", googleOAuthConfig.clientId);
        form.add("client_secret", googleOAuthConfig.clientSecret);
        form.add("refresh_token", refreshToken);
        form.add("grant_type", "refresh_token");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

        // Step 3: Call Google to refresh token
        ResponseEntity<GoogleTokenResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                GoogleTokenResponse.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            GoogleTokenResponse tokenResponse = response.getBody();

            userTokenService.updateAccessTokenByEmail(email, tokenResponse.getAccess_token(), Integer.parseInt(tokenResponse.getExpires_in())); // this persists to MySQL

            return tokenResponse.getAccess_token();
        }

        throw new RuntimeException("Failed to refresh access token from Google.");
    }

    public String getValidAccessToken(String email) {
        UserToken token = userTokenService.getLatestTokenByEmail(email);

        if (userTokenService.isAccessTokenExpired(token)) {
            System.out.println("Access token expired. Refreshing...");
            return refreshAccessTokenAndUpdate(email);
        }

        return token.getAccessToken();
    }

}
