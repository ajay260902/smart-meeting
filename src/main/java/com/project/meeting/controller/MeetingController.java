package com.project.meeting.controller;

import com.project.meeting.model.Meeting;
import com.project.meeting.service.MeetingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {

    private final MeetingService meetingService;

    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @PostMapping
    public ResponseEntity<Meeting> create(@RequestBody Meeting meeting) {
        return ResponseEntity.ok(meetingService.save(meeting));
    }

    @GetMapping
    public ResponseEntity<List<Meeting>> getAll() {
        return ResponseEntity.ok(meetingService.getAll());
    }
}
