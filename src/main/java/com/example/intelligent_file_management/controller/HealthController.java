// src/main/java/com/example/filemanagement/controller/HealthController.java
package com.example.intelligent_file_management.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Intelligent File Management System is up and running!");
    }
}
