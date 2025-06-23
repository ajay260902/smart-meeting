package com.project.meeting.controller;

import com.project.meeting.model.EventRequest;
import com.project.meeting.service.CalendarService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RestController
@CrossOrigin("*")
@RequestMapping("/calendar/events")
public class CalendarController {

    @Autowired
    private CalendarService calendarService;

    @GetMapping
    public ResponseEntity<?> getUserEvents(@RequestParam String email) {
        try {
            String eventsJson = calendarService.getUpcomingEvents(email);
            return ResponseEntity.ok(eventsJson);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createEvent(
            @RequestParam String email,
            @ModelAttribute EventRequest request
    ){
        try {
            String createdEvent = calendarService.createEvent(email, request);
            return ResponseEntity.ok(createdEvent);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body( e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateEvent(@RequestParam String email, @RequestBody EventRequest updatedEvent) {
        String result = calendarService.updateEvent(email, updatedEvent);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteEvent(@RequestParam String email, @RequestParam String eventId) {
        String result = calendarService.deleteEvent(email, eventId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/getColors")
    public ResponseEntity<?> getCalendarColors(@RequestParam String email) {
        try {
            String eventsJson = calendarService.getCalendarColors(email);
            return ResponseEntity.ok(eventsJson);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

}
