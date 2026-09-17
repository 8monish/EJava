package com.monish.springbootapp.Controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.monish.springbootapp.User;
import com.monish.springbootapp.repository.UserRepository;


@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/{userId}")
    public User getById(@PathVariable String userId) {
        return new User(userId, "Monish", "jade", "monish.jade@example.com");
    }

    @GetMapping("/all")
    public List<User> getAll() {
        return this.userRepository.findAll();
    }
}
