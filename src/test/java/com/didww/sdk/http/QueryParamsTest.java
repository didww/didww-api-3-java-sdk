package com.didww.sdk.http;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class QueryParamsTest {

    @Test
    void testEmptyParams() {
        QueryParams params = QueryParams.builder().build();
        assertThat(params.toQueryString()).isEmpty();
    }

    @Test
    void testFilterParams() {
        QueryParams params = QueryParams.builder()
                .filter("name", "test")
                .build();
        assertThat(params.toQueryString()).isEqualTo("?filter[name]=test");
    }

    @Test
    void testMultipleFilters() {
        QueryParams params = QueryParams.builder()
                .filter("name", "test")
                .filter("country.id", "abc")
                .build();
        assertThat(params.toQueryString()).contains("filter[name]=test");
        assertThat(params.toQueryString()).contains("filter[country.id]=abc");
    }

    @Test
    void testIncludeParams() {
        QueryParams params = QueryParams.builder()
                .include("trunk", "pop")
                .build();
        assertThat(params.toQueryString()).isEqualTo("?include=trunk%2Cpop");
    }

    @Test
    void testSortParams() {
        QueryParams params = QueryParams.builder()
                .sort("-created_at", "name")
                .build();
        assertThat(params.toQueryString()).isEqualTo("?sort=-created_at%2Cname");
    }

    @Test
    void testPaginationParams() {
        QueryParams params = QueryParams.builder()
                .page(2, 50)
                .build();
        assertThat(params.toQueryString()).isEqualTo("?page[number]=2&page[size]=50");
    }

    @Test
    void testCombinedParams() {
        QueryParams params = QueryParams.builder()
                .include("trunk")
                .filter("name", "test")
                .sort("-created_at")
                .page(1, 25)
                .build();
        String qs = params.toQueryString();
        assertThat(qs).startsWith("?");
        assertThat(qs).contains("include=trunk");
        assertThat(qs).contains("filter[name]=test");
        assertThat(qs).contains("sort=-created_at");
        assertThat(qs).contains("page[number]=1");
        assertThat(qs).contains("page[size]=25");
    }
}
