package com.ncbachhhh.LTUDM.controller;

import com.ncbachhhh.LTUDM.dto.request.UserCreationRequest;
import com.ncbachhhh.LTUDM.entity.Users;
import com.ncbachhhh.LTUDM.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/users/create")
    Users createUser(@RequestBody UserCreationRequest request) {
        return userService.createUser(request);
    }
}
