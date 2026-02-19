package com.didww.sdk.resource.enums;

public enum TxDtmfFormat implements IntEnum {
    DISABLED(1),
    RFC_2833(2),
    SIP_INFO_RELAY(3),
    SIP_INFO_DTMF(4);

    TxDtmfFormat(int value) { IntEnum.register(this, value); }
}
