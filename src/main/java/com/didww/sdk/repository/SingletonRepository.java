package com.didww.sdk.repository;

import com.didww.sdk.exception.DidwwApiException;
import com.didww.sdk.exception.DidwwClientException;
import com.didww.sdk.http.QueryParams;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SingletonRepository<T> extends ReadOnlyRepository<T> {

    public SingletonRepository(OkHttpClient httpClient, ResourceConverter converter,
                               String baseUrl, String endpoint, Class<T> resourceClass,
                               ObjectMapper objectMapper) {
        super(httpClient, converter, baseUrl, endpoint, resourceClass, objectMapper);
    }

    public ApiResponse<T> find() {
        return find((QueryParams) null);
    }

    public ApiResponse<T> find(QueryParams params) {
        String url = baseUrl + "/" + endpoint + (params != null ? params.toQueryString() : "");
        Request request = new Request.Builder().url(url).get().build();

        try (Response response = httpClient.newCall(request).execute()) {
            handleErrorResponse(response);
            byte[] body = getResponseBody(response);
            JSONAPIDocument<T> document = converter.readDocument(body, resourceClass);
            return new ApiResponse<>(document.get(), extractMeta(document));
        } catch (DidwwApiException e) {
            throw e;
        } catch (Exception e) {
            throw new DidwwClientException("Failed to find " + endpoint, e);
        }
    }
}
