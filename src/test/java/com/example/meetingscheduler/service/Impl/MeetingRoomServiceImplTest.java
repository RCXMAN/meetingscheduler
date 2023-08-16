package com.example.meetingscheduler.service.Impl;

import com.example.meetingscheduler.entity.Interval;
import com.example.meetingscheduler.entity.Meeting;
import com.example.meetingscheduler.entity.MeetingRoom;
import com.example.meetingscheduler.entity.RoomCalendar;
import com.example.meetingscheduler.exception.ExceedCapacity;
import com.example.meetingscheduler.repository.MeetingRepository;
import com.example.meetingscheduler.repository.MeetingRoomRepository;
import com.example.meetingscheduler.service.MeetingRoomService;
import com.example.meetingscheduler.service.RoomCalendarService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.example.meetingscheduler.MeetingSchedulerConstant.CLOSED_TIME;
import static com.example.meetingscheduler.MeetingSchedulerConstant.STARTED_TIME;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class MeetingRoomServiceImplTest {
    @Mock
    private MeetingRoomRepository meetingRoomRepository;
    @Mock
    private RoomCalendarService roomCalendarService;
    @Mock
    private MeetingRepository meetingRepository;
    @InjectMocks
    private MeetingRoomServiceImpl meetingRoomService;

    @Test
    void getAllRooms() {
        MeetingRoom room1 = new MeetingRoom();
        MeetingRoom room2 = new MeetingRoom();

        List<MeetingRoom> meetingRoomList = new ArrayList<>(List.of(room1, room2));

        when(meetingRoomRepository.findAll()).thenReturn(meetingRoomList);

        assertEquals(meetingRoomList, meetingRoomService.getAllRooms());
    }

    @Test
    void getMeetingRoomById() {
        MeetingRoom room = new MeetingRoom();
        room.setRoomId(1L);

        when(meetingRoomRepository.findById(1L)).thenReturn(Optional.of(room));

        assertEquals(room, meetingRoomService.getMeetingRoomById(1L));
    }

    @Test
    void getMeetingRoomByMeeting() {
        Meeting meeting = new Meeting();
        meeting.setId(1L);

        MeetingRoom room = new MeetingRoom();
        room.setRoomId(2L);

        when(meetingRoomRepository.findByBookedMeetingMeetings(meeting))
                .thenReturn(Optional.of(room));

        MeetingRoom actualRoom = meetingRoomService.getMeetingRoomByMeeting(meeting);

        assertEquals(room, actualRoom);
    }

    @Test
    void createNewMeetingRoom() {
        int capacity = 6;
        boolean isAvailable = true;
        MeetingRoom room = new MeetingRoom();
        room.setCapacity(capacity);
        room.setAvailable(isAvailable);

        when(meetingRoomRepository.save(any())).thenReturn(room);

        assertEquals(room, meetingRoomService.createNewMeetingRoom(6, isAvailable));
    }

    @Test
    void updateRoomAvailability() {
        Long roomId = 1L;
        boolean available = false;
        MeetingRoom room = new MeetingRoom();
        room.setAvailable(true);
        room.setRoomId(roomId);

        when(meetingRoomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(meetingRoomRepository.save(room)).thenReturn(room);

        MeetingRoom updatedRoom = meetingRoomService.updateRoomAvailability(roomId, available);

        assertFalse(updatedRoom.isAvailable());
    }

    @Test
    void getAvailabilityRooms() {
        LocalDateTime now = LocalDateTime.of(LocalDate.now(), LocalTime.of(12,0,0));
        LocalDateTime end = now.plus(3, ChronoUnit.HOURS);

        MeetingRoom room1 = new MeetingRoom();
        room1.setRoomId(1L);
        room1.setCapacity(10);
        room1.setAvailable(true);

        MeetingRoom room2 = new MeetingRoom();
        room1.setRoomId(2L);
        room2.setCapacity(15);
        room2.setAvailable(true);

        MeetingRoom room3 = new MeetingRoom();
        room1.setRoomId(3L);
        room3.setCapacity(18);
        room3.setAvailable(true);;

        List<MeetingRoom> allRooms = new ArrayList<>();
        allRooms.add(room1);
        allRooms.add(room2);
        allRooms.add(room3);

        when(meetingRoomRepository.findByCapacityGreaterThanEqualAndIsAvailableIsTrueOrderByCapacityAsc(10))
                .thenReturn(Optional.of(allRooms));

        RoomCalendar roomCalendar = new RoomCalendar();

        Meeting meeting = new Meeting();
        meeting.setId(1L);
        meeting.setInterval(new Interval(LocalDate.now().atTime(STARTED_TIME), LocalDate.now().atTime(CLOSED_TIME)));
        roomCalendar.getMeetings().add(meeting);

        RoomCalendar roomCalendar2 = new RoomCalendar();

        Meeting meeting1 = new Meeting();
        meeting1.setId(2L);
        meeting1.setInterval(new Interval(now.minus(1, ChronoUnit.HOURS),
                now.plus(1, ChronoUnit.HOURS)));

        Meeting meeting2 = new Meeting();
        meeting2.setId(3L);
        meeting2.setInterval(new Interval(now.minus(3, ChronoUnit.HOURS), now.minus(2, ChronoUnit.HOURS)));

        roomCalendar2.getMeetings().add(meeting1);
        roomCalendar2.getMeetings().add(meeting2);

        // Simulate the behavior of isRoomAvailable, assuming it's already tested.
        when(roomCalendarService.getRoomCalenderByRoomAndDate(room1, now.toLocalDate())).thenReturn(roomCalendar);
        when(meetingRepository.findByRoomCalendar(roomCalendar)).thenReturn(roomCalendar.getMeetings());
        when(roomCalendarService.getRoomCalenderByRoomAndDate(room2, now.toLocalDate())).thenReturn(roomCalendar2);
        when(meetingRepository.findByRoomCalendar(roomCalendar2)).thenReturn(roomCalendar2.getMeetings());

        when(roomCalendarService.getRoomCalenderByRoomAndDate(room3, now.toLocalDate())).thenReturn(new RoomCalendar());

        List<MeetingRoom> availableRooms = meetingRoomService.getAvailabilityRooms(10, new Interval(now, end));

        assertEquals(1, availableRooms.size());
        assertEquals(room3, availableRooms.get(0));
    }

    @Test
    void getAvailableRoom_Quick() {
        LocalDateTime now = LocalDateTime.of(LocalDate.now(), LocalTime.of(12,0,0));
        LocalDateTime end = now.plus(3, ChronoUnit.HOURS);

        MeetingRoom room1 = new MeetingRoom();
        room1.setCapacity(10);
        room1.setAvailable(true);

        MeetingRoom room2 = new MeetingRoom();
        room2.setCapacity(15);
        room2.setAvailable(true);

        MeetingRoom room3 = new MeetingRoom();
        room3.setCapacity(18);
        room3.setAvailable(true);

        List<MeetingRoom> allRooms = new ArrayList<>();
        allRooms.add(room1);
        allRooms.add(room2);
        allRooms.add(room3);

        when(meetingRoomRepository.findByCapacityGreaterThanEqualAndIsAvailableIsTrueOrderByCapacityAsc(10))
                .thenReturn(Optional.of(allRooms));

        when(roomCalendarService.getRoomCalenderByRoomAndDate(room1, now.toLocalDate())).thenReturn(new RoomCalendar());
        when(roomCalendarService.getRoomCalenderByRoomAndDate(room2, now.toLocalDate())).thenReturn(new RoomCalendar());
        when(roomCalendarService.getRoomCalenderByRoomAndDate(room3, now.toLocalDate())).thenReturn(new RoomCalendar());

        // Test the getAvailableRoom_Quick method
        MeetingRoom availableRoom = meetingRoomService.getAvailableRoom_Quick(10, new Interval(now, end));

        assertEquals(room1, availableRoom); //
    }

    @Test
    void checkRoomAvailability() {
        long roomId = 1L;
        int roomCapacity = 15;
        int numberOfPersons = 12;
        LocalDateTime now = LocalDateTime.of(LocalDate.now(), LocalTime.of(12,0,0));
        LocalDateTime end = now.plus(3, ChronoUnit.HOURS);

        MeetingRoom room = new MeetingRoom();
        room.setRoomId(roomId);
        room.setCapacity(roomCapacity);
        room.setAvailable(true);

        when(meetingRoomRepository.findById(roomId)).thenReturn(Optional.of(room));

        when(roomCalendarService.getRoomCalenderByRoomAndDate(eq(room), any(LocalDate.class))).thenReturn(new RoomCalendar());

        MeetingRoom availableRoom = meetingRoomService.checkRoomAvailability(roomId, numberOfPersons, new Interval(now, end));

        assertEquals(room, availableRoom);
    }

    @Test
    void checkRoomAvailability_WhenNotAvailable() {
        long roomId = 2L;
        int roomCapacity = 18;
        int numberOfPersons = 20; // More than room capacity
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = now.plus(3, ChronoUnit.HOURS);

        MeetingRoom room = new MeetingRoom();
        room.setRoomId(roomId);
        room.setCapacity(roomCapacity);
        room.setAvailable(true);

        when(meetingRoomRepository.findById(roomId)).thenReturn(Optional.of(room));

        // Simulate room unavailability using mocked behaviors
        when(roomCalendarService.getRoomCalenderByRoomAndDate(eq(room), any(LocalDate.class))).thenReturn(new RoomCalendar());

        assertThrows(ExceedCapacity.class, () -> {
            meetingRoomService.checkRoomAvailability(roomId, numberOfPersons, new Interval(now, end));
        });
    }
}