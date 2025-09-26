package com.literature.assistant.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Slf4j
@Component
public class FileParser {

    /**
     * 解析文件内容
     */
    public String parseFileContent(String filePath) throws IOException {
        String fileType = FileUtil.getFileExtension(filePath).toLowerCase();
        
        switch (fileType) {
            case ".pdf":
                return parsePdfFile(filePath);
            case ".doc":
            case ".docx":
                return parseWordFile(filePath);
            case ".md":
            case ".txt":
                return parseTextFile(filePath);
            default:
                throw new IllegalArgumentException("不支持的文件类型: " + fileType);
        }
    }

    /**
     * 解析PDF文件
     */
    private String parsePdfFile(String filePath) throws IOException {
        try (PDDocument document = Loader.loadPDF(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * 解析Word文件
     */
    private String parseWordFile(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath);
             XWPFDocument document = new XWPFDocument(fis)) {
            
            StringBuilder content = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (text != null && !text.trim().isEmpty()) {
                    content.append(text).append("\n");
                }
            }
            return content.toString();
        }
    }

    /**
     * 解析文本文件
     */
    private String parseTextFile(String filePath) throws IOException {
        return new String(Files.readAllBytes(new File(filePath).toPath()), StandardCharsets.UTF_8);
    }

    /**
     * 获取文件内容摘要（前2000字符）
     */
    public String getContentSummary(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        
        // 取前2000字符作为摘要
        String summary = content.length() > 2000 ? content.substring(0, 2000) + "..." : content;
        
        // 清理过多的空白字符
        summary = summary.replaceAll("\\s+", " ").trim();
        
        return summary;
    }
}