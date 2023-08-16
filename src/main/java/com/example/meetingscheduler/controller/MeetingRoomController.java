package com.example.meetingscheduler.controller;

import com.example.meetingscheduler.entity.Interval;
import com.example.meetingscheduler.entity.MeetingRoom;
import com.example.meetingscheduler.service.MeetingRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/scheduler/rooms")
public class MeetingRoomController {
    private final MeetingRoomService meetingRoomService;

    @GetMapping
    public ResponseEntity<List<MeetingRoom>> getAllRooms() {
        List<MeetingRoom> rooms = meetingRoomService.getAllRooms();
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<MeetingRoom> getMeetingRoomById(@PathVariable Long roomId) {
        MeetingRoom room = meetingRoomService.getMeetingRoomById(roomId);
        return ResponseEntity.ok(room);
    }

    @PostMapping("/create-room")
    private ResponseEntity<MeetingRoom> createBookingRoom(@RequestParam int capacity,
                                                          @RequestParam boolean isAvailable) {
        MeetingRoom room = meetingRoomService.createNewMeetingRoom(capacity, isAvailable);
        return ResponseEntity.ok(room);
    }

    @PutMapping("/update-availability/{roomId}")
    public ResponseEntity<MeetingRoom> updateRoomAvailability(
            @PathVariable Long roomId,
            @RequestParam boolean available) {
        MeetingRoom updatedRoom = meetingRoomService.updateRoomAvailability(roomId, available);
        return ResponseEntity.ok(updatedRoom);
    }

    @GetMapping("/available-rooms")
    public ResponseEntity<List<MeetingRoom>> getAvailabilityRooms(
            @RequestParam int numberOfPersons,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<MeetingRoom> availableRooms = meetingRoomService.getAvailabilityRooms(numberOfPersons, new Interval(startTime, endTime));
        return ResponseEntity.ok(availableRooms);
    }

    @GetMapping("/quick-available-room")
    public ResponseEntity<MeetingRoom> getQuickAvailableRoom(
            @RequestParam int numberOfPersons,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        Interval interval = new Interval(startTime, endTime);
        MeetingRoom availableRoom = meetingRoomService.getAvailableRoom_Quick(numberOfPersons, interval);

        if (availableRoom != null) {
            return ResponseEntity.ok(availableRoom);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
