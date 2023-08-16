package com.example.meetingscheduler.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "scheduler_user")
public class User {
    @Id
    @GeneratedValue
    private Long id;

    private String username;

    private String personal_name;

    private String email;

    @ManyToMany(fetch = FetchType.EAGER)
    List<Meeting> meetings;
}
