package com.didww.sdk.resource;

import java.util.LinkedHashMap;

/**
 * JSON:API {@code meta} with string values. Do not replace with a {@code Map<String, String>} or
 * {@code HashMap<String, String>} field: jsonapi-converter deserializes by {@code field.getType()},
 * which drops the type arguments, so Jackson would store JSON numbers as {@code Integer} and the
 * accessors would throw {@link ClassCastException}. Only a named class carries the value type
 * where Jackson can still read it.
 */
public class MetaMap extends LinkedHashMap<String, String> {
}
