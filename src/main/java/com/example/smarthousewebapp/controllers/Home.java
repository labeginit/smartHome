package com.example.smarthousewebapp.controllers;

import com.example.smarthousewebapp.models.Lamp;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Home {

    @GetMapping("/home")
    String home(Model model){
        model.addAttribute("Lamp", new Lamp());

        return "home";
    }
}
