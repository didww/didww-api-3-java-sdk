package com.didww.sdk.resource.enums;

public enum Codec implements IntEnum {
    TELEPHONE_EVENT(6),
    G723(7),
    G729(8),
    PCMU(9),
    PCMA(10),
    SPEEX(12),
    GSM(13),
    G726_32(14),
    G721(15),
    G726_24(16),
    G726_40(17),
    G726_16(18),
    L16(19);

    Codec(int value) { IntEnum.register(this, value); }
}
