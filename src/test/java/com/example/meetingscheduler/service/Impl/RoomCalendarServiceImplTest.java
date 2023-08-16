package com.example.meetingscheduler.service.Impl;

import com.example.meetingscheduler.entity.MeetingRoom;
import com.example.meetingscheduler.entity.RoomCalendar;
import com.example.meetingscheduler.repository.RoomCalenderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class RoomCalendarServiceImplTest {
    @Mock
    private RoomCalenderRepository roomCalenderRepository;
    @InjectMocks
    private RoomCalendarServiceImpl roomCalendarService;

    @Test
    void getRoomCalenderByRoomAndDate() {
        MeetingRoom room = new MeetingRoom();
        LocalDate date = LocalDate.now();

        RoomCalendar calendar = new RoomCalendar();

        when(roomCalenderRepository.findByMeetingRoomAndDate(room, date)).thenReturn(Optional.of(calendar));

        assertEquals(calendar, roomCalendarService.getRoomCalenderByRoomAndDate(room, date));
    }
}