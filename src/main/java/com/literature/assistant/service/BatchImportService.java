package com.literature.assistant.service;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface BatchImportService {

    /**
     * 创建批量导入任务并返回任务ID
     */
    String startImport(MultipartFile[] files);

    /**
     * 连接到指定任务的SSE进度流
     */
    SseEmitter connectProgress(String importId);
}