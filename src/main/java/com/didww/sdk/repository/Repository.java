package com.didww.sdk.repository;

import com.didww.sdk.converter.DirtySerializationContext;
import com.didww.sdk.exception.DidwwClientException;
import com.didww.sdk.http.JsonApiMediaType;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.BaseResource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.lang.reflect.Field;
import java.util.Collection;

public class Repository<T extends BaseResource> extends ReadOnlyRepository<T> {

    public Repository(OkHttpClient httpClient, ResourceConverter converter,
                      String baseUrl, String endpoint, Class<T> resourceClass,
                      ObjectMapper objectMapper) {
        super(httpClient, converter, baseUrl, endpoint, resourceClass, objectMapper);
    }

    public ApiResponse<T> create(T resource) {
        return create(resource, null);
    }

    public ApiResponse<T> create(T resource, QueryParams params) {
        String url = baseUrl + "/" + endpoint + (params != null ? params.toQueryString() : "");
        byte[] payload;
        try {
            JSONAPIDocument<T> document = new JSONAPIDocument<>(resource);
            payload = converter.writeDocument(document);
        } catch (Exception e) {
            throw new DidwwClientException("Failed to serialize resource for create", e);
        }

        RequestBody body = RequestBody.create(payload, JsonApiMediaType.MEDIA_TYPE);
        Request request = new Request.Builder().url(url).post(body).build();

        try (Response response = httpClient.newCall(request).execute()) {
            handleErrorResponse(response);
            if (response.code() == 204) {
                return new ApiResponse<>(null, null);
            }
            byte[] responseBody = getResponseBody(response);
            JSONAPIDocument<T> responseDoc = converter.readDocument(responseBody, resourceClass);
            T data = responseDoc.get();
            enableDirtyTracking(data);
            return new ApiResponse<>(data, extractMeta(responseDoc));
        } catch (IOException e) {
            throw new DidwwClientException("Failed to create " + endpoint, e);
        }
    }

    public ApiResponse<T> update(T resource) {
        return update(resource, null);
    }

    public ApiResponse<T> update(T resource, QueryParams params) {
        String id = getResourceId(resource);
        String url = baseUrl + "/" + endpoint + "/" + id + (params != null ? params.toQueryString() : "");
        byte[] payload;
        try {
            DirtySerializationContext.enableDirtyOnlyMode();
            JSONAPIDocument<T> document = new JSONAPIDocument<>(resource);
            payload = converter.writeDocument(document);
            payload = ensureDirtyNullRelationships(resource, payload);
        } catch (Exception e) {
            throw new DidwwClientException("Failed to serialize resource for update", e);
        } finally {
            DirtySerializationContext.disableDirtyOnlyMode();
        }

        RequestBody body = RequestBody.create(payload, JsonApiMediaType.MEDIA_TYPE);
        Request request = new Request.Builder().url(url).patch(body).build();

        try (Response response = httpClient.newCall(request).execute()) {
            handleErrorResponse(response);
            byte[] responseBody = getResponseBody(response);
            JSONAPIDocument<T> responseDoc = converter.readDocument(responseBody, resourceClass);
            T data = responseDoc.get();
            enableDirtyTracking(data);
            return new ApiResponse<>(data, extractMeta(responseDoc));
        } catch (IOException e) {
            throw new DidwwClientException("Failed to update " + endpoint + "/" + id, e);
        }
    }

    public void delete(String id) {
        String url = baseUrl + "/" + endpoint + "/" + id;
        Request request = new Request.Builder().url(url).delete().build();

        try (Response response = httpClient.newCall(request).execute()) {
            handleErrorResponse(response);
        } catch (IOException e) {
            throw new DidwwClientException("Failed to delete " + endpoint + "/" + id, e);
        }
    }

    private byte[] ensureDirtyNullRelationships(T resource, byte[] payload) {
        try {
            JsonNode rootNode = objectMapper.readTree(payload);
            if (!(rootNode instanceof ObjectNode)) {
                return payload;
            }

            JsonNode dataNode = rootNode.get("data");
            if (!(dataNode instanceof ObjectNode)) {
                return payload;
            }

            ObjectNode dataObject = (ObjectNode) dataNode;
            ObjectNode relationshipsNode = getOrCreateRelationshipsNode(dataObject);

            boolean changed = false;
            for (Class<?> type = resource.getClass(); type != null && BaseResource.class.isAssignableFrom(type);
                 type = type.getSuperclass()) {
                for (Field field : type.getDeclaredFields()) {
                    Relationship relationship = field.getAnnotation(Relationship.class);
                    if (relationship == null) {
                        continue;
                    }
                    if (!resource.isFieldDirty(field.getName())) {
                        continue;
                    }

                    field.setAccessible(true);
                    Object value = field.get(resource);
                    if (value == null) {
                        ObjectNode relNode = relationshipsNode.putObject(relationship.value());
                        if (Collection.class.isAssignableFrom(field.getType())) {
                            relNode.putArray("data");
                        } else {
                            relNode.putNull("data");
                        }
                        changed = true;
                    }
                }
            }

            if (!changed) {
                return payload;
            }
            return objectMapper.writeValueAsBytes(rootNode);
        } catch (Exception e) {
            throw new DidwwClientException("Failed to serialize null relationships for update", e);
        }
    }

    private ObjectNode getOrCreateRelationshipsNode(ObjectNode dataObject) {
        JsonNode relationships = dataObject.get("relationships");
        if (relationships instanceof ObjectNode) {
            return (ObjectNode) relationships;
        }
        return dataObject.putObject("relationships");
    }

    private String getResourceId(T resource) {
        String id = resource.getId();
        if (id == null) {
            throw new DidwwClientException("Resource ID is null");
        }
        return id;
    }
}
