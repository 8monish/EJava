package com.monish.springbootapp.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.monish.springbootapp.User;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @GetMapping("/{userId}")
    public User getById(@PathVariable String userId) {
        return new User(userId, "Monish", "jade", "monish.jade@example.com");
    }
}
