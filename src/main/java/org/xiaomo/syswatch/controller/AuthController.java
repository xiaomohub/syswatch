package org.xiaomo.syswatch.controller;

import lombok.Data;
import org.springframework.web.bind.annotation.*;
import org.xiaomo.syswatch.service.UserService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginReq req) {
        return userService.login(req.getUsername(), req.getPassword());
    }

    @Data
    public static class LoginReq {
        private String username;
        private String password;
    }
}
