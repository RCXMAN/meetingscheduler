package com.example.meetingscheduler.service.Impl;

import com.example.meetingscheduler.MeetingSchedulerConstant;
import com.example.meetingscheduler.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;

@ExtendWith(SpringExtension.class)
class EmailServiceTest {
    @Mock
    private JavaMailSender javaMailSender;
    @InjectMocks
    private EmailService emailService;

    @Test
    void sendEmailToUser() {
        User user = new User();
        user.setEmail("test@example.com");

        String subject = "Test Subject";
        String body = "Test Body";

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        doNothing().when(javaMailSender).send(messageCaptor.capture());

        emailService.sendEmailToUser(user, subject, body);

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals(MeetingSchedulerConstant.EMAIL, sentMessage.getFrom());
        assertEquals(user.getEmail(), sentMessage.getTo()[0]);
        assertEquals(subject, sentMessage.getSubject());
        assertEquals(body, sentMessage.getText());
    }
}