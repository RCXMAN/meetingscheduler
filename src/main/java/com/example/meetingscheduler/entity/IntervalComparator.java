package com.example.meetingscheduler.entity;

import java.util.Comparator;

public class IntervalComparator implements Comparator<Meeting> {
    @Override
    public int compare(Meeting meeting1, Meeting meeting2) {
        return meeting1.getInterval().getStartTime().compareTo(meeting2.getInterval().getStartTime());
    }
}
