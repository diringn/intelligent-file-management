// src/main/java/com/example/filemanagement/model/Rule.java
package com.example.intelligent_file_management.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
}
