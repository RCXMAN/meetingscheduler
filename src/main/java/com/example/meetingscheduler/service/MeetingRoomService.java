package com.example.meetingscheduler.service;

import com.example.meetingscheduler.entity.Interval;
import com.example.meetingscheduler.entity.Meeting;
import com.example.meetingscheduler.entity.MeetingRoom;
import com.example.meetingscheduler.entity.User;

import java.util.List;

public interface MeetingRoomService {
    List<MeetingRoom> getAllRooms();

    MeetingRoom getMeetingRoomById(Long roomId);

    MeetingRoom getMeetingRoomByMeeting(Meeting meeting);

    MeetingRoom createNewMeetingRoom(int capacity, boolean isAvailable);
    MeetingRoom updateRoomAvailability(Long roomId, boolean available);
    List<MeetingRoom> getAvailabilityRooms(int numberOfPersons, Interval interval);
    MeetingRoom getAvailableRoom_Quick(int numberOfPersons, Interval interval);
    MeetingRoom checkRoomAvailability(Long roomId, int numberOfPersons, Interval interval);
}
