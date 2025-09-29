package com.literature.assistant.service.impl;

import cn.hutool.json.JSONUtil;
import com.literature.assistant.entity.Literature;
import com.literature.assistant.exception.BusinessException;
import com.literature.assistant.service.BatchImportService;
import com.literature.assistant.service.LiteratureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchImportServiceImpl implements BatchImportService {

    private final LiteratureService literatureService;

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final ConcurrentMap<String, MultipartFile[]> filesMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    @Override
    public String startImport(MultipartFile[] files) {
        validateFiles(files);
        String importId = UUID.randomUUID().toString();
        filesMap.put(importId, files);
        return importId;
    }

    @Override
    public SseEmitter connectProgress(String importId) {
        SseEmitter emitter = new SseEmitter(300000L);
        emitters.put(importId, emitter);

        emitter.onCompletion(() -> cleanup(importId));
        emitter.onTimeout(() -> cleanup(importId));
        emitter.onError(e -> cleanup(importId));

        executor.execute(() -> process(importId));
        return emitter;
    }

    private void process(String importId) {
        MultipartFile[] files = filesMap.get(importId);
        if (files == null) {
            try {
                sendEvent(importId, "error", "导入任务不存在");
            } catch (IOException ignored) {}
            completeWithError(importId, new IllegalArgumentException("导入任务不存在"));
            return;
        }

        try {
            sendEvent(importId, "start", files.length);

            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                try {
                    sendEvent(importId, "progress", "正在处理: " + file.getOriginalFilename());

                    Literature literature = literatureService.uploadLiterature(file);

                    sendEvent(importId, "file_complete", JSONUtil.createObj()
                            .set("fileName", file.getOriginalFilename())
                            .set("success", true)
                            .set("literatureId", literature.getId()));

                } catch (Exception e) {
                    log.error("文件处理失败: {}", file.getOriginalFilename(), e);
                    sendEvent(importId, "file_error", JSONUtil.createObj()
                            .set("fileName", file.getOriginalFilename())
                            .set("error", e.getMessage()));
                }

                sendEvent(importId, "progress_update", JSONUtil.createObj()
                        .set("current", i + 1)
                        .set("total", files.length));
            }

            sendEvent(importId, "complete", "批量导入完成");
            complete(importId);

        } catch (Exception e) {
            log.error("批量导入异常", e);
            try {
                sendEvent(importId, "error", "批量导入失败: " + e.getMessage());
            } catch (IOException ex) {
                log.error("SSE发送错误失败", ex);
            }
            completeWithError(importId, e);
        }
    }

    private void sendEvent(String importId, String eventName, Object data) throws IOException {
        SseEmitter emitter = emitters.get(importId);
        if (emitter != null) {
            emitter.send(SseEmitter.event().name(eventName).data(data));
        }
    }

    private void complete(String importId) {
        SseEmitter emitter = emitters.get(importId);
        if (emitter != null) {
            emitter.complete();
        }
        cleanup(importId);
    }

    private void completeWithError(String importId, Exception e) {
        SseEmitter emitter = emitters.get(importId);
        if (emitter != null) {
            emitter.completeWithError(e);
        }
        cleanup(importId);
    }

    private void cleanup(String importId) {
        emitters.remove(importId);
        filesMap.remove(importId);
    }

    private void validateFiles(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new BusinessException("请选择要导入的文件");
        }
        if (files.length > 16) {
            throw new BusinessException("一次最多导入16个文件");
        }
    }
}