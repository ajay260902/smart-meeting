package com.project.meeting.service;

import com.project.meeting.model.UserToken;
import com.project.meeting.utils.AppConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class DriveService {

    @Autowired
    private TokenRefreshService tokenRefreshService;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, String> uploadFile(String email, MultipartFile file) throws IOException {
        String token = tokenRefreshService.getValidAccessToken(email);

        String url = "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart&fields=id,webViewLink";

        // Metadata
        String metadataJson = """
        {
            "name": "%s",
            "parents": ["%s"]
        }
    """.formatted(file.getOriginalFilename(), AppConstant.FOLDER_ID);

        HttpHeaders metadataHeader = new HttpHeaders();
        metadataHeader.setContentType(MediaType.APPLICATION_JSON);

        HttpHeaders fileHeader = new HttpHeaders();
        fileHeader.setContentType(MediaType.parseMediaType(file.getContentType()));

        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("metadata", new HttpEntity<>(metadataJson, metadataHeader));
        body.add("file", new HttpEntity<>(file.getBytes(), fileHeader));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<?> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

        Map<?, ?> responseBody = response.getBody();
        if (responseBody != null && responseBody.get("id") != null && responseBody.get("webViewLink") != null) {
            return Map.of(
                    "fileId", responseBody.get("id").toString(),
                    "fileUrl", responseBody.get("webViewLink").toString()
            );
        } else {
            throw new RuntimeException("Drive upload failed or incomplete response: " + responseBody);
        }
    }


    public Map getFileMetadata(String email, String fileId) {
        String token = tokenRefreshService.getValidAccessToken(email);

        String url = "https://www.googleapis.com/drive/v3/files/" + fileId + "?fields=id,name,mimeType,webViewLink";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
        return response.getBody();
    }

    public void deleteFile(String email, String fileId) {
        String token = tokenRefreshService.getValidAccessToken(email);

        String url = "https://www.googleapis.com/drive/v3/files/" + fileId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
    }
}
