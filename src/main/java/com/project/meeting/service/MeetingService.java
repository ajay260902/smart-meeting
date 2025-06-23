package com.project.meeting.service;

import com.project.meeting.model.Meeting;
import com.project.meeting.repository.MeetingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MeetingService {
    private final MeetingRepository repository;

    public MeetingService(MeetingRepository repository) {
        this.repository = repository;
    }

    public Meeting save(Meeting meeting) {
        return repository.save(meeting);
    }

    public List<Meeting> getAll() {
        return repository.findAll();
    }
}
