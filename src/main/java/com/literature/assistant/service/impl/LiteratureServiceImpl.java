package com.literature.assistant.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.literature.assistant.entity.Literature;
import com.literature.assistant.exception.BusinessException;
import com.literature.assistant.mapper.LiteratureMapper;
import com.literature.assistant.service.LiteratureService;
import com.literature.assistant.service.AIService;
import com.literature.assistant.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LiteratureServiceImpl extends ServiceImpl<LiteratureMapper, Literature> implements LiteratureService {

    private final LiteratureMapper literatureMapper;
    private final AIService aiService;

    @Value("${file.upload.path}")
    private String uploadBasePath;

    // AI 密钥由 AIService 的实现统一从配置读取

    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    @Override
    public IPage<Literature> getLiteraturePage(int page, int size, String category, 
                                            String description, String readingGuide, String tags) {
        Page<Literature> pageParam = new Page<>(page, size);
        return literatureMapper.selectLiteraturePage(pageParam, category, description, readingGuide, tags);
    }

    @Override
    @Transactional
    public Literature uploadLiterature(MultipartFile file) {
        // 验证文件
        validateFile(file);

        // 保存文件
        String filePath = saveFile(file);

        // 创建文献记录
        Literature literature = createLiteratureRecord(file, filePath);

        // 调用AI生成阅读指南
        generateReadingGuide(literature);

        // 保存到数据库
        save(literature);

        return literature;
    }

    @Override
    public void batchImportLiterature(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new BusinessException("请选择要导入的文件");
        }

        if (files.length > 16) {
            throw new BusinessException("一次最多导入16个文件");
        }

        for (MultipartFile file : files) {
            CompletableFuture.runAsync(() -> {
                try {
                    uploadLiterature(file);
                    // SSE推送处理完成消息
                    // 这里需要实现SSE推送逻辑
                } catch (Exception e) {
                    log.error("批量导入文件失败: {}", file.getOriginalFilename(), e);
                    // SSE推送错误消息
                }
            }, virtualThreadExecutor);
        }
    }

    @Override
    public byte[] downloadLiterature(Long id) {
        Literature literature = getById(id);
        if (literature == null) {
            throw new BusinessException("文献不存在");
        }

        try {
            return Files.readAllBytes(Paths.get(literature.getFilePath()));
        } catch (IOException e) {
            log.error("下载文献文件失败: " + literature.getFilePath(), e);
            throw new BusinessException("文件下载失败");
        }
    }

    @Override
    public Literature getLiteratureDetail(Long id) {
        return getById(id);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (!FileUtil.isValidFileType(originalFilename)) {
            throw new BusinessException("不支持的文件类型，仅支持PDF、Word、Markdown文件");
        }

        long fileSize = file.getSize();
        if (fileSize > 50 * 1024 * 1024) { // 50MB
            throw new BusinessException("文件大小不能超过50MB");
        }
    }

    private String saveFile(MultipartFile file) {
        try {
            String uploadPath = FileUtil.getUploadPath(uploadBasePath);
            String fileName = FileUtil.generateFileName(file.getOriginalFilename());
            Path filePath = Paths.get(uploadPath, fileName);

            Files.copy(file.getInputStream(), filePath);
            return filePath.toString();
        } catch (IOException e) {
            log.error("保存文件失败", e);
            throw new BusinessException("文件保存失败");
        }
    }

    private Literature createLiteratureRecord(MultipartFile file, String filePath) {
        Literature literature = new Literature();
        literature.setTitle(StrUtil.subBefore(file.getOriginalFilename(), ".", true));
        literature.setOriginalFileName(file.getOriginalFilename());
        literature.setFilePath(filePath);
        literature.setFileSize(file.getSize());
        literature.setFileType(FileUtil.getFileExtension(file.getOriginalFilename()));
        literature.setCreateTime(LocalDateTime.now());
        literature.setUpdateTime(LocalDateTime.now());
        literature.setDeleted(0);
        return literature;
    }

    private void generateReadingGuide(Literature literature) {
        try {
            // 解析文件内容
            String content = parseFileContent(literature.getFilePath());
            
            // 调用AI生成阅读指南
            String readingGuide = aiService.generateReadingGuide(content);
            literature.setReadingGuide(readingGuide);

            // 使用虚拟线程异步生成分类和描述
            CompletableFuture.runAsync(() -> {
                try {
                    String aiResponse = aiService.generateClassification(content);
                    // 解析AI返回的JSON并更新文献信息
                    updateLiteratureWithAIResponse(literature, aiResponse);
                    updateById(literature);
                } catch (Exception e) {
                    log.error("AI分类生成失败: " + literature.getTitle(), e);
                }
            }, virtualThreadExecutor);

        } catch (Exception e) {
            log.error("生成阅读指南失败: " + literature.getTitle(), e);
            throw new BusinessException("AI服务调用失败");
        }
    }

    private String parseFileContent(String filePath) {
        // 实现文件内容解析逻辑
        // 这里需要根据文件类型使用不同的解析器
        // PDF使用PDFBox，Word使用POI，Markdown直接读取
        return "文件内容解析占位符";
    }

    private void updateLiteratureWithAIResponse(Literature literature, String aiResponse) {
        // 解析AI返回的JSON并更新文献信息
        // 这里需要实现JSON解析逻辑
        literature.setCategory("AI生成分类");
        literature.setDescription("AI生成描述");
        literature.setTags("AI,文献,学术");
    }
}