package com.didww.sdk.resource;

import com.github.jasminb.jsonapi.annotations.Id;

public abstract class BaseResource {

    @Id
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public static <T extends BaseResource> T build(Class<T> type, String id) {
        try {
            T instance = type.getDeclaredConstructor().newInstance();
            instance.setId(id);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to build " + type.getSimpleName(), e);
        }
    }
}
