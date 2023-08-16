package com.example.meetingscheduler.repository;

import com.example.meetingscheduler.entity.Meeting;
import com.example.meetingscheduler.entity.MeetingRoom;
import com.example.meetingscheduler.entity.RoomCalendar;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase
class MeetingRoomRepositoryTest{
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private MeetingRoomRepository roomRepository;

    @Test
    void findByCapacityGreaterThanEqualAAndAvailableIsTrueOrderByCapacityAsc() {
        MeetingRoom room1 = new MeetingRoom();
        room1.setCapacity(8);
        room1.setAvailable(true);

        entityManager.persist(room1);

        MeetingRoom room2 = new MeetingRoom();
        room2.setCapacity(6);
        room2.setAvailable(true);
        entityManager.persist(room2);

        MeetingRoom room3 = new MeetingRoom();
        room3.setCapacity(2);
        room3.setAvailable(true);
        entityManager.persist(room3);

        MeetingRoom room4 = new MeetingRoom();
        room4.setCapacity(10);
        room4.setAvailable(false);
        entityManager.persist(room4);

        List<MeetingRoom> rooms = roomRepository
                .findByCapacityGreaterThanEqualAndIsAvailableIsTrueOrderByCapacityAsc(6)
                .orElse(new ArrayList<>());
        assertEquals(rooms.size(), 2);
        assertEquals(rooms.get(0), room2);
        assertEquals(rooms.get(1), room1);
    }

    @Test
    void findByBookedMeetingMeetings() {
        MeetingRoom room = new MeetingRoom();
        entityManager.persist(room);

        RoomCalendar calendar = new RoomCalendar(room, LocalDate.now());
        entityManager.persist(calendar);

        Meeting meeting1 = new Meeting();
        meeting1.setRoomCalendar(calendar);
        entityManager.persist(meeting1);

        RoomCalendar calendar2 = new RoomCalendar(room, LocalDate.now().plusDays(1));
        entityManager.persist(calendar2);

        Meeting meeting2 = new Meeting();
        meeting2.setRoomCalendar(calendar2);
        entityManager.persist(meeting2);

        MeetingRoom foundRoom = roomRepository.findByBookedMeetingMeetings(meeting1)
                .orElse(null);


        assertNotNull(foundRoom);
        assertEquals(room, foundRoom);

        MeetingRoom foundRoom2 = roomRepository.findByBookedMeetingMeetings(meeting2)
                .orElse(null);
        assertNotNull(foundRoom2);
    }
}