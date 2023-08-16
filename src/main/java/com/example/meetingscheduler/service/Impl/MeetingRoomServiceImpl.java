package com.example.meetingscheduler.service.Impl;

import com.example.meetingscheduler.MeetingSchedulerConstant;
import com.example.meetingscheduler.entity.*;
import com.example.meetingscheduler.exception.ExceedCapacity;
import com.example.meetingscheduler.exception.NoSuchSourceException;
import com.example.meetingscheduler.repository.MeetingRepository;
import com.example.meetingscheduler.repository.MeetingRoomRepository;
import com.example.meetingscheduler.service.MeetingRoomService;
import com.example.meetingscheduler.service.RoomCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import static com.example.meetingscheduler.MeetingSchedulerConstant.CLOSED_TIME;
import static com.example.meetingscheduler.MeetingSchedulerConstant.STARTED_TIME;


@Service
@RequiredArgsConstructor
public class MeetingRoomServiceImpl implements MeetingRoomService {
    private final MeetingRoomRepository meetingRoomRepository;
    private final RoomCalendarService roomCalendarService;
    private final MeetingRepository meetingRepository;

    @Override
    public List<MeetingRoom> getAllRooms() {
        return meetingRoomRepository.findAll();
    }

    @Override
    public MeetingRoom getMeetingRoomById(Long roomId) {
        return meetingRoomRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchSourceException());
    }

    @Override
    public MeetingRoom getMeetingRoomByMeeting(Meeting meeting) {
        return meetingRoomRepository.findByBookedMeetingMeetings(meeting)
                .orElseThrow(() -> new NoSuchSourceException());
    }

    @Override
    public MeetingRoom createNewMeetingRoom(int capacity, boolean isAvailable) {
        MeetingRoom newRoom = new MeetingRoom();
        newRoom.setCapacity(capacity);
        newRoom.setAvailable(isAvailable);

        return meetingRoomRepository.save(newRoom);
    }

    @Override
    public MeetingRoom updateRoomAvailability(Long roomId, boolean available) {
        MeetingRoom room = meetingRoomRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchSourceException());

        if(room.isAvailable() == available) {
            return room;
        }

        room.setAvailable(available);
        return meetingRoomRepository.save(room);
    }

    @Override
    public List<MeetingRoom> getAvailabilityRooms(int numberOfPersons, Interval interval) {
        List<MeetingRoom> rooms = meetingRoomRepository
                .findByCapacityGreaterThanEqualAndIsAvailableIsTrueOrderByCapacityAsc(numberOfPersons)
                .orElse(new ArrayList<>());

        List<MeetingRoom> result = new ArrayList<>();

        for (MeetingRoom room : rooms) {
            if (isRoomAvailable(room, interval)) {
                result.add(room);
            }
        }

        return result;
    }

    @Override
    public MeetingRoom getAvailableRoom_Quick(int numberOfPersons, Interval interval) {
        List<MeetingRoom> rooms = meetingRoomRepository
                .findByCapacityGreaterThanEqualAndIsAvailableIsTrueOrderByCapacityAsc(numberOfPersons)
                .orElse(new ArrayList<>());

        return findAvailableRoom(rooms, interval);
    }

    @Override
    public MeetingRoom checkRoomAvailability(Long roomId, int numberOfPersons, Interval interval) {
        MeetingRoom room = meetingRoomRepository
                .findById(roomId)
                .orElseThrow(() -> new NoSuchSourceException());

        if (numberOfPersons > room.getCapacity()) {
            throw new ExceedCapacity();
        }

        if (isRoomAvailable(room, interval)) {
            return room;
        }

        return null;
    }

    private boolean isRoomAvailable(MeetingRoom room, Interval interval) {
        LocalDate meetingDate = interval.getStartTime().toLocalDate();

        RoomCalendar roomCalendar = roomCalendarService.getRoomCalenderByRoomAndDate(room, meetingDate);
        SortedSet<Meeting> meetings = roomCalendar.getMeetings();

        LocalTime intervalStartTime = interval.getStartTime().toLocalTime();
        LocalTime intervalEndTime = interval.getEndTime().toLocalTime();

        return isWithinWorkingHours(intervalStartTime) && isWithinWorkingHours(intervalEndTime) &&
                isIntervalAvailable(interval, meetings);
    }

    private MeetingRoom findAvailableRoom(List<MeetingRoom> rooms, Interval interval) {
        for (MeetingRoom room : rooms) {
            LocalDate meetingDate = interval.getStartTime().toLocalDate();

            RoomCalendar roomCalendar = roomCalendarService.getRoomCalenderByRoomAndDate(room, meetingDate);
            SortedSet<Meeting> meetings = roomCalendar.getMeetings();

            LocalTime intervalStartTime = interval.getStartTime().toLocalTime();
            LocalTime intervalEndTime = interval.getEndTime().toLocalTime();

            if (!isWithinWorkingHours(intervalStartTime) || !isWithinWorkingHours(intervalEndTime)) {
                continue;
            }

            boolean isAvailable = isIntervalAvailable(interval, meetings);
            if (isAvailable) {
                return room;
            }
        }
        return null;
    }


    private boolean isWithinWorkingHours(LocalTime time) {
        return !time.isBefore(STARTED_TIME) &&
                !time.isAfter(MeetingSchedulerConstant.CLOSED_TIME);
    }

    private boolean isIntervalAvailable(Interval interval, SortedSet<Meeting> meetings) {
        if (meetings.isEmpty()) {
            return true;
        }

        Meeting firstMeeting = meetings.first();
        if (isGapBefore(firstMeeting.getInterval(), interval)) {
            return true;
        }

        Meeting lastMeeting = meetings.last();
        if (isGapAfter(lastMeeting.getInterval(), interval)) {
            return true;
        }

        Iterator<Meeting> iterator = meetings.iterator();
        Meeting currentMeeting = iterator.next();
        while (iterator.hasNext()) {
            Meeting nextMeeting = iterator.next();
            if (isGapBetween(currentMeeting.getInterval(), nextMeeting.getInterval(), interval)) {
                return true;
            }
            currentMeeting = nextMeeting;
        }

        return false;
    }

    private boolean isGapBefore(Interval existingInterval, Interval interval) {
        return LocalDate.now().atTime(STARTED_TIME).isBefore(interval.getStartTime())
                && existingInterval.getStartTime().isAfter(interval.getEndTime());
    }

    private boolean isGapAfter(Interval existingInterval, Interval interval) {
        return existingInterval.getEndTime().isBefore(interval.getStartTime())
                && LocalDate.now().atTime(CLOSED_TIME).isAfter(interval.getEndTime());
    }

    private boolean isGapBetween(Interval beforeInterval, Interval existingInterval, Interval interval) {
        return beforeInterval.getEndTime().isBefore(interval.getStartTime())
                && existingInterval.getStartTime().isAfter(interval.getEndTime());
    }

}
