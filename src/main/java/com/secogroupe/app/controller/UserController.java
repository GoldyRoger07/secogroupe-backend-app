package com.secogroupe.app.controller;

// import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.secogroupe.app.entity.User;

@RestController
@RequestMapping("/api")
public class UserController {

    @GetMapping("/v1/users")
    public List<User> getUsers(){
        return List.of();
    }

    @GetMapping("/v1/users/{id}")
    public List<User> getUsers(@PathVariable("id") int id){
        return List.of();
    }

    @PostMapping("/v1/users")
    public ResponseEntity<?> createUser(){
        return null;
    }

    @PutMapping("/v1/users")
    public ResponseEntity<?> updateUser(){
        return null;
    }

    @DeleteMapping("/v1/users")
    public ResponseEntity<?> deleteUser(){
        return null;
    }
    
}
