package com.literature.assistant.util;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

public class JSONUtil {
    
    public static JSONObject createObj() {
        return JSONUtil.createObj();
    }
    
    public static String toJsonStr(Object obj) {
        return JSONUtil.toJsonStr(obj);
    }
    
    public static <T> T parseObj(String jsonStr, Class<T> beanClass) {
        return JSONUtil.toBean(jsonStr, beanClass);
    }
}