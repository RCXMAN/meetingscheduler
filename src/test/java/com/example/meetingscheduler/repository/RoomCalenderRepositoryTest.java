package com.example.meetingscheduler.repository;

import com.example.meetingscheduler.entity.MeetingRoom;
import com.example.meetingscheduler.entity.RoomCalendar;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase
class RoomCalenderRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private RoomCalenderRepository roomCalenderRepository;

    @Test
    void findByMeetingRoomAndDate() {
        MeetingRoom room = new MeetingRoom();
        LocalDate date = LocalDate.now();
        entityManager.persist(room);

        RoomCalendar calendar = new RoomCalendar();
        calendar.setDate(date);
        calendar.setMeetingRoom(room);
        entityManager.persist(calendar);

        RoomCalendar test_calendar = roomCalenderRepository.findByMeetingRoomAndDate(room, date)
                .orElse(null);

        assertEquals(test_calendar, calendar);
    }
}