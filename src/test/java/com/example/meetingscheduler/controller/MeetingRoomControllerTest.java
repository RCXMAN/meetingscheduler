package com.example.meetingscheduler.controller;

import com.example.meetingscheduler.entity.Interval;
import com.example.meetingscheduler.entity.MeetingRoom;
import com.example.meetingscheduler.service.MeetingRoomService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MeetingRoomController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(SpringExtension.class)
class MeetingRoomControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private MeetingRoomService meetingRoomService;

    @Test
    void getAllRooms() throws Exception {
        List<MeetingRoom> rooms = new ArrayList<>(List.of(new MeetingRoom(), new MeetingRoom()));

        given(meetingRoomService.getAllRooms()).willReturn(rooms);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/scheduler/rooms")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(rooms.size()));
    }

    @Test
    void getMeetingRoomById() throws Exception {
        Long roomId = 1L;
        MeetingRoom room = new MeetingRoom();
        room.setRoomId(roomId);

        given(meetingRoomService.getMeetingRoomById(roomId)).willReturn(room);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/scheduler/rooms/{roomId}", roomId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value(roomId));
    }

    @Test
    void createBookingRoom() throws Exception {
        int capacity = 10;
        boolean isAvailable = true;
        MeetingRoom createdRoom = new MeetingRoom(); // Prepare your created MeetingRoom object
        createdRoom.setCapacity(10);
        createdRoom.setAvailable(isAvailable);

        given(meetingRoomService.createNewMeetingRoom(capacity, isAvailable)).willReturn(createdRoom);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/scheduler/rooms/create-room")
                        .param("capacity", String.valueOf(capacity))
                        .param("isAvailable", String.valueOf(isAvailable))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capacity").value(capacity))
                .andExpect(jsonPath("$.available").value(isAvailable));
    }

    @Test
    void updateRoomAvailability() throws Exception {
        Long roomId = 1L;
        boolean available = true;
        MeetingRoom updatedRoom = new MeetingRoom();
        updatedRoom.setRoomId(roomId);
        updatedRoom.setAvailable(available);

        when(meetingRoomService.updateRoomAvailability(roomId, available)).thenReturn(updatedRoom);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/scheduler/rooms/update-availability/{roomId}", roomId)
                        .param("available", String.valueOf(available))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(updatedRoom.isAvailable()));
    }

    @Test
    void getAvailabilityRooms() throws Exception {
        int numberOfPersons = 5;
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(2);
        List<MeetingRoom> availableRooms = new ArrayList<>(List.of(new MeetingRoom(), new MeetingRoom()));

        when(meetingRoomService.getAvailabilityRooms(
                numberOfPersons, new Interval(startTime, endTime))).thenReturn(availableRooms);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/scheduler/rooms/available-rooms")
                        .param("numberOfPersons", String.valueOf(numberOfPersons))
                        .param("startTime", startTime.toString())
                        .param("endTime", endTime.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(availableRooms.size()));
    }

    @Test
    void getQuickAvailableRoom() throws Exception {
        int numberOfPersons = 5;
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(2);
        MeetingRoom availableRoom = new MeetingRoom();
        availableRoom.setCapacity(numberOfPersons);

        when(meetingRoomService.getAvailableRoom_Quick(
                numberOfPersons, new Interval(startTime, endTime))).thenReturn(availableRoom);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/scheduler/rooms/quick-available-room")
                        .param("numberOfPersons", String.valueOf(numberOfPersons))
                        .param("startTime", startTime.toString())
                        .param("endTime", endTime.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capacity").value(availableRoom.getCapacity()));
    }

    @Test
    void getQuickAvailable_NonRoom() throws Exception {
        int numberOfPersons = 5;
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(2);

        given(meetingRoomService.getAvailableRoom_Quick(
                numberOfPersons, new Interval(startTime, endTime))).willReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/scheduler/rooms/quick-available-room")
                        .param("numberOfPersons", String.valueOf(numberOfPersons))
                        .param("startTime", startTime.toString())
                        .param("endTime", endTime.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}