package com.didww.sdk.resource;

import com.didww.sdk.resource.enums.CallbackMethod;
import com.didww.sdk.resource.enums.ExportStatus;
import com.didww.sdk.resource.enums.ExportType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Type;

import java.time.OffsetDateTime;
import java.util.Map;

@Type("exports")
public class Export extends BaseResource {

    public static Export build(String id) {
        return BaseResource.build(Export.class, id);
    }

    @JsonProperty("export_type")
    private ExportType exportType;

    @JsonProperty(value = "url", access = JsonProperty.Access.WRITE_ONLY)
    private String url;

    @JsonProperty("callback_url")
    private String callbackUrl;

    @JsonProperty("callback_method")
    private CallbackMethod callbackMethod;

    @JsonProperty(value = "status", access = JsonProperty.Access.WRITE_ONLY)
    private ExportStatus status;

    @JsonProperty("filters")
    private Map<String, Object> filters;

    @JsonProperty(value = "created_at", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime createdAt;

    public ExportType getExportType() {
        return exportType;
    }

    public void setExportType(ExportType exportType) {
        this.exportType = exportType;
    }

    public String getUrl() {
        return url;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public CallbackMethod getCallbackMethod() {
        return callbackMethod;
    }

    public void setCallbackMethod(CallbackMethod callbackMethod) {
        this.callbackMethod = callbackMethod;
    }

    public ExportStatus getStatus() {
        return status;
    }

    public Map<String, Object> getFilters() {
        return filters;
    }

    public void setFilters(Map<String, Object> filters) {
        this.filters = filters;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
