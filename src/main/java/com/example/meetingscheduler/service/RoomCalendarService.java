package com.example.meetingscheduler.service;

import com.example.meetingscheduler.entity.MeetingRoom;
import com.example.meetingscheduler.entity.RoomCalendar;

import java.time.LocalDate;

public interface RoomCalendarService {
    RoomCalendar getRoomCalenderByRoomAndDate(MeetingRoom room, LocalDate bookingDate);
}
