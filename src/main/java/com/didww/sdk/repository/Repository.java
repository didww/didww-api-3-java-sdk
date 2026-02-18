package com.didww.sdk.repository;

import com.didww.sdk.exception.DidwwApiException;
import com.didww.sdk.exception.DidwwClientException;
import com.didww.sdk.http.JsonApiMediaType;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.BaseResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
            byte[] responseBody = getResponseBody(response);
            JSONAPIDocument<T> responseDoc = converter.readDocument(responseBody, resourceClass);
            return new ApiResponse<>(responseDoc.get(), extractMeta(responseDoc));
        } catch (DidwwApiException e) {
            throw e;
        } catch (Exception e) {
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
            JSONAPIDocument<T> document = new JSONAPIDocument<>(resource);
            payload = converter.writeDocument(document);
        } catch (Exception e) {
            throw new DidwwClientException("Failed to serialize resource for update", e);
        }

        RequestBody body = RequestBody.create(payload, JsonApiMediaType.MEDIA_TYPE);
        Request request = new Request.Builder().url(url).patch(body).build();

        try (Response response = httpClient.newCall(request).execute()) {
            handleErrorResponse(response);
            byte[] responseBody = getResponseBody(response);
            JSONAPIDocument<T> responseDoc = converter.readDocument(responseBody, resourceClass);
            return new ApiResponse<>(responseDoc.get(), extractMeta(responseDoc));
        } catch (DidwwApiException e) {
            throw e;
        } catch (Exception e) {
            throw new DidwwClientException("Failed to update " + endpoint + "/" + id, e);
        }
    }

    public void delete(String id) {
        String url = baseUrl + "/" + endpoint + "/" + id;
        Request request = new Request.Builder().url(url).delete().build();

        try (Response response = httpClient.newCall(request).execute()) {
            handleErrorResponse(response);
        } catch (DidwwApiException e) {
            throw e;
        } catch (Exception e) {
            throw new DidwwClientException("Failed to delete " + endpoint + "/" + id, e);
        }
    }

    private String getResourceId(T resource) {
        String id = resource.getId();
        if (id == null) {
            throw new DidwwClientException("Resource ID is null");
        }
        return id;
    }
}
