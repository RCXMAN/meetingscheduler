package com.example.meetingscheduler.service.Impl;

import com.example.meetingscheduler.entity.User;
import com.example.meetingscheduler.exception.NoSuchSourceException;
import com.example.meetingscheduler.repository.UserRepository;
import com.example.meetingscheduler.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(NoSuchSourceException::new);
    }

    @Override
    public List<User> getUsersByIds(List<Long> ids) {
        return userRepository.findAllById(ids);
    }
}
