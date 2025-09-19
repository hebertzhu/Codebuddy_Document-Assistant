package com.literature.assistant.config;

import com.literature.assistant.common.Result;
import com.literature.assistant.common.ResultCode;
import com.literature.assistant.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        log.warn("参数验证失败: {}", errors);
        return Result.error(ResultCode.BAD_REQUEST.getCode(), "参数验证失败: " + errors);
    }

    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        log.warn("参数绑定失败: {}", errors);
        return Result.error(ResultCode.BAD_REQUEST.getCode(), "参数绑定失败: " + errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleConstraintViolationException(ConstraintViolationException e) {
        log.warn("参数约束违反: {}", e.getMessage());
        return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<?> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.warn("文件大小超过限制: {}", e.getMessage());
        return Result.error(ResultCode.FILE_TOO_LARGE);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数: {}", e.getMessage());
        return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.error(ResultCode.ERROR.getCode(), "系统异常，请稍后重试");
    }
}