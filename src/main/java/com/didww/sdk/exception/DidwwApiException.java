package com.didww.sdk.exception;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DidwwApiException extends RuntimeException {
    private final int httpStatus;
    private final List<ApiError> errors;

    public DidwwApiException(int httpStatus, List<ApiError> errors) {
        super(buildMessage(httpStatus, errors));
        this.httpStatus = httpStatus;
        this.errors = errors != null ? Collections.unmodifiableList(errors) : Collections.emptyList();
    }

    public DidwwApiException(int httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.errors = Collections.emptyList();
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public List<ApiError> getErrors() {
        return errors;
    }

    private static String buildMessage(int httpStatus, List<ApiError> errors) {
        StringBuilder sb = new StringBuilder("DIDWW API error (HTTP " + httpStatus + ")");
        if (errors != null && !errors.isEmpty()) {
            sb.append(": ");
            for (int i = 0; i < errors.size(); i++) {
                if (i > 0) sb.append("; ");
                sb.append(errors.get(i).getDetail());
            }
        }
        return sb.toString();
    }

    public static class ApiError {
        private String title;
        private String detail;
        private String status;
        private String code;
        private Map<String, Object> source;
        private Map<String, Object> meta;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDetail() { return detail; }
        public void setDetail(String detail) { this.detail = detail; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public Map<String, Object> getSource() { return source; }
        public void setSource(Map<String, Object> source) { this.source = source; }

        public Map<String, Object> getMeta() { return meta; }
        public void setMeta(Map<String, Object> meta) { this.meta = meta; }

        @Override
        public String toString() {
            return "ApiError{title='" + title + "', detail='" + detail + "'}";
        }
    }
}
