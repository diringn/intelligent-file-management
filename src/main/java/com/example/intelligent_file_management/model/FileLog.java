package com.example.intelligent_file_management.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;
    private String action;
    private LocalDateTime timestamp;
    private String details;
}
