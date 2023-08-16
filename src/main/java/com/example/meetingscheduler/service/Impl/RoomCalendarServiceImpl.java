package com.example.meetingscheduler.service.Impl;

import com.example.meetingscheduler.entity.MeetingRoom;
import com.example.meetingscheduler.entity.RoomCalendar;
import com.example.meetingscheduler.repository.RoomCalenderRepository;
import com.example.meetingscheduler.service.RoomCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;


@Service
@RequiredArgsConstructor
public class RoomCalendarServiceImpl implements RoomCalendarService {
    private final RoomCalenderRepository roomCalenderRepository;
    @Override
    public RoomCalendar getRoomCalenderByRoomAndDate(MeetingRoom room, LocalDate bookingDate) {
        return roomCalenderRepository.findByMeetingRoomAndDate(room, bookingDate)
                .orElse(new RoomCalendar(room, bookingDate));
    }
}
