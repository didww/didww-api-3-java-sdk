package com.didww.sdk.resource;

import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

@Type("voice_out_trunk_regenerate_credentials")

@Getter
public class VoiceOutTrunkRegenerateCredential extends BaseResource {

    @Relationship("voice_out_trunk")
    private VoiceOutTrunk voiceOutTrunk;

    public void setVoiceOutTrunk(VoiceOutTrunk voiceOutTrunk) {
        this.voiceOutTrunk = markDirty("voiceOutTrunk", voiceOutTrunk);
    }
}
