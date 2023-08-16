package com.example.meetingscheduler.utils;

import com.example.meetingscheduler.MeetingSchedulerConstant;
import com.example.meetingscheduler.entity.Meeting;
import com.example.meetingscheduler.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;


public class Utils {
    public static String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public static String generateInvitationContent(User user, Meeting meeting, String content) {
        String declineLink = MeetingSchedulerConstant.BASE_Url
                + "/users/decline-meeting?user="
                + user.getUsername()
                + "&meeting=" + meeting.getId();

        return content + "\n\n"
                + "If you want decline the meeting, please click the link below:\n"
                + "Decline Link: " + declineLink;
    }

    public static String generateRoomMaintenanceContent(Meeting meeting) {
        return "Your meeting on " + meeting.getInterval() + " (Meeting ID: " + meeting.getId()
                + ") has been cancelled due to maintenance of the meeting room.";
    }
}
