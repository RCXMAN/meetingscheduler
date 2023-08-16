package com.example.meetingscheduler.repository;

import com.example.meetingscheduler.entity.Meeting;
import com.example.meetingscheduler.entity.RoomCalendar;
import com.example.meetingscheduler.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.SortedSet;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    SortedSet<Meeting> findByRoomCalendar(RoomCalendar calendar);
    SortedSet<Meeting> findByRoomCalendarDateAndParticipants(LocalDate date, User user);
}
