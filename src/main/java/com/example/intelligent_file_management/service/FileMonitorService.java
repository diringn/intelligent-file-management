package com.example.intelligent_file_management.service;

import com.example.intelligent_file_management.model.FileLog;
import com.example.intelligent_file_management.repository.FileLogRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileMonitorService {

    private final FileLogRepository fileLogRepository;
    private final FileAutomationService fileAutomationService;

    private WatchService watchService;
    private Thread watchThread;
    private final Map<WatchKey, Path> keyPathMap = new HashMap<>();

    @Value("${file.monitor.path}")
    private String directoryToWatch;

    @PostConstruct
    public void init() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            Path startPath = Paths.get(directoryToWatch);

            if (!Files.exists(startPath) || !Files.isDirectory(startPath)) {
                log.error("Директория для мониторинга не существует или не является директорией: {}", directoryToWatch);
                return;
            }

            registerAll(startPath);

            watchThread = new Thread(this::processEvents);
            watchThread.start();

            log.info("FileMonitorService запущен и отслеживает директорию: {}", directoryToWatch);
        } catch (IOException e) {
            log.error("Ошибка при инициализации FileMonitorService: ", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        try {
            watchService.close();
            if (watchThread != null) {
                watchThread.interrupt();
            }
            log.info("FileMonitorService остановлен.");
        } catch (IOException e) {
            log.error("Ошибка при остановке FileMonitorService: ", e);
        }
    }

    private void registerAll(Path start) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                registerDir(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void registerDir(Path dir) throws IOException {
        WatchKey key = dir.register(
                watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY
        );
        keyPathMap.put(key, dir);
        log.debug("Зарегистрирована директория: {}", dir);
    }

    private void processEvents() {
        while (true) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException e) {
                log.info("Поток мониторинга прерван.");
                return;
            } catch (ClosedWatchServiceException e) {
                log.info("WatchService закрыт.");
                return;
            }

            Path dir = keyPathMap.get(key);
            if (dir == null) {
                log.warn("WatchKey не распознан: {}", key);
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }

                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path name = ev.context();
                Path child = dir.resolve(name);

                log.info("Событие {} для файла {}", kind.name(), child);

                FileLog fileLog = FileLog.builder()
                        .filename(child.toString())
                        .action(kind.name())
                        .timestamp(LocalDateTime.now())
                        .details("Событие " + kind.name() + " для файла: " + child)
                        .build();
                fileLogRepository.save(fileLog);

                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    try {
                        if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                            registerAll(child);
                        } else {
                            fileAutomationService.applyAutomation(child.toString());
                        }
                    } catch (IOException e) {
                        log.error("Ошибка при регистрации новой директории: {}", child, e);
                    }
                } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                    fileAutomationService.applyAutomation(child.toString());
                }
            }

            boolean valid = key.reset();
            if (!valid) {
                keyPathMap.remove(key);
                if (keyPathMap.isEmpty()) {
                    break;
                }
            }
        }
    }
}
