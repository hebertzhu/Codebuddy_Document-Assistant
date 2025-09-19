package com.literature.assistant.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    ERROR(500, "系统异常"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    FILE_TOO_LARGE(413, "文件过大"),
    UNSUPPORTED_MEDIA_TYPE(415, "不支持的媒体类型"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),

    // 业务异常
    FILE_UPLOAD_FAILED(1001, "文件上传失败"),
    FILE_PARSE_FAILED(1002, "文件解析失败"),
    AI_SERVICE_ERROR(1003, "AI服务调用失败"),
    INVALID_API_KEY(1004, "无效的API密钥"),
    LITERATURE_NOT_FOUND(1005, "文献不存在"),
    BATCH_IMPORT_LIMIT(1006, "批量导入数量超过限制");

    private final Integer code;
    private final String message;
}