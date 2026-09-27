package com.example.runnerz.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRestClient userRestClient;

    public UserController(UserRestClient userRestClient) {
        this.userRestClient = userRestClient;
    }

    @GetMapping("")
    List<User> findAll() {
        return userRestClient.findAll();
    }

    @GetMapping("/{id}")
    User findById(@PathVariable Integer id) {
        return userRestClient.findById(id);
    }
}
