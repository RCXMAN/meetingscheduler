package com.example.meetingscheduler.controller.request;

import com.example.meetingscheduler.entity.Meeting;
import com.example.meetingscheduler.entity.User;
import lombok.Data;

import java.util.List;

@Data
public class CancelMeetingRequest {
    String creatorUsername;
    Long meetingId;
    String content;
}
