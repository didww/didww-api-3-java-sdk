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

    /**
     * Export filters. Dates are {@code "YYYY-MM-DD HH:MM:SS"} in UTC; list values are sent as JSON arrays.
     * <ul>
     *   <li>Every export: {@code from} (inclusive) and {@code to} (exclusive).</li>
     *   <li>{@code cdr_in}: {@code did_number}, {@code source_number}, {@code source_number_out},
     *       {@code source_country_iso}.</li>
     *   <li>{@code cdr_out}: {@code voice_out_trunk.id}, {@code source}, {@code cli}, {@code destination}.</li>
     *   <li>{@code sms_in} and {@code sms_out}: {@code source_address_eq}, {@code source_address_cont},
     *       {@code destination_address_eq}, {@code destination_address_cont}, {@code status}, {@code sms_id},
     *       {@code sms_trunk.id} (a string or a list).</li>
     *   <li>{@code sms_in} only: {@code trunk_type} (a string or a list, see
     *       {@link com.didww.sdk.resource.enums.ExportTrunkType}).</li>
     *   <li>{@code sms_out} only: {@code service_type} ({@code a2p} or {@code p2p}) and
     *       {@code sender_id_verification.id} (a string or a list).</li>
     *   <li>Read back only: {@code year}, {@code month} and {@code day}, on exports created at earlier API versions.</li>
     * </ul>
     *
     * @param filters the filters
     */
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
