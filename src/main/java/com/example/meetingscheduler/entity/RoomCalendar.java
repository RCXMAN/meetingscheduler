package com.example.meetingscheduler.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SortComparator;

import java.time.LocalDate;
import java.util.SortedSet;
import java.util.TreeSet;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomCalendar {
    @Id
    @GeneratedValue
    private Long calendarId;
    private LocalDate date;

    @OneToMany(mappedBy = "roomCalendar")
    @SortComparator(IntervalComparator.class)
    private SortedSet<Meeting> meetings = new TreeSet<>();

    @ManyToOne(cascade = CascadeType.PERSIST)
    private MeetingRoom meetingRoom;

    public RoomCalendar(MeetingRoom room, LocalDate bookingDate) {
        this.meetingRoom = room;
        this.date = bookingDate;
    }
}
