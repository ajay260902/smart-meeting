package com.project.meeting.model;

import org.springframework.web.multipart.MultipartFile;

public class EventRequest {
    private  String eventId;
    private String summary;
    private String description;
    private String startDateTime; // format: 2025-06-20T10:00:00+05:30
    private String endDateTime;   // format: 2025-06-20T11:00:00+05:30
    private MultipartFile file;

    @Override
    public String toString() {
        return "EventRequest{" +
                "eventId='" + eventId + '\'' +
                ", summary='" + summary + '\'' +
                ", description='" + description + '\'' +
                ", startDateTime='" + startDateTime + '\'' +
                ", endDateTime='" + endDateTime + '\'' +
                ", file=" + file +
                '}';
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public String getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(String endDateTime) {
        this.endDateTime = endDateTime;
    }
}