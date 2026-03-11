package com.smtech.SM_Caterer.API;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping({"/health", "/api/v1/health"})
    public String health() {
        return "Application is UP!";
    }
}
