package com.monish.springbootapp.Controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @GetMapping("/getbyid/{userId}")
    public User getById(@PathVariable String userId) {
        return new User(userId, "Monish", "jade", "monish.jade@example.com");
    }

    @GetMapping("/all")
    public List<User> getAll() {
        return this.userRepository.findAll();
    }

    @PostMapping("/postcheck")
    public User postcheck(@RequestBody User entity) {
        return this.userRepository.save(entity);
    }

    @PutMapping("/update/{userId}")
    public User update(@PathVariable String userId, @RequestBody User entity) {
        entity.setUserId(userId);
        return this.userRepository.save(entity);
    }

    @DeleteMapping("/delete/{userId}")
    public void delete(@PathVariable String userId) {
        this.userRepository.deleteById(userId);
    }
    
}
