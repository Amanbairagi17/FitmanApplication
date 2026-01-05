package com.fitness.fitman.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.PublicKey;

@RestController
@RequestMapping("/home")
public class RootController {

    @GetMapping
    public String home(){
        return "Home page view ";
    }
}
