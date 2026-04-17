package com.didww.sdk.resource;

import com.didww.sdk.resource.enums.CallbackMethod;
import com.didww.sdk.resource.enums.ExportStatus;
import com.didww.sdk.resource.enums.ExportType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Map;

@Type("exports")

@Getter
public class Export extends BaseResource {

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

    /**
     * Export filters map. For CDR exports, the following keys are supported:
     * <ul>
     *   <li>{@code from} - ISO 8601 lower bound, INCLUSIVE (time_start &gt;= from). cdr_in / cdr_out only.</li>
     *   <li>{@code to} - ISO 8601 upper bound, EXCLUSIVE (time_start &lt; to). cdr_in / cdr_out only.</li>
     *   <li>{@code did_number} - filter by DID number (cdr_in only).</li>
     *   <li>{@code voice_out_trunk_id} - filter by trunk (cdr_out only).</li>
     * </ul>
     */
    @JsonProperty("filters")
    private Map<String, Object> filters;

    @JsonProperty(value = "created_at", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime createdAt;

    @JsonProperty("external_reference_id")
    private String externalReferenceId;

    public void setExportType(ExportType exportType) {
        this.exportType = markDirty("exportType", exportType);
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = markDirty("callbackUrl", callbackUrl);
    }

    public void setCallbackMethod(CallbackMethod callbackMethod) {
        this.callbackMethod = markDirty("callbackMethod", callbackMethod);
    }

    public void setFilters(Map<String, Object> filters) {
        this.filters = markDirty("filters", filters);
    }

    public void setExternalReferenceId(String externalReferenceId) {
        this.externalReferenceId = markDirty("externalReferenceId", externalReferenceId);
    }

    @JsonIgnore
    public boolean isPending() {
        return ExportStatus.PENDING.equals(status);
    }

    @JsonIgnore
    public boolean isProcessing() {
        return ExportStatus.PROCESSING.equals(status);
    }

    @JsonIgnore
    public boolean isCompleted() {
        return ExportStatus.COMPLETED.equals(status);
    }
}
