package com.didww.sdk.converter;

import com.fasterxml.jackson.annotation.JsonFilter;

@JsonFilter("dirtyNullFilter")
abstract class DirtyNullFilterMixIn {
}
