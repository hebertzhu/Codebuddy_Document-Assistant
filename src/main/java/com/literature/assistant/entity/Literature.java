package com.literature.assistant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("literature")
public class Literature {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 文献标题
     */
    private String title;
    
    /**
     * 文献存储路径
     */
    private String filePath;
    
    /**
     * 原始文件名
     */
    private String originalFileName;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 文件类型（pdf/doc/docx/md/txt）
     */
    private String fileType;
    
    /**
     * 文献标签，多个用逗号分隔
     */
    private String tags;
    
    /**
     * 文献描述
     */
    private String description;
    
    /**
     * AI生成的阅读指南
     */
    private String readingGuide;
    
    /**
     * 文献分类
     */
    private String category;
    
    /**
     * 作者
     */
    private String author;
    
    /**
     * 出版年份
     */
    private Integer publishYear;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除标志（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer deleted;

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setPublishYear(Integer publishYear) {
        this.publishYear = publishYear;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setReadingGuide(String readingGuide) {
        this.readingGuide = readingGuide;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * 获取可读的文件大小
     */
    public String getFileSizeReadable() {
        if (fileSize == null) return "";
        long size = fileSize;
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }
}