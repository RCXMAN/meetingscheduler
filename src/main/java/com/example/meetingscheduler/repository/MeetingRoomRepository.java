package com.example.meetingscheduler.repository;

import com.example.meetingscheduler.entity.Meeting;
import com.example.meetingscheduler.entity.MeetingRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingRoomRepository extends JpaRepository<MeetingRoom, Long> {
    Optional<List<MeetingRoom>> findByCapacityGreaterThanEqualAndIsAvailableIsTrueOrderByCapacityAsc(int numberOfPersons);
    Optional<MeetingRoom> findByBookedMeetingMeetings(Meeting meeting);
}
