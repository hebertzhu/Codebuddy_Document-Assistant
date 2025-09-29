package com.literature.assistant.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.literature.assistant.service.AIService;
import com.literature.assistant.service.SSEHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class KimiAIServiceImpl implements AIService {

    private final OkHttpClient okHttpClient;
    private final ResourceLoader resourceLoader;

    @Value("${ai.kimi.base-url}")
    private String baseUrl;

    @Value("${ai.kimi.timeout}")
    private int timeout;

    @Value("${ai.kimi.max-tokens}")
    private int maxTokens;

    @Value("${ai.kimi.temperature}")
    private double temperature;

    @Value("${ai.kimi.api-key:}")
    private String kimiApiKey;

    @Override
    public String generateReadingGuide(String content) {
        try {
            String systemPrompt = loadSystemPrompt();
            String requestBody = buildChatRequest(systemPrompt, content, false);

            Request request = new Request.Builder()
                    .url(baseUrl + "/chat/completions")
                    .header("Authorization", "Bearer " + kimiApiKey)
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody, MediaType.get("application/json")))
                    .build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("AI服务调用失败: " + response.code() + " - " + response.message());
                }

                String responseBody = response.body().string();
                JSONObject jsonResponse = JSONUtil.parseObj(responseBody);
                return jsonResponse.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getStr("content");
            }
        } catch (Exception e) {
            log.error("生成阅读指南失败", e);
            throw new RuntimeException("AI服务调用失败", e);
        }
    }

    @Override
    public String generateClassification(String content) {
        try {
            String classificationPrompt = "请根据以下文献内容生成分类和描述信息。响应必须是纯净的JSON格式，包含以下字段：\n" +
                    "- category: 文献分类（字符串）\n" +
                    "- description: 文献描述（字符串，100-200字）\n" +
                    "- tags: 文献标签（数组，3-5个关键词）\n\n" +
                    "文献内容：\n" + content.substring(0, Math.min(content.length(), 2000));

            String requestBody = buildChatRequest(classificationPrompt, content, true);

            Request request = new Request.Builder()
                    .url(baseUrl + "/chat/completions")
                    .header("Authorization", "Bearer " + kimiApiKey)
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody, MediaType.get("application/json")))
                    .build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("AI分类服务调用失败: " + response.code() + " - " + response.message());
                }

                String responseBody = response.body().string();
                JSONObject jsonResponse = JSONUtil.parseObj(responseBody);
                return jsonResponse.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getStr("content");
            }
        } catch (Exception e) {
            log.error("生成分类信息失败", e);
            throw new RuntimeException("AI分类服务调用失败", e);
        }
    }

    @Override
    public void generateReadingGuideStream(String content, SSEHandler sseHandler) {
        SseEmitter emitter = new SseEmitter(300000L); // 5分钟超时

        try {
            String systemPrompt = loadSystemPrompt();
            String requestBody = buildChatRequest(systemPrompt, content, true);

            Request request = new Request.Builder()
                    .url(baseUrl + "/chat/completions")
                    .header("Authorization", "Bearer " + kimiApiKey)
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody, MediaType.get("application/json")))
                    .build();

            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    log.error("SSE AI调用失败", e);
                    emitter.completeWithError(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        emitter.completeWithError(new IOException("AI服务调用失败: " + response.code()));
                        return;
                    }

                    try (ResponseBody responseBody = response.body()) {
                        if (responseBody != null) {
                            String line;
                            while ((line = responseBody.source().readUtf8Line()) != null) {
                                if (line.startsWith("data: ")) {
                                    String data = line.substring(6);
                                    if ("[DONE]".equals(data)) {
                                        emitter.complete();
                                        break;
                                    }
                                    sseHandler.handle(emitter, data);
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("SSE响应处理失败", e);
                        emitter.completeWithError(e);
                    }
                }
            });

        } catch (Exception e) {
            log.error("SSE请求构建失败", e);
            emitter.completeWithError(e);
        }
    }

    private String loadSystemPrompt() {
        try {
            Resource resource = resourceLoader.getResource("classpath:system-prompt.txt");
            return FileUtil.readUtf8String(resource.getFile());
        } catch (Exception e) {
            log.warn("无法加载系统提示词，使用默认提示词", e);
            return "你是一个专业的学术文献分析助手。请根据用户上传的文献内容，生成一份结构化的阅读指南。";
        }
    }

    private String buildChatRequest(String systemPrompt, String content, boolean stream) {
        JSONObject requestJson = JSONUtil.createObj();
        requestJson.set("model", "moonshot-v1-8k");
        requestJson.set("max_tokens", maxTokens);
        requestJson.set("temperature", temperature);
        requestJson.set("stream", stream);

        JSONObject systemMessage = JSONUtil.createObj()
                .set("role", "system")
                .set("content", systemPrompt);

        JSONObject userMessage = JSONUtil.createObj()
                .set("role", "user")
                .set("content", content);

        requestJson.set("messages", JSONUtil.createArray().add(systemMessage).add(userMessage));

        return requestJson.toString();
    }

    // OkHttpClient Bean moved to AIConfig to avoid circular dependency
}