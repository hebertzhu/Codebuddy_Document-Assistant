package com.literature.assistant.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
public class FileUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static String generateFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid + extension;
    }

    public static String getFileExtension(String fileName) {
        if (StrUtil.isBlank(fileName)) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex);
    }

    public static String getUploadPath(String basePath) {
        String dateDir = LocalDateTime.now().format(DATE_FORMATTER);
        Path path = Paths.get(basePath, dateDir);
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            log.error("创建上传目录失败: {}", path, e);
            throw new RuntimeException("创建上传目录失败");
        }
        return path.toString();
    }

    public static boolean isValidFileType(String fileName) {
        if (StrUtil.isBlank(fileName)) {
            return false;
        }
        String extension = getFileExtension(fileName).toLowerCase();
        return extension.equals(".pdf") || extension.equals(".doc") || 
               extension.equals(".docx") || extension.equals(".md") || 
               extension.equals(".txt");
    }

    public static long getFileSize(MultipartFile file) {
        return file.getSize();
    }

    public static String getFileSizeReadable(long size) {
        return cn.hutool.core.io.FileUtil.readableFileSize(size);
    }

    public static void deleteFile(String filePath) {
        try {
            java.nio.file.Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            log.warn("删除文件失败: {}", filePath, e);
        }
    }
}