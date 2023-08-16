package com.example.meetingscheduler.service.Impl;

import com.example.meetingscheduler.entity.User;
import com.example.meetingscheduler.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

//@Service
//@RequiredArgsConstructor
//public class NotificationServiceImpl implements NotificationService {
//
//    private final SimpMessagingTemplate messagingTemplate;
//
//    @Override
//    public void sendInviteNotification(User user, String content) {
//        // deal with content
//        messagingTemplate.convertAndSendToUser(user.getUsername(),"/specific", content);
//    }
//
//    @Override
//    public void sendCancelNotification(User user, String content) {
//        // deal with content
//        messagingTemplate.convertAndSendToUser(user.getUsername(),"/specific", content);
//    }
//
//}
