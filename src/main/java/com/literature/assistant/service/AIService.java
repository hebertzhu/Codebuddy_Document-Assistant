package com.literature.assistant.service;

public interface AIService {
    
    /**
     * 生成文献阅读指南
     */
    String generateReadingGuide(String content);
    
    /**
     * 生成文献分类和描述（返回JSON格式）
     */
    String generateClassification(String content);
    
    /**
     * 流式生成阅读指南（SSE）
     */
    void generateReadingGuideStream(String content, SSEHandler sseHandler);
}