package com.example.meetingscheduler.service;

import com.example.meetingscheduler.entity.User;

public interface NotificationService {
    void sendInviteNotification(User user, String content);
    void sendCancelNotification(User user, String content);
}

