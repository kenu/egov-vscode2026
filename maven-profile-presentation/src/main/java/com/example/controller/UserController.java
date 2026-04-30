package com.example.controller;

import com.example.domain.User;
import com.example.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    private final UserMapper userMapper;

    @Value("${spring.profiles.active:unknown}")
    private String activeProfile;

    public UserController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @GetMapping("/users")
    public List<User> getUsers() {
        return userMapper.findAll();
    }

    @GetMapping("/profile")
    public String getProfile() {
        return "Active Profile: " + activeProfile;
    }
}
