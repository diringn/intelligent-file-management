// src/main/java/com/example/filemanagement/controller/LogController.java
package com.example.intelligent_file_management.controller;

import com.example.intelligent_file_management.model.FileLog;
import com.example.intelligent_file_management.repository.FileLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

    private final FileLogRepository fileLogRepository;

    @GetMapping
    public ResponseEntity<List<FileLog>> getAllLogs() {
        List<FileLog> logs = fileLogRepository.findAll();
        return ResponseEntity.ok(logs);
    }
}
