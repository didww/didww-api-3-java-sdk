package com.didww.sdk.resource.enums;

public enum SstRefreshMethod implements IntEnum {
    INVITE(1),
    UPDATE(2),
    UPDATE_FALLBACK_INVITE(3);

    SstRefreshMethod(int value) { IntEnum.register(this, value); }
}
