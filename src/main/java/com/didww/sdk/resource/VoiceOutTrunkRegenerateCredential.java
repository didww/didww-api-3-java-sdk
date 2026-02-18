package com.didww.sdk.resource;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;

@Type("voice_out_trunk_regenerate_credentials")
public class VoiceOutTrunkRegenerateCredential implements HasId {

    @Id
    private String id;

    @Relationship("voice_out_trunk")
    private VoiceOutTrunk voiceOutTrunk;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public VoiceOutTrunk getVoiceOutTrunk() {
        return voiceOutTrunk;
    }

    public void setVoiceOutTrunk(VoiceOutTrunk voiceOutTrunk) {
        this.voiceOutTrunk = voiceOutTrunk;
    }
}
