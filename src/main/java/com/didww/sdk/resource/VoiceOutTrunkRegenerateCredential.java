package com.didww.sdk.resource;

import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;

@Type("voice_out_trunk_regenerate_credentials")
public class VoiceOutTrunkRegenerateCredential extends BaseResource {

    public static VoiceOutTrunkRegenerateCredential build(String id) {
        return BaseResource.build(VoiceOutTrunkRegenerateCredential.class, id);
    }

    @Relationship("voice_out_trunk")
    private VoiceOutTrunk voiceOutTrunk;

    public VoiceOutTrunk getVoiceOutTrunk() {
        return voiceOutTrunk;
    }

    public void setVoiceOutTrunk(VoiceOutTrunk voiceOutTrunk) {
        this.voiceOutTrunk = voiceOutTrunk;
    }
}
