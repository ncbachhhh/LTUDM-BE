package com.ncbachhhh.LTUDM.controller;

import com.ncbachhhh.LTUDM.dto.request.UserRegisterRequest;
import com.ncbachhhh.LTUDM.entity.User.User;
import com.ncbachhhh.LTUDM.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/auth/register")
    User createUser(@RequestBody @Valid UserRegisterRequest request) {
        return userService.createUser(request);
    }
}
