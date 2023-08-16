package com.example.meetingscheduler.service.Impl;

import com.example.meetingscheduler.entity.User;
import com.example.meetingscheduler.exception.NoSuchSourceException;
import com.example.meetingscheduler.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getUserByUsername() {
        String username = "testUser";
        User user = new User();
        user.setUsername(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        User resultUser = userService.getUserByUsername(username);

        assertEquals(user, resultUser);
    }

    @Test
    void getUserByUsername_NonExistingUser() {
        String nonExistingUsername = "nonExistingUsername";
        when(userRepository.findByUsername(nonExistingUsername)).thenReturn(Optional.empty());

        assertThrows(NoSuchSourceException.class, () -> userService.getUserByUsername(nonExistingUsername));
    }

    @Test
    void getUsersByIds() {
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);
        List<User> userList = new ArrayList<>(List.of(user1, user2));
        List<Long> idList = new ArrayList<>(List.of(1L, 2L));

        when(userRepository.findAllById(idList)).thenReturn(userList);

        assertEquals(userList, userService.getUsersByIds(idList));
    }

    @Test
    void respondInvitation() {

    }
}