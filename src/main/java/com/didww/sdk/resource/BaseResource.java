package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.jasminb.jsonapi.annotations.Id;

import java.util.HashSet;
import java.util.Set;

@JsonFilter("dirtyNullFilter")
public abstract class BaseResource {

    @Id
    private String id;

    @JsonIgnore
    private final Set<String> dirtyFields = new HashSet<>();

    @JsonIgnore
    private boolean dirtyTrackingEnabled = false;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = markDirty("id", id);
    }

    public static <T extends BaseResource> T build(Class<T> type, String id) {
        try {
            T instance = type.getDeclaredConstructor().newInstance();
            instance.enableDirtyTracking();
            instance.setId(id);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to build " + type.getSimpleName(), e);
        }
    }

    protected <T> T markDirty(String fieldName, T value) {
        markDirty(fieldName);
        return value;
    }

    protected void markDirty(String fieldName) {
        if (!dirtyTrackingEnabled || fieldName == null || fieldName.isEmpty()) {
            return;
        }
        dirtyFields.add(toSnakeCase(fieldName));
    }

    @JsonIgnore
    public boolean isFieldDirty(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            return false;
        }
        return dirtyFields.contains(toSnakeCase(fieldName));
    }

    @JsonIgnore
    public boolean hasDirtyFields() {
        return !dirtyFields.isEmpty();
    }

    @JsonIgnore
    public void enableDirtyTracking() {
        dirtyTrackingEnabled = true;
    }

    private String toSnakeCase(String fieldName) {
        StringBuilder snakeCase = new StringBuilder();
        for (int i = 0; i < fieldName.length(); i++) {
            char current = fieldName.charAt(i);
            if (Character.isUpperCase(current)) {
                if (i > 0 && fieldName.charAt(i - 1) != '_') {
                    snakeCase.append('_');
                }
                snakeCase.append(Character.toLowerCase(current));
            } else {
                snakeCase.append(current);
            }
        }
        return snakeCase.toString();
    }

}
