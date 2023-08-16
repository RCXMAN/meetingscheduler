package com.example.meetingscheduler.controller;

import com.example.meetingscheduler.controller.request.CancelMeetingRequest;
import com.example.meetingscheduler.controller.request.NewMeetingRequest;
import com.example.meetingscheduler.entity.Interval;
import com.example.meetingscheduler.entity.Meeting;
import com.example.meetingscheduler.service.MeetingService;
import com.example.meetingscheduler.utils.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MeetingSchedulerController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(SpringExtension.class)
class MeetingSchedulerControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private MeetingService meetingService;

    @Test
    void scheduleMeeting() throws Exception {
        List<Long> idList = new ArrayList<>(List.of(1L, 2L));
        Interval interval = new Interval(LocalDateTime.now(), LocalDateTime.now().plusHours(2));
        String content = "Meeting content";

        NewMeetingRequest newMeetingRequest = new NewMeetingRequest();
        newMeetingRequest.setAttendeesId(idList);
        newMeetingRequest.setInterval(interval);
        newMeetingRequest.setContent(content);

        given(meetingService.scheduleMeeting(idList, interval, content)).willReturn(true);

        mockMvc.perform(post("/api/scheduler/meetings/schedule-meeting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newMeetingRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void scheduleMeetingInDesignatedRoom() throws Exception {
        Long roomId = 100L;
        List<Long> idList = new ArrayList<>(List.of(1L, 2L));
        Interval interval = new Interval(LocalDateTime.now(), LocalDateTime.now().plusHours(2));
        String content = "Meeting content";

        NewMeetingRequest newMeetingRequest = new NewMeetingRequest();
        newMeetingRequest.setAttendeesId(idList);
        newMeetingRequest.setInterval(interval);
        newMeetingRequest.setContent(content);

        given(meetingService.scheduleMeeting_withRoom(roomId, idList, interval, content)).willReturn(true);

        mockMvc.perform(post("/api/scheduler/meetings/schedule-meeting/{roomId}", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newMeetingRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user1")
    public void declineInvite() throws Exception {
        String username = "user1";
        Long meetingId = 1L;

        mockMvc.perform(put("/api/scheduler/meetings/decline-meeting")
                .param("user", username)
                .param("meeting", String.valueOf(meetingId))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user1")
    public void declineInvite_InvalidUsername() throws Exception {
        String username = "invalidUsername";
        Long meetingId = 1L;

        mockMvc.perform(put("/api/scheduler/meetings/decline-meeting")
                        .param("user", username)
                        .param("meeting", String.valueOf(meetingId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelMeeting() throws Exception {
        String creatorUsername = "creator";
        Long meetingId = 100L;
        String content = "Cancel Meeting";
        CancelMeetingRequest cancelMeetingRequest = new CancelMeetingRequest();
        cancelMeetingRequest.setCreatorUsername(creatorUsername);
        cancelMeetingRequest.setMeetingId(meetingId);
        cancelMeetingRequest.setContent(content);

        given(meetingService.cancelMeetingByCreator(creatorUsername, meetingId, content)).willReturn(true);

        mockMvc.perform(delete("/api/scheduler/meetings/cancel-meeting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelMeetingRequest)))
                .andExpect(status().isNoContent());
    }

    @Test
    void cancelMaintenanceMeeting() throws Exception {
        Long roomId = 123L;
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(2);

        mockMvc.perform(delete("/api/scheduler/meetings/cancel-maintenance-meeting")
                        .param("roomId", String.valueOf(roomId))
                        .param("startTime", String.valueOf(startTime))
                        .param("endTime", String.valueOf(endTime))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void getMeetingsOfUserByDay() throws Exception {
        String username = "testUser";
        LocalDate date = LocalDate.now();

        SortedSet<Meeting> meetings = new TreeSet<>();
        Meeting meeting1 = new Meeting();
        meeting1.setInterval(new Interval(LocalDateTime.now(), LocalDateTime.now()));
        Meeting meeting2 = new Meeting();
        meeting2.setInterval(new Interval(LocalDateTime.now(), LocalDateTime.now()));
        meetings.add(meeting1);
        meetings.add(meeting2);

        given(meetingService.getMeetingsOfUserByDay(username, date)).willReturn(meetings);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/scheduler/meetings/user-meetingsByDay")
                        .param("username", username)
                        .param("date", date.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(meetings.size()));
    }

    @Test
    void getMeetingsOfUserByWeek() throws Exception {
        String username = "testUser";
        LocalDate date = LocalDate.now();

        Map<LocalDate, SortedSet<Meeting>> meetings = new HashMap<>();
        SortedSet<Meeting> meetingSortedSet = new TreeSet<>();
        Meeting meeting1 = new Meeting();
        meeting1.setInterval(new Interval(LocalDateTime.now(), LocalDateTime.now()));
        meetingSortedSet.add(meeting1);
        meetings.put(date, meetingSortedSet);
        given(meetingService.getMeetingsOfUserByWeek(username, date)).willReturn(meetings);

        // Perform the API request
        mockMvc.perform(MockMvcRequestBuilders.get("/api/scheduler/meetings/user-meetingsByWeek")
                        .param("username", username)
                        .param("date", date.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(meetings.size()));
    }

    @Test
    void getMeetingsOfUserByMonth() throws Exception {
        String username = "testUser";
        LocalDate date = LocalDate.now();

        Map<LocalDate, SortedSet<Meeting>> meetings = new HashMap<>();

        Meeting meeting1 = new Meeting();
        meeting1.setInterval(new Interval(LocalDateTime.now(), LocalDateTime.now()));
        Meeting meeting2 = new Meeting();
        meeting2.setInterval(new Interval(LocalDateTime.now(), LocalDateTime.now()));

        SortedSet<Meeting> meetingSortedSet1 = new TreeSet<>();
        meetingSortedSet1.add(meeting1);
        meetings.put(date, meetingSortedSet1);

        SortedSet<Meeting> meetingSortedSet2 = new TreeSet<>();
        meetingSortedSet2.add(meeting2);

        meetings.put(date.plusDays(1),  meetingSortedSet2);
        meetings.put(date, meetingSortedSet1);
        given(meetingService.getMeetingsOfUserByMonth(username, date)).willReturn(meetings);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/scheduler/meetings/user-meetingsByMonth")
                        .param("username", username)
                        .param("date", date.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(meetings.size()));
    }

    @Test
    void getRoomMeetingsByDay() throws Exception {
        Long roomId = 123L;
        LocalDate date = LocalDate.now();

        SortedSet<Meeting> meetingSortedSet = new TreeSet<>();
        Meeting meeting1 = new Meeting();
        meeting1.setInterval(new Interval(LocalDateTime.now(), LocalDateTime.now()));
        meetingSortedSet.add(meeting1);

        given(meetingService.getRoomMeetingsByDay(roomId, date)).willReturn(meetingSortedSet);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/scheduler/meetings/room-meetingsByDay")
                        .param("roomId", roomId.toString())
                        .param("date", date.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(meetingSortedSet.size()));
    }

    @Test
    void getRoomMeetingsByWeek() throws Exception {
        Long roomId = 123L;
        LocalDate date = LocalDate.now();

        Map<LocalDate, SortedSet<Meeting>> meetings = new HashMap<>();

        SortedSet<Meeting> meetingSortedSet = new TreeSet<>();
        Meeting meeting1 = new Meeting();
        meeting1.setInterval(new Interval(LocalDateTime.now(), LocalDateTime.now()));
        meetingSortedSet.add(meeting1);
        meetings.put(date, meetingSortedSet);

        given(meetingService.getRoomMeetingsByWeekOfDay(roomId, date)).willReturn(meetings);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/scheduler/meetings/room-meetingsByWeek")
                        .param("roomId", roomId.toString())
                        .param("date", date.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(meetings.size()));
    }

    @Test
    void getRoomMeetingsByMonth() throws Exception {
        Long roomId = 123L;
        LocalDate date = LocalDate.now();

        Map<LocalDate, SortedSet<Meeting>> meetings = new HashMap<>();

        SortedSet<Meeting> meetingSortedSet1 = new TreeSet<>();
        Meeting meeting1 = new Meeting();
        meeting1.setInterval(new Interval(LocalDateTime.now(), LocalDateTime.now()));
        meetingSortedSet1.add(meeting1);

        SortedSet<Meeting> meetingSortedSet2 = new TreeSet<>();
        Meeting meeting2 = new Meeting();
        meeting2.setInterval(new Interval(LocalDateTime.now(), LocalDateTime.now()));
        meetingSortedSet2.add(meeting2);

        meetings.put(date.plusDays(1),  meetingSortedSet2);
        meetings.put(date, meetingSortedSet1);

        given(meetingService.getRoomMeetingsByMonthOfDay(roomId, date)).willReturn(meetings);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/scheduler/meetings/room-meetingsByMonth")
                        .param("roomId", roomId.toString())
                        .param("date", date.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(meetings.size()));
    }
}