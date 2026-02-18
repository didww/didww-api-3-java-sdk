package com.didww.sdk.repository;

import java.util.Map;

public class ApiResponse<T> {
    private final T data;
    private final Map<String, Object> meta;

    public ApiResponse(T data, Map<String, Object> meta) {
        this.data = data;
        this.meta = meta;
    }

    public T getData() {
        return data;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }
}
