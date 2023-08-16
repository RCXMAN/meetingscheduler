package com.example.meetingscheduler.controller.request;

import com.example.meetingscheduler.entity.Interval;
import com.example.meetingscheduler.entity.User;
import lombok.Data;

import java.util.List;

@Data
public class NewMeetingRequest {
    List<Long> attendeesId;
    Interval interval;
    String content;
}
