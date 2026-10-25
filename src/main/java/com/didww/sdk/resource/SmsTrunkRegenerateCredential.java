package com.didww.sdk.resource;

import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

/**
 * Regenerates the credentials of an {@code smpp_esme} or {@code http_out} SMS trunk (API 2026-10-22). Create it
 * with {@code new SmsTrunkRegenerateCredential()} (not {@code withId}) and read the new credentials back from the
 * trunk.
 */
@Type("sms_trunk_regenerate_credentials")

@Getter
public class SmsTrunkRegenerateCredential extends BaseResource {

    @Relationship("sms_trunk")
    private SmsTrunk smsTrunk;

    public void setSmsTrunk(SmsTrunk smsTrunk) {
        this.smsTrunk = markDirty("smsTrunk", smsTrunk);
    }
}
