package com.example.intelligent_file_management;

import com.example.intelligent_file_management.model.FileLog;
import com.example.intelligent_file_management.repository.FileLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LogControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private FileLogRepository fileLogRepository;

    @BeforeEach
    public void setup() {
        fileLogRepository.deleteAll();

        FileLog log1 = FileLog.builder()
                .filename("C:/path/to/watch/file1.txt")
                .action("CREATED")
                .timestamp(LocalDateTime.now())
                .details("Событие CREATED")
                .build();

        FileLog log2 = FileLog.builder()
                .filename("C:/path/to/watch/file2.txt")
                .action("MODIFIED")
                .timestamp(LocalDateTime.now())
                .details("Событие MODIFIED")
                .build();

        fileLogRepository.save(log1);
        fileLogRepository.save(log2);
    }

    @Test
    void testGetAllLogs() {
        ResponseEntity<FileLog[]> response = restTemplate.getForEntity("/api/logs", FileLog[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        FileLog[] logs = response.getBody();
        assertThat(logs).isNotNull();
        assertThat(logs.length).isEqualTo(2);
    }
}
