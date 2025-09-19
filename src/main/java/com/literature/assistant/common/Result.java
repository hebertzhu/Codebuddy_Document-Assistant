package com.literature.assistant.common;

import lombok.Data;
import java.io.Serializable;

@Data
public class Result<T> implements Serializable {
    private Integer code;
    private String message;
    private T data;
    private Long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.code = ResultCode.SUCCESS.getCode();
        result.message = ResultCode.SUCCESS.getMessage();
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = ResultCode.SUCCESS.getCode();
        result.message = ResultCode.SUCCESS.getMessage();
        result.data = data;
        return result;
    }

    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.code = ResultCode.SUCCESS.getCode();
        result.message = message;
        result.data = data;
        return result;
    }

    public static <T> Result<T> error() {
        Result<T> result = new Result<>();
        result.code = ResultCode.ERROR.getCode();
        result.message = ResultCode.ERROR.getMessage();
        return result;
    }

    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.code = ResultCode.ERROR.getCode();
        result.message = message;
        return result;
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.code = code;
        result.message = message;
        return result;
    }

    public static <T> Result<T> error(ResultCode resultCode) {
        Result<T> result = new Result<>();
        result.code = resultCode.getCode();
        result.message = resultCode.getMessage();
        return result;
    }
}