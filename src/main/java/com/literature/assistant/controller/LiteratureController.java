package com.literature.assistant.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.literature.assistant.common.Result;
import com.literature.assistant.entity.Literature;
import com.literature.assistant.service.LiteratureService;
import com.literature.assistant.service.SSEHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RestController
@RequestMapping("/literature")
@RequiredArgsConstructor
@Tag(name = "文献管理", description = "文献上传、查询、下载等接口")
public class LiteratureController {

    private final LiteratureService literatureService;
    private final ExecutorService sseExecutor = Executors.newVirtualThreadPerTaskExecutor();

    @PostMapping("/upload")
    @Operation(summary = "上传文献文件", description = "上传单个文献文件并生成阅读指南")
    public Result<Literature> uploadLiterature(
            @Parameter(description = "文献文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "API密钥") @RequestParam("apiKey") String apiKey) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        if (StrUtil.isBlank(apiKey)) {
            return Result.error("API密钥不能为空");
        }

        try {
            Literature literature = literatureService.uploadLiterature(file, apiKey);
            return Result.success("文献上传成功", literature);
        } catch (Exception e) {
            log.error("文献上传失败", e);
            return Result.error("文献上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/batch-import")
    @Operation(summary = "批量导入文献", description = "批量导入文献文件，通过SSE返回处理进度")
    public SseEmitter batchImportLiterature(
            @Parameter(description = "文献文件数组") @RequestParam("files") MultipartFile[] files,
            @Parameter(description = "API密钥") @RequestParam("apiKey") String apiKey,
            HttpServletResponse response) {

        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        SseEmitter emitter = new SseEmitter(300000L); // 5分钟超时

        sseExecutor.execute(() -> {
            try {
                if (files == null || files.length == 0) {
                    emitter.send(SseEmitter.event().name("error").data("请选择要导入的文件"));
                    emitter.complete();
                    return;
                }

                if (files.length > 16) {
                    emitter.send(SseEmitter.event().name("error").data("一次最多导入16个文件"));
                    emitter.complete();
                    return;
                }

                // 发送开始事件
                emitter.send(SseEmitter.event().name("start").data(files.length));

                for (int i = 0; i < files.length; i++) {
                    MultipartFile file = files[i];
                    try {
                        emitter.send(SseEmitter.event()
                                .name("progress")
                                .data("正在处理: " + file.getOriginalFilename()));

                        Literature literature = literatureService.uploadLiterature(file, apiKey);

                        emitter.send(SseEmitter.event()
                                .name("file_complete")
                                .data(JSONUtil.createObj()
                                        .set("fileName", file.getOriginalFilename())
                                        .set("success", true)
                                        .set("literatureId", literature.getId())));

                    } catch (Exception e) {
                        log.error("文件处理失败: {}", file.getOriginalFilename(), e);
                        emitter.send(SseEmitter.event()
                                .name("file_error")
                                .data(JSONUtil.createObj()
                                        .set("fileName", file.getOriginalFilename())
                                        .set("error", e.getMessage())));
                    }

                    // 更新进度
                    emitter.send(SseEmitter.event()
                            .name("progress_update")
                            .data(JSONUtil.createObj()
                                    .set("current", i + 1)
                                    .set("total", files.length)));
                }

                emitter.send(SseEmitter.event().name("complete").data("批量导入完成"));
                emitter.complete();

            } catch (Exception e) {
                log.error("批量导入异常", e);
                try {
                    emitter.send(SseEmitter.event().name("error").data("批量导入失败: " + e.getMessage()));
                } catch (IOException ex) {
                    log.error("SSE发送错误失败", ex);
                }
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询文献列表", description = "根据条件分页查询文献列表")
    public Result<IPage<Literature>> getLiteratureList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "分类") @RequestParam(required = false) String category,
            @Parameter(description = "描述关键词") @RequestParam(required = false) String description,
            @Parameter(description = "指南关键词") @RequestParam(required = false) String readingGuide,
            @Parameter(description = "标签") @RequestParam(required = false) String tags) {

        try {
            IPage<Literature> result = literatureService.getLiteraturePage(page, size, category, description, readingGuide, tags);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询文献列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取文献详情", description = "根据ID获取文献详细信息")
    public Result<Literature> getLiteratureDetail(@PathVariable Long id) {
        try {
            Literature literature = literatureService.getLiteratureDetail(id);
            if (literature == null) {
                return Result.error("文献不存在");
            }
            return Result.success(literature);
        } catch (Exception e) {
            log.error("获取文献详情失败", e);
            return Result.error("获取详情失败: " + e.getMessage());
        }
    }

    @GetMapping("/download/{id}")
    @Operation(summary = "下载文献文件", description = "根据ID下载文献原始文件")
    public ResponseEntity<byte[]> downloadLiterature(@PathVariable Long id) {
        try {
            Literature literature = literatureService.getLiteratureDetail(id);
            if (literature == null) {
                return ResponseEntity.notFound().build();
            }

            byte[] fileContent = literatureService.downloadLiterature(id);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + literature.getOriginalFileName() + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, getContentType(literature.getFileType()))
                    .body(fileContent);
                    
        } catch (Exception e) {
            log.error("下载文献失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private String getContentType(String fileType) {
        switch (fileType.toLowerCase()) {
            case ".pdf": return "application/pdf";
            case ".doc": return "application/msword";
            case ".docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case ".md": return "text/markdown";
            case ".txt": return "text/plain";
            default: return "application/octet-stream";
        }
    }
}