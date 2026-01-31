package com.ncbachhhh.LTUDM.controller;

import com.ncbachhhh.LTUDM.dto.request.UserLoginRequest;
import com.ncbachhhh.LTUDM.dto.request.UserRegisterRequest;
import com.ncbachhhh.LTUDM.dto.response.ApiResponse;
import com.ncbachhhh.LTUDM.entity.User.User;
import com.ncbachhhh.LTUDM.exception.ErrorCode;
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
    ApiResponse<User> createUser(@RequestBody @Valid UserRegisterRequest request) {
        ApiResponse<User> response = new ApiResponse<>();
        response.setData(userService.createUser(request));
        response.setCode(ErrorCode.SUCCESS.getCode());

        return response;
    }

    @PostMapping("/auth/login")
    ApiResponse<User> loginUser(@RequestBody @Valid UserLoginRequest request) {
        ApiResponse<User> response = new ApiResponse<>();
        response.setData(userService.loginUser(request));
        response.setCode(ErrorCode.SUCCESS.getCode());

        return response;
    }
}
