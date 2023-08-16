package com.example.meetingscheduler.service.Impl;

import com.example.meetingscheduler.entity.*;
import com.example.meetingscheduler.repository.MeetingRepository;
import com.example.meetingscheduler.service.MeetingRoomService;
import com.example.meetingscheduler.service.RoomCalendarService;
import com.example.meetingscheduler.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class MeetingServiceImplTest {
    @Mock
    private MeetingRepository meetingRepository;
    @Mock
    private MeetingRoomService meetingRoomService;
    @Mock
    private RoomCalendarService roomCalendarService;
    @Mock
    private EmailService emailService;
    @Mock
    private UserService userService;
    @InjectMocks
    private MeetingServiceImpl meetingService;

    @Test
    void respondInvitation() {
        Meeting meeting = new Meeting();
        meeting.setId(1L);

        User user1 = new User();
        user1.setId(100L);
        user1.setUsername("user1");

        User user2 = new User();
        user2.setId(110L);
        user1.setUsername("user2");

        List<User> userList = new ArrayList<>();
        userList.add(user1);
        userList.add(user2);
        meeting.setParticipants(userList);

        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));
        when(userService.getUserByUsername("user1")).thenReturn(user1);

        meetingService.respondInvitation(1L, "user1");

        assertEquals(List.of(user2), meeting.getParticipants());
    }

    @Test
    @WithMockUser(username = "test_user")
    void scheduleMeeting() {
        List<Long> attendeesId = List.of(1L, 2L);
        Interval interval = new Interval(LocalDateTime.now(), LocalDateTime.now().plusHours(2));
        String content = "Meeting content";

        User creator = new User();
        when(userService.getUserByUsername(anyString())).thenReturn(creator);

        MeetingRoom room = new MeetingRoom();
        room.setCapacity(2);
        when(meetingRoomService.getAvailableRoom_Quick(eq(2), any(Interval.class))).thenReturn(room);

        RoomCalendar roomCalendar = new RoomCalendar();
        when(roomCalendarService.getRoomCalenderByRoomAndDate(eq(room), any(LocalDate.class))).thenReturn(roomCalendar);
        when(userService.getUsersByIds(attendeesId)).thenReturn(List.of(new User(), new User()));
        when(meetingRepository.save(any(Meeting.class))).thenReturn(new Meeting());

        boolean result = meetingService.scheduleMeeting(attendeesId, interval, content);
        assertTrue(result);
        verify(emailService, times(2)).sendEmailToUser(any(), any(), any());
    }

    @Test
    @WithMockUser(username = "test_user")
    void scheduleMeeting_withRoom() {
        Long roomId = 1L;
        List<Long> attendeesId = List.of(1L, 2L);
        Interval interval = new Interval(LocalDateTime.now(), LocalDateTime.now().plusHours(2));
        String content = "Meeting content";

        User creator = new User();
        when(userService.getUserByUsername(anyString())).thenReturn(creator);

        MeetingRoom room = new MeetingRoom();
        room.setCapacity(2);
        when(meetingRoomService.checkRoomAvailability(eq(roomId), eq(2), any(Interval.class))).thenReturn(room);

        RoomCalendar roomCalendar = new RoomCalendar();
        when(roomCalendarService.getRoomCalenderByRoomAndDate(eq(room), any(LocalDate.class))).thenReturn(roomCalendar);
        when(meetingRepository.save(any(Meeting.class))).thenReturn(new Meeting());

        boolean result = meetingService.scheduleMeeting_withRoom(roomId, attendeesId, interval, content);

        assertTrue(result);
    }

    @Test
    @WithMockUser(username = "test_user")
    void scheduleMeeting_withNonRoom() {
        Long roomId = 1L;
        List<Long> attendeesId = List.of(1L, 2L);
        Interval interval = new Interval(LocalDateTime.now(), LocalDateTime.now().plusHours(2));
        String content = "Meeting content";

        User creator = new User();
        when(userService.getUserByUsername(anyString())).thenReturn(creator);

        MeetingRoom room = new MeetingRoom();
        room.setCapacity(2);
        when(meetingRoomService.checkRoomAvailability(eq(roomId), eq(2), any(Interval.class))).thenReturn(null);

        boolean result = meetingService.scheduleMeeting_withRoom(roomId, attendeesId, interval, content);

        assertFalse(result);
    }

    @Test
    void cancelMeetingByCreator() {
        Long meetingId = 1L;
        String creatorUsername = "creator";
        String content = "Meeting canceled";

        User creator = new User();
        creator.setUsername(creatorUsername);

        Meeting meeting = new Meeting();
        meeting.setId(meetingId);
        meeting.setCreator(creator);
        meeting.setParticipants(List.of(new User(), new User()));

        MeetingRoom room = new MeetingRoom();
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));
        when(meetingRoomService.getMeetingRoomByMeeting(meeting)).thenReturn(room);
        when(userService.getUserByUsername(creatorUsername)).thenReturn(creator);

        boolean result = meetingService.cancelMeetingByCreator(creatorUsername, meetingId, content);

        assertTrue(result);
        verify(emailService, times(2)).sendEmailToUser(any(), any(), any());
    }

    @Test
    void cancelMeetingByCreator_Failure_NotCreator() {
        Long meetingId = 1L;
        String creatorUsername = "creator";
        String content = "Cancellation content";

        Meeting meeting = new Meeting();
        User differentUser = new User();
        differentUser.setUsername("otherUser");
        meeting.setCreator(differentUser);

        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));
        when(userService.getUserByUsername(creatorUsername)).thenReturn(new User());

        boolean result = meetingService.cancelMeetingByCreator(creatorUsername, meetingId, content);

        assertFalse(result);
    }

    @Test
    void cancelMeetingByCreator_Failure_NonRoom() {
        Long meetingId = 1L;
        String creatorUsername = "creator";
        String content = "Meeting canceled";

        User creator = new User();
        creator.setUsername(creatorUsername);

        Meeting meeting = new Meeting();
        meeting.setId(meetingId);
        meeting.setCreator(creator);

        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));
        when(meetingRoomService.getMeetingRoomByMeeting(meeting)).thenReturn(null);
        when(userService.getUserByUsername(creatorUsername)).thenReturn(creator);

        boolean result = meetingService.cancelMeetingByCreator(creatorUsername, meetingId, content);

        assertFalse(result);
    }

    @Test
    void testCancelMeetingDueMaintenance() {
        MeetingRoom room = new MeetingRoom();
        when(meetingRoomService.updateRoomAvailability(anyLong(), anyBoolean())).thenReturn(room);

        RoomCalendar calendar = new RoomCalendar();
        when(roomCalendarService.getRoomCalenderByRoomAndDate(any(), any(LocalDate.class))).thenReturn(calendar);

        Meeting meeting = new Meeting();
        meeting.setInterval(new Interval(LocalDateTime.now(), LocalDateTime.now()));
        meeting.setParticipants(List.of(new User(), new User()));

        calendar.getMeetings().add(meeting);

        when(meetingRepository.findByRoomCalendar(any())).thenReturn(calendar.getMeetings());

        LocalDate maintenanceStartDate = LocalDate.of(2023, Month.AUGUST, 15);
        LocalDate maintenanceEndDate = LocalDate.of(2023, Month.AUGUST, 19);

        meetingService.cancelMeetingDueMaintenance(
                1L,
                        LocalDateTime.of(maintenanceStartDate, LocalTime.MIN),
                        LocalDateTime.of(maintenanceEndDate, LocalTime.MAX)
        );

        verify(meetingRoomService, times(1)).updateRoomAvailability(anyLong(), eq(false));
        verify(roomCalendarService, times(5)).getRoomCalenderByRoomAndDate(any(), any(LocalDate.class));
        verify(meetingRepository, times(5)).findByRoomCalendar(any());
        verify(meetingRepository, atLeast(1)).deleteAllInBatch(anySet());
        verify(emailService, atLeast(2)).sendEmailToUser(any(), any(), any());
    }

    @Test
    void getMeetingsOfUserByDay() {
        String username = "user123";
        LocalDate date = LocalDate.of(2023, 8, 14);

        User user = new User();
        user.setUsername(username);

        SortedSet<Meeting> meetings = new TreeSet<>();
        Meeting meeting1 = new Meeting();
        meeting1.setInterval(new Interval(LocalDateTime.now(), LocalDateTime.now().plusHours(2)));
        Meeting meeting2 = new Meeting();
        meeting2.setInterval(new Interval(LocalDateTime.now(), LocalDateTime.now().plusHours(2)));
        meetings.add(meeting1);
        meetings.add(meeting2);

        when(userService.getUserByUsername(username)).thenReturn(user);
        when(meetingRepository.findByRoomCalendarDateAndParticipants(date, user))
                .thenReturn(meetings);

        SortedSet<Meeting> result = meetingService.getMeetingsOfUserByDay(username, date);

        assertEquals(result, meetings);
    }

    @Test
    void getMeetingsOfUserByWeek() {
        String username = "user123";
        LocalDate date = LocalDate.of(2023, 8, 14);

        User user = new User();
        user.setUsername(username);

        LocalDate startOfWeek = date.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        Meeting meeting1 = new Meeting();
        meeting1.setInterval(new Interval(LocalDateTime.now(), LocalDateTime.now()));
        Meeting meeting2 = new Meeting();
        meeting2.setInterval(new Interval(LocalDateTime.now(), LocalDateTime.now()));
        Meeting meeting3 = new Meeting();
        meeting3.setInterval(new Interval(LocalDateTime.now(), LocalDateTime.now()));


        RoomCalendar roomCalendar1 = new RoomCalendar();
        roomCalendar1.getMeetings().add(meeting1);

        RoomCalendar roomCalendar2 = new RoomCalendar();
        roomCalendar2.getMeetings().add(meeting2);
        roomCalendar2.getMeetings().add(meeting3);

        RoomCalendar roomCalendar3 = new RoomCalendar();

        when(userService.getUserByUsername(username)).thenReturn(user);
        when(meetingRepository.findByRoomCalendarDateAndParticipants(startOfWeek, user))
                .thenReturn(roomCalendar1.getMeetings());
        when(meetingRepository.findByRoomCalendarDateAndParticipants(startOfWeek.plusDays(1), user))
                .thenReturn(roomCalendar3.getMeetings());
        when(meetingRepository.findByRoomCalendarDateAndParticipants(endOfWeek, user))
                .thenReturn(roomCalendar2.getMeetings());

        Map<LocalDate, SortedSet<Meeting>> result = meetingService.getMeetingsOfUserByWeek(username, date);

        assertEquals(7, result.size());
        assertEquals(roomCalendar1.getMeetings(), result.get(startOfWeek));
        assertTrue(result.get(startOfWeek.plusDays(1)).isEmpty());
        assertEquals(roomCalendar2.getMeetings(), result.get(endOfWeek));
    }

    @Test
    void getMeetingsOfUserByMonth() {
        String username = "user123";
        LocalDate date = LocalDate.of(2023, 8, 14);

        User user = new User();
        user.setUsername(username);

        LocalDate startOfMonth = date.withDayOfMonth(1);
        LocalDate endOfMonth = date.withDayOfMonth(date.lengthOfMonth());

        Meeting meeting1 = new Meeting();
        meeting1.setInterval(new Interval(LocalDateTime.now(), LocalDateTime.now()));
        Meeting meeting2 = new Meeting();
        meeting2.setInterval(new Interval(LocalDateTime.now(), LocalDateTime.now()));
        Meeting meeting3 = new Meeting();
        meeting3.setInterval(new Interval(LocalDateTime.now(), LocalDateTime.now()));

        RoomCalendar roomCalendar1 = new RoomCalendar();
        roomCalendar1.getMeetings().add(meeting1);
        roomCalendar1.getMeetings().add(meeting2);

        RoomCalendar roomCalendar2 = new RoomCalendar();
        roomCalendar2.getMeetings().add(meeting3);


        when(userService.getUserByUsername(username)).thenReturn(user);
        when(meetingRepository.findByRoomCalendarDateAndParticipants(startOfMonth, user))
                .thenReturn(roomCalendar1.getMeetings());
        when(meetingRepository.findByRoomCalendarDateAndParticipants(endOfMonth, user))
                .thenReturn(roomCalendar2.getMeetings());

        // Call the method
        Map<LocalDate, SortedSet<Meeting>> result = meetingService.getMeetingsOfUserByMonth(username, date);

        assertEquals(date.lengthOfMonth(), result.size()); // Ensure the map contains all days of the month
        assertEquals(roomCalendar1.getMeetings(), result.get(startOfMonth)); // Ensure meetings for the first day are correct
        assertEquals(roomCalendar2.getMeetings(), result.get(endOfMonth));
    }

    @Test
    void getRoomMeetingsByDay() {
        Long roomId = 1L;
        LocalDate date = LocalDate.of(2023, 8, 14);

        MeetingRoom room = new MeetingRoom();
        room.setRoomId(roomId);

        RoomCalendar calendar = new RoomCalendar();

        Meeting meeting1 = new Meeting();
        meeting1.setInterval(new Interval(LocalDateTime.now(), LocalDateTime.now()));
        Meeting meeting2 = new Meeting();
        meeting2.setInterval(new Interval(LocalDateTime.now(), LocalDateTime.now()));

        calendar.getMeetings().add(meeting1);
        calendar.getMeetings().add(meeting2);

        when(meetingRoomService.getMeetingRoomById(roomId)).thenReturn(room);
        when(roomCalendarService.getRoomCalenderByRoomAndDate(room, date)).thenReturn(calendar);
        when(meetingRepository.findByRoomCalendar(calendar)).thenReturn(calendar.getMeetings());

        SortedSet<Meeting> result = meetingService.getRoomMeetingsByDay(roomId, date);

        assertEquals(calendar.getMeetings(), result);
    }

    @Test
    void getRoomMeetingsByWeekOfDay() {
        Long roomId = 1L;
        LocalDate date = LocalDate.of(2023, 8, 14);

        LocalDate startOfWeek = date.with(DayOfWeek.MONDAY);

        MeetingRoom room = new MeetingRoom();
        room.setRoomId(roomId);

        RoomCalendar calendar1 = new RoomCalendar();
        calendar1.setCalendarId(1L);
        RoomCalendar calendar2 = new RoomCalendar();
        calendar2.setCalendarId(2L);

        Meeting meeting1 = new Meeting();
        meeting1.setId(1L);
        meeting1.setInterval(new Interval(LocalDateTime.now(), LocalDateTime.now()));
        Meeting meeting2 = new Meeting();
        meeting2.setId(2L);
        meeting2.setInterval(new Interval(LocalDateTime.now(), LocalDateTime.now()));
        Meeting meeting3 = new Meeting();
        meeting3.setId(3L);
        meeting3.setInterval(new Interval(LocalDateTime.now(), LocalDateTime.now()));

        calendar1.getMeetings().add(meeting1);
        calendar2.getMeetings().add(meeting2);
        calendar2.getMeetings().add(meeting3);


        when(meetingRoomService.getMeetingRoomById(roomId)).thenReturn(room);

        when(roomCalendarService.getRoomCalenderByRoomAndDate(room, startOfWeek))
                .thenReturn(calendar1);
        when(meetingRepository.findByRoomCalendar(calendar1))
                .thenReturn(calendar1.getMeetings());

        when(roomCalendarService.getRoomCalenderByRoomAndDate(room, startOfWeek.plusDays(1)))
                .thenReturn(calendar2);
        when(meetingRepository.findByRoomCalendar(calendar2))
                .thenReturn(calendar2.getMeetings());

        Map<LocalDate, SortedSet<Meeting>> result = meetingService.getRoomMeetingsByWeekOfDay(roomId, date);

        assertEquals(7, result.size());
        assertEquals(calendar1.getMeetings(), result.get(startOfWeek));
        assertEquals(calendar2.getMeetings(), result.get(startOfWeek.plusDays(1)));
    }

    @Test
    void getRoomMeetingsByMonthOfDay() {
        Long roomId = 1L;
        LocalDate date = LocalDate.of(2023, 8, 1); // Assuming August 2023

        LocalDate startOfMonth = date.withDayOfMonth(1);
        LocalDate endOfMonth = date.withDayOfMonth(date.lengthOfMonth());

        MeetingRoom room = new MeetingRoom();
        room.setRoomId(roomId);

        RoomCalendar calendar1 = new RoomCalendar();
        calendar1.setCalendarId(1L);
        RoomCalendar calendar2 = new RoomCalendar();
        calendar2.setCalendarId(2L);

        Meeting meeting1 = new Meeting();
        meeting1.setId(1L);
        meeting1.setInterval(new Interval(LocalDateTime.now(), LocalDateTime.now()));
        Meeting meeting2 = new Meeting();
        meeting2.setId(2L);
        meeting2.setInterval(new Interval(LocalDateTime.now(), LocalDateTime.now()));
        Meeting meeting3 = new Meeting();
        meeting3.setId(3L);
        meeting3.setInterval(new Interval(LocalDateTime.now(), LocalDateTime.now()));

        calendar1.getMeetings().add(meeting1);
        calendar2.getMeetings().add(meeting2);
        calendar2.getMeetings().add(meeting3);

        when(meetingRoomService.getMeetingRoomById(roomId)).thenReturn(room);

        when(roomCalendarService.getRoomCalenderByRoomAndDate(room, startOfMonth))
                .thenReturn(calendar1);
        when(meetingRepository.findByRoomCalendar(calendar1))
                .thenReturn(calendar1.getMeetings());

        when(roomCalendarService.getRoomCalenderByRoomAndDate(room, endOfMonth))
                .thenReturn(calendar2);
        when(meetingRepository.findByRoomCalendar(calendar2))
                .thenReturn(calendar2.getMeetings());

        Map<LocalDate, SortedSet<Meeting>> result = meetingService.getRoomMeetingsByMonthOfDay(roomId, date);

        assertEquals(date.lengthOfMonth(), result.size());
        assertEquals(calendar1.getMeetings(), result.get(startOfMonth));
        assertEquals(calendar2.getMeetings(), result.get(endOfMonth));
    }
}