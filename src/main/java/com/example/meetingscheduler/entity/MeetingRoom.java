package com.example.meetingscheduler.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MeetingRoom {
    @Id
    @GeneratedValue
    private Long roomId;

    private int capacity;

    @OneToMany(mappedBy = "meetingRoom", cascade = CascadeType.PERSIST)
    @MapKey(name = "date")
    @JsonIgnore
    private Map<LocalDate, RoomCalendar> bookedMeeting = new HashMap<>();

    private boolean isAvailable;
}
