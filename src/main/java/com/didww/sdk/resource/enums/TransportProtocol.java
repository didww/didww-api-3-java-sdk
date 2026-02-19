package com.didww.sdk.resource.enums;

public enum TransportProtocol implements IntEnum {
    UDP(1),
    TCP(2),
    TLS(3);

    TransportProtocol(int value) { IntEnum.register(this, value); }
}
