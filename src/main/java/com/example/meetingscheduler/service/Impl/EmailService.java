package com.example.meetingscheduler.service.Impl;

import com.example.meetingscheduler.MeetingSchedulerConstant;
import com.example.meetingscheduler.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendEmailToUser(User user,
                                 String subject,
                                 String body) {
        SimpleMailMessage message = new SimpleMailMessage();

        String toEmail = user.getEmail();
        message.setFrom(MeetingSchedulerConstant.EMAIL);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }
}
