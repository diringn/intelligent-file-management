// src/test/java/com/example/filemanagement/FileMonitorServiceTest.java
package com.example.intelligent_file_management;

import com.example.intelligent_file_management.service.FileMonitorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FileMonitorServiceTest {

	@Autowired
	private FileMonitorService fileMonitorService;

	@Test
	void contextLoads() {
		// Тест пройдёт, если контекст приложения загрузится без ошибок
		// и FileMonitorService корректно инициализируется
	}
}
