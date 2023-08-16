package com.example.meetingscheduler.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Meeting implements Comparable<Meeting> {
    @Id
    @GeneratedValue
    private Long id;
    @Embedded
    private Interval interval;

    @ManyToOne
    @JsonIgnore
    private User creator;

    @ManyToMany
    @JsonIgnore
    private List<User> participants = new ArrayList<>();

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JsonIgnore
    private RoomCalendar roomCalendar;

    @Override
    public int compareTo(Meeting meeting) {
        return interval.getStartTime().compareTo(meeting.getInterval().getStartTime());
    }
}
