package com.didww.sdk.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class QueryParams {
    private final Map<String, String> filters;
    private final List<String> includes;
    private final List<String> sorts;
    private final Integer pageNumber;
    private final Integer pageSize;

    private QueryParams(Builder builder) {
        this.filters = Collections.unmodifiableMap(new LinkedHashMap<>(builder.filters));
        this.includes = Collections.unmodifiableList(new ArrayList<>(builder.includes));
        this.sorts = Collections.unmodifiableList(new ArrayList<>(builder.sorts));
        this.pageNumber = builder.pageNumber;
        this.pageSize = builder.pageSize;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String toQueryString() {
        List<String> parts = new ArrayList<>();

        if (!includes.isEmpty()) {
            parts.add("include=" + encode(String.join(",", includes)));
        }

        for (Map.Entry<String, String> entry : filters.entrySet()) {
            parts.add("filter[" + encode(entry.getKey()) + "]=" + encode(entry.getValue()));
        }

        if (!sorts.isEmpty()) {
            parts.add("sort=" + encode(String.join(",", sorts)));
        }

        if (pageNumber != null) {
            parts.add("page[number]=" + pageNumber);
        }
        if (pageSize != null) {
            parts.add("page[size]=" + pageSize);
        }

        return parts.isEmpty() ? "" : "?" + String.join("&", parts);
    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Builder {
        private final Map<String, String> filters = new LinkedHashMap<>();
        private final List<String> includes = new ArrayList<>();
        private final List<String> sorts = new ArrayList<>();
        private Integer pageNumber;
        private Integer pageSize;

        public Builder filter(String key, String value) {
            filters.put(key, value);
            return this;
        }

        public Builder include(String... relationships) {
            includes.addAll(Arrays.asList(relationships));
            return this;
        }

        public Builder sort(String... fields) {
            sorts.addAll(Arrays.asList(fields));
            return this;
        }

        public Builder page(int number, int size) {
            this.pageNumber = number;
            this.pageSize = size;
            return this;
        }

        public QueryParams build() {
            return new QueryParams(this);
        }
    }
}
