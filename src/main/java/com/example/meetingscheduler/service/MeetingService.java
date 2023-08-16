package com.example.meetingscheduler.service;

import com.example.meetingscheduler.entity.Interval;
import com.example.meetingscheduler.entity.Meeting;
import com.example.meetingscheduler.entity.MeetingRoom;
import com.example.meetingscheduler.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

public interface MeetingService {
    void respondInvitation(Long meetingId, String username);
    // 并发问题
    boolean scheduleMeeting(List<Long> attendeesId, Interval interval, String content);
    // 并发问题
    boolean scheduleMeeting_withRoom(Long roomId, List<Long> attendeesId, Interval interval, String content);
    boolean cancelMeetingByCreator(String creatorUsername, Long meetingId, String content);
    void cancelMeetingDueMaintenance(Long roomId, LocalDateTime startTime, LocalDateTime endTime);
    SortedSet<Meeting> getMeetingsOfUserByDay(String username, LocalDate date);
    Map<LocalDate, SortedSet<Meeting>> getMeetingsOfUserByWeek(String username, LocalDate date);
    Map<LocalDate, SortedSet<Meeting>> getMeetingsOfUserByMonth(String username, LocalDate date);
    SortedSet<Meeting> getRoomMeetingsByDay(Long roomId, LocalDate date);
    Map<LocalDate, SortedSet<Meeting>> getRoomMeetingsByWeekOfDay(Long roomId, LocalDate date);
    Map<LocalDate, SortedSet<Meeting>> getRoomMeetingsByMonthOfDay(Long roomId, LocalDate date);
}
