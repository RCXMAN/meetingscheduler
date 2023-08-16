package com.example.meetingscheduler.repository;

import com.example.meetingscheduler.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase
class UserRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsername() {
        User user = new User();
        user.setUsername("user");
        entityManager.persist(user);

        User test_user = userRepository.findByUsername("user")
                .orElse(null);

        assertEquals(test_user, user);
    }
}