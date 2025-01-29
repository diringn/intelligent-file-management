package com.example.intelligent_file_management.service;

import com.example.intelligent_file_management.model.Rule;
import com.example.intelligent_file_management.repository.RuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileAutomationService {

    private final RuleRepository ruleRepository;

    public void applyAutomation(String filePath) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            log.warn("Файл не найден: {}", filePath);
            return;
        }

        String extension = getFileExtension(path.getFileName().toString());

        List<Rule> rules = ruleRepository.findByFileExtension(extension);
        if (rules.isEmpty()) {
            log.debug("Нет правил для расширения: {}", extension);
            return;
        }

        for (Rule rule : rules) {
            try {
                switch (rule.getOperationType()) {
                    case MOVE -> moveFile(path, rule.getTargetDirectory());
                    case RENAME -> renameFile(path, rule.getRenamePattern());
                    case CONVERT -> convertFile(path, rule.getTargetDirectory());
                    default -> log.debug("Для файла {} не задана операция (NONE).", filePath);
                }
            } catch (IOException e) {
                log.error("Ошибка при обработке файла {} по правилу {}: {}", filePath, rule.getName(), e.getMessage());
            }
        }
    }

    private String getFileExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index == -1) {
            return "";
        }
        return fileName.substring(index + 1).toLowerCase();
    }

    private void moveFile(Path source, String targetDirectory) throws IOException {
        if (targetDirectory == null || targetDirectory.isBlank()) {
            log.warn("Не указана целевая директория для операции MOVE.");
            return;
        }
        Path targetDir = Paths.get(targetDirectory);
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }
        Path targetPath = targetDir.resolve(source.getFileName());
        Files.move(source, targetPath, StandardCopyOption.REPLACE_EXISTING);
        log.info("Файл {} перемещён в {}", source, targetPath);
    }

    private void renameFile(Path source, String pattern) throws IOException {
        if (pattern == null || pattern.isBlank()) {
            log.warn("Не указан renamePattern для операции RENAME.");
            return;
        }

        String originalName = source.getFileName().toString();
        String baseName = originalName.contains(".")
                ? originalName.substring(0, originalName.lastIndexOf('.'))
                : originalName;
        String extension = getFileExtension(originalName);

        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        String newName = pattern
                .replace("{originalName}", baseName)
                .replace("{date}", dateStr);

        if (!extension.isEmpty()) {
            newName += "." + extension;
        }

        Path targetPath = source.resolveSibling(newName);
        Files.move(source, targetPath, StandardCopyOption.REPLACE_EXISTING);
        log.info("Файл {} переименован в {}", source, targetPath);
    }

    private void convertFile(Path source, String targetDirectory) throws IOException {
        if (targetDirectory == null || targetDirectory.isBlank()) {
            log.warn("Не указана целевая директория для операции CONVERT.");
            return;
        }
        Path targetDir = Paths.get(targetDirectory);
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        String extension = getFileExtension(source.getFileName().toString());
        if (!extension.equals("png")) {
            log.warn("Данная конвертация поддерживает только PNG -> JPG (демо). Расширение: {}", extension);
            return;
        }

        BufferedImage inputImage = ImageIO.read(source.toFile());
        if (inputImage == null) {
            log.warn("ImageIO не смог прочитать файл как изображение: {}", source);
            return;
        }

        String baseName = source.getFileName().toString()
                .replaceAll("\\.png$", "")
                .replaceAll("\\.PNG$", "");
        Path targetPath = targetDir.resolve(baseName + ".jpg");

        boolean result = ImageIO.write(inputImage, "jpg", targetPath.toFile());
        if (!result) {
            log.warn("ImageIO не смог сконвертировать PNG в JPG: {}", source);
            return;
        }

        log.info("Файл {} успешно сконвертирован в {}", source, targetPath);
    }
}
