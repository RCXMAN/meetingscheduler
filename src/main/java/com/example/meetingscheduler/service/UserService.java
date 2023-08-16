package com.example.meetingscheduler.service;

import com.example.meetingscheduler.entity.Meeting;
import com.example.meetingscheduler.entity.User;

import java.util.List;

public interface UserService {
    User getUserByUsername(String username);
    List<User> getUsersByIds(List<Long> ids);
}
