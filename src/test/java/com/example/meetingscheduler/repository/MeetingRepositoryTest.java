package com.example.meetingscheduler.repository;

import com.example.meetingscheduler.entity.Interval;
import com.example.meetingscheduler.entity.Meeting;
import com.example.meetingscheduler.entity.RoomCalendar;
import com.example.meetingscheduler.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase
class MeetingRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private MeetingRepository meetingRepository;

    @Test
    void findByRoomCalendar() {
        User user1 = new User();
        user1.setUsername("user1");
        User user2 = new User();
        user2.setUsername("user2");
        User user3 = new User();
        user3.setUsername("user2");
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(user3);

        RoomCalendar roomCalendar = new RoomCalendar();
        roomCalendar.setDate(LocalDate.now());
        entityManager.persist(roomCalendar);

        Meeting meeting1 = new Meeting();
        meeting1.setCreator(user1);
        meeting1.setParticipants(List.of(user2, user3));
        meeting1.setRoomCalendar(roomCalendar);
        meeting1.setInterval(new Interval(LocalDateTime.now(), LocalDateTime.now()));
        entityManager.persist(meeting1);

        Meeting meeting2 = new Meeting();
        meeting2.setCreator(user2);
        meeting2.setParticipants(List.of(user1, user3));
        meeting2.setRoomCalendar(roomCalendar);
        meeting2.setInterval(new Interval(LocalDateTime.now(), LocalDateTime.now()));
        entityManager.persist(meeting2);

        SortedSet<Meeting> expectedSet = new TreeSet<>();
        expectedSet.add(meeting1);
        expectedSet.add(meeting2);

        SortedSet<Meeting> meetingList = meetingRepository.findByRoomCalendar(roomCalendar);


        assertEquals(expectedSet, meetingList);
    }

    @Test
    void findByRoomCalendarDateAndParticipants() {
        LocalDate date = LocalDate.now();

        User user1 = new User();
        user1.setUsername("user1");
        User user2 = new User();
        user2.setUsername("user2");
        User user3 = new User();
        user3.setUsername("user3");
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(user3);

        RoomCalendar roomCalendar1 = new RoomCalendar();
        roomCalendar1.setDate(date);
        entityManager.persist(roomCalendar1);

        RoomCalendar roomCalendar2 = new RoomCalendar();
        roomCalendar2.setDate(date);
        entityManager.persist(roomCalendar2);

        Meeting meeting1 = new Meeting();
        meeting1.setCreator(user1);
        meeting1.setParticipants(List.of(user2, user3));
        meeting1.setRoomCalendar(roomCalendar1);
        meeting1.setInterval(new Interval(LocalDateTime.now(), LocalDateTime.now()));
        entityManager.persist(meeting1);

        Meeting meeting2 = new Meeting();
        meeting2.setCreator(user2);
        meeting2.setParticipants(List.of(user1, user3));
        meeting2.setRoomCalendar(roomCalendar1);
        meeting2.setInterval(new Interval(LocalDateTime.now(), LocalDateTime.now()));
        entityManager.persist(meeting2);

        Meeting meeting3 = new Meeting();
        meeting3.setCreator(user2);
        meeting3.setParticipants(List.of(user3));
        meeting3.setRoomCalendar(roomCalendar2);
        meeting3.setInterval(new Interval(LocalDateTime.now(), LocalDateTime.now()));
        entityManager.persist(meeting3);

        SortedSet<Meeting> expectSet = new TreeSet<>();
        expectSet.add(meeting1);
        expectSet.add(meeting2);
        expectSet.add(meeting3);

        SortedSet<Meeting> meetingList = meetingRepository
                .findByRoomCalendarDateAndParticipants(date, user3);
        assertEquals(expectSet, meetingList);
    }
}