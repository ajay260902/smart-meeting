package com.project.meeting.controller;

import com.project.meeting.config.GoogleOAuthConfig;
import com.project.meeting.model.GoogleTokenResponse;
import com.project.meeting.model.GoogleUserInfo;
import com.project.meeting.model.UserToken;
import com.project.meeting.service.TokenRefreshService;
import com.project.meeting.service.UserTokenService;
import com.project.meeting.utils.AppConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin("*")
@RequestMapping("/auth")
public class OAuthController {

    @Autowired
    private GoogleOAuthConfig googleOAuthConfig;

    @Autowired
    private TokenRefreshService tokenRefreshService;

    @Autowired
    private UserTokenService userTokenService;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/google")
    public ResponseEntity<?> redirectToGoogle() {
        String authUrl = googleOAuthConfig.getAuthUrl();
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, authUrl)
                .build();
    }

    @GetMapping("/callback")
    public ResponseEntity<?> handleGoogleCallback(@RequestParam("code") String code) {
        try {
            String tokenEndpoint = "https://oauth2.googleapis.com/token";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
            formParams.add("code", code);
            formParams.add("client_id", googleOAuthConfig.clientId);
            formParams.add("client_secret", googleOAuthConfig.clientSecret);
            formParams.add("redirect_uri", googleOAuthConfig.redirectUri);
            formParams.add("grant_type", "authorization_code");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formParams, headers);

            ResponseEntity<GoogleTokenResponse> response = restTemplate.exchange(
                    tokenEndpoint,
                    HttpMethod.POST,
                    request,
                    GoogleTokenResponse.class
            );

            GoogleTokenResponse tokenResponse = response.getBody();

            if (tokenResponse == null || tokenResponse.getAccess_token() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access token not received from Google");
            }

            // ✅ Correct header
            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.set("Authorization", "Bearer " + tokenResponse.getAccess_token());

            HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);

            ResponseEntity<GoogleUserInfo> userInfoResponse = restTemplate.exchange(
                    "https://www.googleapis.com/oauth2/v2/userinfo",
                    HttpMethod.GET,
                    userRequest,
                    GoogleUserInfo.class
            );



            String userEmail = userInfoResponse.getBody().getEmail();

            // save in database
            UserToken token = new UserToken();
            token.setEmail(userEmail); // You'll need to fetch user profile to get email
            token.setAccessToken(tokenResponse.getAccess_token());
            token.setRefreshToken(tokenResponse.getRefresh_token());
            token.setExpiresIn(Integer.parseInt(tokenResponse.getExpires_in()));
            token.setIssuedAt(LocalDateTime.now());
            token.setTokenType(tokenResponse.getToken_type());
            token.setScope(tokenResponse.getScope());

            userTokenService.saveToken(token);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("access_token", tokenResponse.getAccess_token());
            responseBody.put("refresh_token", tokenResponse.getRefresh_token());
            responseBody.put("expires_in", tokenResponse.getExpires_in());
            responseBody.put("token_type", tokenResponse.getToken_type());
            responseBody.put("scope", tokenResponse.getScope());

           // 👇 User info
            responseBody.put("email", userInfoResponse.getBody().getEmail());
            responseBody.put("name", userInfoResponse.getBody().getName());
            responseBody.put("picture", userInfoResponse.getBody().getPicture());
            responseBody.put("google_id", userInfoResponse.getBody().getId());

            System.err.println(responseBody);


            String frontendRedirectUrl = AppConstant.FRONTEND_URL +
                    "?access_token=" + tokenResponse.getAccess_token() +
                    "&refresh_token=" + tokenResponse.getRefresh_token() +
                    "&email=" + userInfoResponse.getBody().getEmail();

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, frontendRedirectUrl)
                    .build();


        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestParam("email") String email) {
        try {
            String newAccessToken = tokenRefreshService.refreshAccessTokenAndUpdate(email);

            return ResponseEntity.ok("Token Refreshed successfully new token is :: "+newAccessToken);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(" Failed to refresh access token: " + e.getMessage());
        }
    }


    @GetMapping("/userinfo")
    public ResponseEntity<?> getGoogleUserInfo(@RequestParam String email) {
        try {
            UserToken userToken = userTokenService.getLatestTokenByEmail(email);

            String accessToken = userToken.getAccessToken();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);

            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<GoogleUserInfo> response = restTemplate.exchange(
                    "https://www.googleapis.com/oauth2/v2/userinfo",
                    HttpMethod.GET,
                    request,
                    GoogleUserInfo.class
            );

            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch user info", "details", e.getMessage()));
        }
    }


}
