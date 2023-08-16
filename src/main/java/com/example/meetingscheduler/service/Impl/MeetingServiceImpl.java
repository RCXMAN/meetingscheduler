package com.example.meetingscheduler.service.Impl;

import com.example.meetingscheduler.entity.*;
import com.example.meetingscheduler.exception.NoSuchSourceException;
import com.example.meetingscheduler.repository.MeetingRepository;
import com.example.meetingscheduler.repository.RoomCalenderRepository;
import com.example.meetingscheduler.service.MeetingRoomService;
import com.example.meetingscheduler.service.MeetingService;
import com.example.meetingscheduler.service.RoomCalendarService;
import com.example.meetingscheduler.service.UserService;
import com.example.meetingscheduler.utils.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {
    private final MeetingRepository meetingRepository;
    private final MeetingRoomService meetingRoomService ;
    private final RoomCalendarService roomCalendarService;
    private final EmailService emailService;
    private final UserService userService;

    private final RoomCalenderRepository calenderRepository;
    @Override
    public void respondInvitation(Long meetingId, String username) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(NoSuchSourceException::new);
        User user = userService.getUserByUsername(username);
        meeting.getParticipants().remove(user);
        meetingRepository.save(meeting);
    }

    @Override
    // 并发问题
    public boolean scheduleMeeting(List<Long> attendeesId, Interval interval, String content) {
        return scheduleMeetingInternal(null, attendeesId, interval, content);
    }

    @Override
    // 并发问题
    public boolean scheduleMeeting_withRoom(Long roomId, List<Long> attendeesId, Interval interval, String content) {
        return scheduleMeetingInternal(roomId, attendeesId, interval, content);
    }

    @Override
    public boolean cancelMeetingByCreator(String creatorUsername, Long meetingId, String content) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(NoSuchSourceException::new);

        if (!meeting.getCreator().equals(userService.getUserByUsername(creatorUsername))) {
            return false;
        }

        MeetingRoom room = meetingRoomService.getMeetingRoomByMeeting(meeting);

        if (room == null) {
            return false;
        }

        List<User> users = meeting.getParticipants();

        for (User user : users) {
            emailService.sendEmailToUser(user, "Meeting Cancel", content);
        }

        meetingRepository.delete(meeting);

        return true;
    }

    @Override
    public void cancelMeetingDueMaintenance(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        MeetingRoom room = meetingRoomService.updateRoomAvailability(roomId, false);
        Interval maintenanceTime = new Interval(startTime, endTime);
        LocalDate start = maintenanceTime.getStartTime().toLocalDate();
        LocalDate end = maintenanceTime.getEndTime().toLocalDate();

        deleteMeetingsByDateRangeAndSendNotification(start, end, room);
    }

    @Override
    public SortedSet<Meeting> getMeetingsOfUserByDay(String username, LocalDate date) {
        User user = userService.getUserByUsername(username);
        return meetingRepository.findByRoomCalendarDateAndParticipants(date, user);
    }

    @Override
    public Map<LocalDate, SortedSet<Meeting>> getMeetingsOfUserByWeek(String username, LocalDate date) {
        LocalDate startOfWeek = date.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        User user = userService.getUserByUsername(username);

        return getMeetingsOfUserByDateRange(startOfWeek, endOfWeek, user);
    }

    @Override
    public Map<LocalDate, SortedSet<Meeting>> getMeetingsOfUserByMonth(String username, LocalDate date) {
        LocalDate startOfMonth = date.withDayOfMonth(1);
        LocalDate endOfMonth = date.withDayOfMonth(date.lengthOfMonth());
        User user = userService.getUserByUsername(username);

        return getMeetingsOfUserByDateRange(startOfMonth, endOfMonth, user);
    }

    private Map<LocalDate, SortedSet<Meeting>> getMeetingsOfUserByDateRange(LocalDate start, LocalDate end, User user) {
        Map<LocalDate, SortedSet<Meeting>> meetingMap = new HashMap<>();
        for (LocalDate currentDate = start; !currentDate.isAfter(end); currentDate = currentDate.plusDays(1)) {
            SortedSet<Meeting> meetingsForDay = meetingRepository.findByRoomCalendarDateAndParticipants(currentDate, user);
            meetingMap.put(currentDate, meetingsForDay);
        }
        return meetingMap;
    }

    @Override
    public SortedSet<Meeting> getRoomMeetingsByDay(Long roomId, LocalDate date) {
        MeetingRoom room = meetingRoomService.getMeetingRoomById(roomId);
        RoomCalendar calendar = roomCalendarService.getRoomCalenderByRoomAndDate(room, date);
        return meetingRepository.findByRoomCalendar(calendar);
    }

    @Override
    public Map<LocalDate, SortedSet<Meeting>> getRoomMeetingsByWeekOfDay(Long roomId, LocalDate date) {
        LocalDate startOfWeek = date.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        return getMeetingsOfRoomByDateRange(startOfWeek, endOfWeek, roomId);
    }

    @Override
    public Map<LocalDate, SortedSet<Meeting>> getRoomMeetingsByMonthOfDay(Long roomId, LocalDate date) {
        LocalDate startOfMonth = date.withDayOfMonth(1);
        LocalDate endOfMonth = date.withDayOfMonth(date.lengthOfMonth());
        return getMeetingsOfRoomByDateRange(startOfMonth, endOfMonth, roomId);
    }

    private boolean scheduleMeetingInternal(Long roomId, List<Long> attendeesId, Interval interval, String content) {
        List<User> attendees = userService.getUsersByIds(attendeesId);
        int numberOfPersons = attendeesId.size();

        MeetingRoom room;
        if (roomId != null) {
            room = meetingRoomService.checkRoomAvailability(roomId, numberOfPersons, interval);
        } else {
            room = meetingRoomService.getAvailableRoom_Quick(numberOfPersons, interval);
        }

        if (room == null) {
            return false;
        }

        LocalDate bookingDate = interval.getStartTime().toLocalDate();

        Meeting meeting = new Meeting();
        meeting.setParticipants(attendees);
        meeting.setInterval(interval);
        meeting.setCreator(userService.getUserByUsername(Utils.getCurrentUsername()));
        RoomCalendar calendar = roomCalendarService.getRoomCalenderByRoomAndDate(room, bookingDate);
        meeting.setRoomCalendar(calendar);

        meetingRepository.save(meeting);

        //send notification
        for (User user : attendees) {
            emailService.sendEmailToUser(
                    user,
                    "New Meeting",
                    Utils.generateInvitationContent(user, meeting, content));
        }

        return true;
    }

    private void deleteMeetingsByDateRangeAndSendNotification(
            LocalDate start,
            LocalDate end,
            MeetingRoom room
    ) {
        for (LocalDate currentDate = start; !currentDate.isAfter(end); currentDate = currentDate.plusDays(1)) {
            RoomCalendar calendar = roomCalendarService.getRoomCalenderByRoomAndDate(room, currentDate);

            SortedSet<Meeting> meetingsToDelete = meetingRepository.findByRoomCalendar(calendar);

            if (!meetingsToDelete.isEmpty()) {
                meetingsToDelete.forEach(meeting -> {
                    for (User user : meeting.getParticipants()) {
                        emailService.sendEmailToUser(
                                user,
                                "Meeting Cancellation",
                                Utils.generateRoomMaintenanceContent(meeting));
                    }
                });
                meetingRepository.deleteAllInBatch(meetingsToDelete);
            }
        }
    }

    private Map<LocalDate, SortedSet<Meeting>> getMeetingsOfRoomByDateRange(
            LocalDate start,
            LocalDate end,
            Long roomId) {
        Map<LocalDate, SortedSet<Meeting>> meetings = new HashMap<>();
        MeetingRoom room = meetingRoomService.getMeetingRoomById(roomId);
        for (LocalDate currentDate = start; !currentDate.isAfter(end); currentDate = currentDate.plusDays(1)) {
            RoomCalendar calendar = roomCalendarService.getRoomCalenderByRoomAndDate(room, currentDate);
            if (calendar == null || calendar.getMeetings().isEmpty()) {
                meetings.put(currentDate, null);
                continue;
            }
            SortedSet<Meeting> meetingsForDay = meetingRepository.findByRoomCalendar(calendar);
            meetings.put(currentDate, meetingsForDay);
        }

        return meetings;
    }

}
