package com.didww.sdk.repository;

import com.didww.sdk.exception.DidwwApiException;
import com.didww.sdk.exception.DidwwClientException;
import com.didww.sdk.http.JsonApiMediaType;
import com.didww.sdk.http.QueryParams;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReadOnlyRepository<T> {
    protected final OkHttpClient httpClient;
    protected final ResourceConverter converter;
    protected final String baseUrl;
    protected final String endpoint;
    protected final Class<T> resourceClass;
    protected final ObjectMapper objectMapper;

    public ReadOnlyRepository(OkHttpClient httpClient, ResourceConverter converter,
                              String baseUrl, String endpoint, Class<T> resourceClass,
                              ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.converter = converter;
        this.baseUrl = baseUrl;
        this.endpoint = endpoint;
        this.resourceClass = resourceClass;
        this.objectMapper = objectMapper;
    }

    public ApiResponse<List<T>> list() {
        return list(null);
    }

    public ApiResponse<List<T>> list(QueryParams params) {
        String url = baseUrl + "/" + endpoint + (params != null ? params.toQueryString() : "");
        Request request = new Request.Builder().url(url).get().build();

        try (Response response = httpClient.newCall(request).execute()) {
            handleErrorResponse(response);
            byte[] body = getResponseBody(response);
            JSONAPIDocument<List<T>> document = converter.readDocumentCollection(body, resourceClass);
            Map<String, Object> meta = extractMeta(document);
            return new ApiResponse<>(document.get(), meta);
        } catch (DidwwApiException e) {
            throw e;
        } catch (Exception e) {
            throw new DidwwClientException("Failed to list " + endpoint, e);
        }
    }

    public ApiResponse<T> find(String id) {
        return find(id, null);
    }

    public ApiResponse<T> find(String id, QueryParams params) {
        String url = baseUrl + "/" + endpoint + "/" + id + (params != null ? params.toQueryString() : "");
        Request request = new Request.Builder().url(url).get().build();

        try (Response response = httpClient.newCall(request).execute()) {
            handleErrorResponse(response);
            byte[] body = getResponseBody(response);
            JSONAPIDocument<T> document = converter.readDocument(body, resourceClass);
            Map<String, Object> meta = extractMeta(document);
            return new ApiResponse<>(document.get(), meta);
        } catch (DidwwApiException e) {
            throw e;
        } catch (Exception e) {
            throw new DidwwClientException("Failed to find " + endpoint + "/" + id, e);
        }
    }

    protected void handleErrorResponse(Response response) throws IOException {
        if (!response.isSuccessful()) {
            String body = response.body() != null ? response.body().string() : "";
            List<DidwwApiException.ApiError> errors = new ArrayList<>();
            try {
                JsonNode root = objectMapper.readTree(body);
                JsonNode errorsNode = root.get("errors");
                if (errorsNode != null && errorsNode.isArray()) {
                    for (JsonNode errorNode : errorsNode) {
                        DidwwApiException.ApiError error = objectMapper.treeToValue(
                                errorNode, DidwwApiException.ApiError.class);
                        errors.add(error);
                    }
                }
            } catch (Exception ignored) {
            }
            if (errors.isEmpty()) {
                throw new DidwwApiException(response.code(), body);
            }
            throw new DidwwApiException(response.code(), errors);
        }
    }

    protected byte[] getResponseBody(Response response) throws IOException {
        ResponseBody body = response.body();
        if (body == null) {
            throw new DidwwClientException("Empty response body");
        }
        return body.bytes();
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> extractMeta(JSONAPIDocument<?> document) {
        Object meta = document.getMeta();
        if (meta instanceof Map) {
            return (Map<String, Object>) meta;
        }
        return null;
    }
}
