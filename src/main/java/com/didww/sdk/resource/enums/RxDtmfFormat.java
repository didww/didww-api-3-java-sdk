package com.didww.sdk.resource.enums;

public enum RxDtmfFormat implements IntEnum {
    RFC_2833(1),
    SIP_INFO(2),
    RFC_2833_OR_SIP_INFO(3);

    RxDtmfFormat(int value) { IntEnum.register(this, value); }
}
