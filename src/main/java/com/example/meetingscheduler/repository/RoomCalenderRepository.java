package com.example.meetingscheduler.repository;

import com.example.meetingscheduler.entity.MeetingRoom;
import com.example.meetingscheduler.entity.RoomCalendar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface RoomCalenderRepository extends JpaRepository<RoomCalendar, Long> {
    Optional<RoomCalendar> findByMeetingRoomAndDate(MeetingRoom room, LocalDate date);
}
