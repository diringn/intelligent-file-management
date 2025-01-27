package com.example.intelligent_file_management.repository;

import com.example.intelligent_file_management.model.FileLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileLogRepository extends JpaRepository<FileLog, Long> {
}
