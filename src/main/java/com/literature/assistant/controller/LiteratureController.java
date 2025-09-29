package com.literature.assistant.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.literature.assistant.common.Result;
import com.literature.assistant.entity.Literature;
import com.literature.assistant.service.LiteratureService;
import com.literature.assistant.service.SSEHandler;
import com.literature.assistant.service.BatchImportService;
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

import jakarta.servlet.http.HttpServletResponse;
import cn.hutool.json.JSONUtil;
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
    private final BatchImportService batchImportService;
    private final ExecutorService sseExecutor = Executors.newVirtualThreadPerTaskExecutor();

    @PostMapping("/upload")
    @Operation(summary = "上传文献文件", description = "上传单个文献文件并生成阅读指南")
    public Result<Literature> uploadLiterature(
            @Parameter(description = "文献文件") @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        try {
            Literature literature = literatureService.uploadLiterature(file);
            return Result.success("文献上传成功", literature);
        } catch (Exception e) {
            log.error("文献上传失败", e);
            return Result.error("文献上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/batch-import/start")
    @Operation(summary = "创建批量导入任务", description = "创建批量导入任务，返回任务ID")
    public java.util.Map<String, String> startBatchImport(
            @Parameter(description = "文献文件数组") @RequestParam("files") MultipartFile[] files) {
        String importId = batchImportService.startImport(files);
        return java.util.Collections.singletonMap("importId", importId);
    }

    @GetMapping(value = "/batch-import/progress/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "批量导入进度SSE", description = "根据任务ID连接SSE获取批量导入进度")
    public SseEmitter batchImportProgress(@PathVariable("id") String importId) {
        return batchImportService.connectProgress(importId);
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