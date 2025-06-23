package com.project.meeting.service;

import com.project.meeting.model.EventRequest;
import com.project.meeting.model.UserToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;

@Service
public class CalendarService {

    @Autowired
    private TokenRefreshService tokenRefreshService;

    @Autowired
    private  DriveService driveService;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getUpcomingEvents(String email) {
        String token = tokenRefreshService.getValidAccessToken(email);

        String url = "https://www.googleapis.com/calendar/v3/calendars/primary/events" +
                "?maxResults=10" +
                "&orderBy=startTime" +
                "&singleEvents=true" +
                "&fields=items(id,summary,description,start,end,attachments,colorId)";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(MediaType.parseMediaTypes("application/json"));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );

        return response.getBody();
    }

    public String createEvent(String email, EventRequest request) throws IOException {
        String token = tokenRefreshService.getValidAccessToken(email);
        String url = "https://www.googleapis.com/calendar/v3/calendars/primary/events?supportsAttachments=true";

        // Upload file to Drive (if provided)
        Map<String,String> driveUpload=new HashMap<>();
        if (request.getFile() != null && !request.getFile().isEmpty()) {
            driveUpload= driveService.uploadFile(email,request.getFile());
        }

        // Build event body
        Map<String, Object> event = new HashMap<>();
        event.put("summary", request.getSummary());

        String description = request.getDescription();
        event.put("description", description);

        Map<String, String> start = new HashMap<>();
        start.put("dateTime", request.getStartDateTime());
        event.put("start", start);

        Map<String, String> end = new HashMap<>();
        end.put("dateTime", request.getEndDateTime());
        event.put("end", end);

        event.put("colorId", "5");


        if(driveUpload.containsKey("fileId") && driveUpload.containsKey("fileUrl")) {
            Map<String, Object> attachment = new HashMap<>();
            attachment.put("fileId", driveUpload.get("fileId"));
            attachment.put("fileUrl", driveUpload.get("fileUrl"));
            attachment.put("title", request.getFile().getOriginalFilename());
            attachment.put("mimeType", request.getFile().getContentType());

            event.put("attachments", List.of(attachment));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(event, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                httpEntity,
                String.class
        );

        return response.getBody(); // raw JSON from Google API
    }

    public String updateEvent(String email, EventRequest updatedEvent) {
        try {
            String accessToken = tokenRefreshService.getValidAccessToken(email);
            String url = "https://www.googleapis.com/calendar/v3/calendars/primary/events/" + updatedEvent.getEventId();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("summary", updatedEvent.getSummary());
            body.put("description", updatedEvent.getDescription());

            Map<String, String> start = Map.of(
                    "dateTime", updatedEvent.getStartDateTime(),
                    "timeZone", "Asia/Kolkata"
            );
            Map<String, String> end = Map.of(
                    "dateTime", updatedEvent.getEndDateTime(),
                    "timeZone", "Asia/Kolkata"
            );

            body.put("start", start);
            body.put("end", end);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            restTemplate.exchange(url, HttpMethod.PUT, request, String.class);

            return "Event updated successfully: " + updatedEvent.getEventId();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update event: " + e.getMessage());
        }
    }

    public String deleteEvent(String email, String eventId) {
        try {
            String accessToken = tokenRefreshService.getValidAccessToken(email);
            String url = "https://www.googleapis.com/calendar/v3/calendars/primary/events/" + eventId;

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);

            return "Event deleted successfully: " + eventId;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to delete event: " + e.getMessage());
        }
    }

    public String getCalendarColors(String email) {
        String accessToken = tokenRefreshService.getValidAccessToken(email);

        String url = "https://www.googleapis.com/calendar/v3/colors";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );

        return response.getBody(); // You can parse this JSON if needed
    }



}
