package com.example.meetingscheduler.controller;

import com.example.meetingscheduler.controller.request.CancelMeetingRequest;
import com.example.meetingscheduler.controller.request.NewMeetingRequest;
import com.example.meetingscheduler.entity.Meeting;
import com.example.meetingscheduler.service.MeetingService;
import com.example.meetingscheduler.utils.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.SortedSet;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/scheduler/meetings")
public class MeetingSchedulerController {
    private final MeetingService meetingSchedulerService;

    @PostMapping("/schedule-meeting")
    public ResponseEntity<Boolean> scheduleMeeting(@RequestBody NewMeetingRequest newMeetingRequest) {
        boolean isSuccess = meetingSchedulerService.scheduleMeeting(
                newMeetingRequest.getAttendeesId(),
                newMeetingRequest.getInterval(),
                newMeetingRequest.getContent());

        return ResponseEntity.ok(isSuccess);
    }

    @PostMapping("/schedule-meeting/{roomId}")
    public ResponseEntity<Boolean> ScheduleMeetingInDesignatedRoom(
            @RequestBody NewMeetingRequest newMeetingRequest,
            @PathVariable Long roomId) {
        boolean isSuccess = meetingSchedulerService.scheduleMeeting_withRoom(
                roomId,
                newMeetingRequest.getAttendeesId(),
                newMeetingRequest.getInterval(),
                newMeetingRequest.getContent());

        return ResponseEntity.ok(isSuccess);
    }

    @PutMapping("/decline-meeting")
    public ResponseEntity<Void> declineInvite(
            @RequestParam(value = "user") String username,
            @RequestParam(value = "meeting") Long meetingId) {
        if (!Utils.getCurrentUsername().equals(username)) {
            return ResponseEntity.badRequest().build();
        }

        meetingSchedulerService.respondInvitation(meetingId, username);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/cancel-meeting")
    public ResponseEntity<Void> cancelMeeting(@RequestBody CancelMeetingRequest cancelMeetingRequest) {
        boolean isSuccess = meetingSchedulerService.cancelMeetingByCreator(
                cancelMeetingRequest.getCreatorUsername(),
                cancelMeetingRequest.getMeetingId(),
                cancelMeetingRequest.getContent()
        );

        if (isSuccess) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/cancel-maintenance-meeting")
    public ResponseEntity<Void> cancelMaintenanceMeeting(
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        meetingSchedulerService.cancelMeetingDueMaintenance(roomId, startTime, endTime);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user-meetingsByDay")
    public ResponseEntity<SortedSet<Meeting>> getMeetingsOfUserByDay(
            @RequestParam String username,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        SortedSet<Meeting> meetings = meetingSchedulerService.getMeetingsOfUserByDay(username, date);
        return ResponseEntity.ok(meetings);
    }

    @GetMapping("/user-meetingsByWeek")
    public ResponseEntity<Map<LocalDate, SortedSet<Meeting>>> getMeetingsOfUserByWeek(
            @RequestParam String username,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Map<LocalDate, SortedSet<Meeting>> meetings = meetingSchedulerService.getMeetingsOfUserByWeek(username, date);
        return ResponseEntity.ok(meetings);
    }

    @GetMapping("/user-meetingsByMonth")
    public ResponseEntity<Map<LocalDate, SortedSet<Meeting>>> getMeetingsOfUserByMonth(
            @RequestParam String username,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Map<LocalDate, SortedSet<Meeting>> meetings = meetingSchedulerService.getMeetingsOfUserByMonth(username, date);
        return ResponseEntity.ok(meetings);
    }

    @GetMapping("/room-meetingsByDay")
    public ResponseEntity<SortedSet<Meeting>> getRoomMeetingsByDay(
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        SortedSet<Meeting> meetings = meetingSchedulerService.getRoomMeetingsByDay(roomId, date);
        return ResponseEntity.ok(meetings);
    }

    @GetMapping("/room-meetingsByWeek")
    public ResponseEntity<Map<LocalDate, SortedSet<Meeting>>> getRoomMeetingsByWeek(
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Map<LocalDate, SortedSet<Meeting>> meetings = meetingSchedulerService.getRoomMeetingsByWeekOfDay(roomId, date);
        return ResponseEntity.ok(meetings);
    }

    @GetMapping("/room-meetingsByMonth")
    public ResponseEntity<Map<LocalDate, SortedSet<Meeting>>> getRoomMeetingsByMonth(
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Map<LocalDate, SortedSet<Meeting>> meetings = meetingSchedulerService.getRoomMeetingsByMonthOfDay(roomId, date);
        return ResponseEntity.ok(meetings);
    }
}
