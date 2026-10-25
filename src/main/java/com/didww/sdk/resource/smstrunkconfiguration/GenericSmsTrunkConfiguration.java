package com.didww.sdk.resource.smstrunkconfiguration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Configuration of a type this SDK version does not know yet, or of a known type whose attributes must be
 * sent verbatim. The type and attributes are kept as received and written back unchanged, explicit nulls
 * included, so it also serves to clear nullable attributes (see the package documentation). Numbers keep
 * their exact value.
 */
public class GenericSmsTrunkConfiguration extends SmsTrunkConfiguration {

    private final String type;
    private final ObjectNode attributes;

    /**
     * Creates a configuration with no attributes.
     *
     * @param type the wire-level configuration type
     */
    public GenericSmsTrunkConfiguration(String type) {
        this(type, JsonNodeFactory.instance.objectNode());
    }

    /**
     * Creates a configuration with the given attributes. The node is kept, not copied.
     *
     * @param type the wire-level configuration type
     * @param attributes the attributes; a {@code NullNode} value is sent as {@code null}
     */
    public GenericSmsTrunkConfiguration(String type, ObjectNode attributes) {
        this.type = Objects.requireNonNull(type, "type");
        this.attributes = Objects.requireNonNull(attributes, "attributes");
    }

    @Override
    @JsonIgnore
    public String getType() {
        return type;
    }

    /**
     * Returns the attributes. The node is mutable; call {@code setConfiguration} on the trunk again after
     * changing it.
     *
     * @return the attributes
     */
    @JsonIgnore
    public ObjectNode getAttributes() {
        return attributes;
    }

    /**
     * Attribute values may carry credentials, so only the attribute names and explicit nulls are shown.
     * The wire payload is unaffected.
     */
    @Override
    public String toString() {
        List<String> names = new ArrayList<>();
        attributes.fieldNames().forEachRemaining(names::add);
        names.sort(null);
        StringBuilder sb = new StringBuilder("GenericSmsTrunkConfiguration(type=").append(type).append(", attributes={");
        for (int i = 0; i < names.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            String name = names.get(i);
            sb.append(name).append('=').append(attributes.get(name).isNull() ? "null" : "[FILTERED]");
        }
        return sb.append("})").toString();
    }
}
