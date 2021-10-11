package com.example.serverdatabase;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

@RestController
public class Responder implements WebMvcConfigurer {

    /*
    Gets the status of specified device
    Can later be written using GSON to easier parse the request
    Curl Request = curl -X POST http://localhost:8080/getDeviceStatus -H "Content-Type: application/json" -d '{"deviceName":"KitchenLight"}'
     */
    @RequestMapping(value = "/getDeviceStatus", method = RequestMethod.POST, headers = "Accept=*/*", produces = "application/json", consumes = "application/json")
    public String postResponse(@RequestBody String keyword) {
        return keyword + " Is off"; // Will later tell status of the device
    }

    @GetMapping("/testAPI")
    public String welcomePage() {
        return "API is working!";
    }
}


