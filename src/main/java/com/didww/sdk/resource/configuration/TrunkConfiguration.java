package com.didww.sdk.resource.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class TrunkConfiguration {

    private static final Map<String, Class<? extends TrunkConfiguration>> TYPE_MAP;

    static {
        Map<String, Class<? extends TrunkConfiguration>> map = new HashMap<>();
        map.put("sip_configurations", SipConfiguration.class);
        map.put("h323_configurations", H323Configuration.class);
        map.put("iax2_configurations", Iax2Configuration.class);
        map.put("pstn_configurations", PstnConfiguration.class);
        TYPE_MAP = Collections.unmodifiableMap(map);
    }

    @JsonIgnore
    public abstract String getType();

    public static Map<String, Class<? extends TrunkConfiguration>> getTypeMap() {
        return TYPE_MAP;
    }
}
