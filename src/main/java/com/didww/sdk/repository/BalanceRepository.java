package com.didww.sdk.repository;

import com.didww.sdk.exception.DidwwClientException;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.Balance;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BalanceRepository extends ReadOnlyRepository<Balance> {

    public BalanceRepository(OkHttpClient httpClient, ResourceConverter converter,
                             String baseUrl, String endpoint, ObjectMapper objectMapper) {
        super(httpClient, converter, baseUrl, endpoint, Balance.class, objectMapper);
    }

    public ApiResponse<Balance> find() {
        return find((QueryParams) null);
    }

    public ApiResponse<Balance> find(QueryParams params) {
        String url = baseUrl + "/" + endpoint + (params != null ? params.toQueryString() : "");
        Request request = new Request.Builder().url(url).get().build();

        try (Response response = httpClient.newCall(request).execute()) {
            handleErrorResponse(response);
            byte[] body = getResponseBody(response);
            JSONAPIDocument<Balance> document = converter.readDocument(body, resourceClass);
            return new ApiResponse<>(document.get(), extractMeta(document));
        } catch (IOException e) {
            throw new DidwwClientException("Failed to find " + endpoint, e);
        }
    }
}
