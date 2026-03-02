package com.didww.sdk.converter;

import com.didww.sdk.resource.BaseResource;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;

public class DirtyNullFieldFilter extends SimpleBeanPropertyFilter {

    @Override
    public void serializeAsField(Object pojo, JsonGenerator jgen,
                                 SerializerProvider provider, PropertyWriter writer) throws Exception {
        if (!(pojo instanceof BaseResource)) {
            writer.serializeAsField(pojo, jgen, provider);
            return;
        }

        BaseResource resource = (BaseResource) pojo;
        boolean dirtyOnlyMode = DirtySerializationContext.isDirtyOnlyModeEnabled();
        boolean dirty = resource.isFieldDirty(writer.getName());

        if (dirtyOnlyMode && !dirty) {
            if (!jgen.canOmitFields()) {
                writer.serializeAsOmittedField(pojo, jgen, provider);
            }
            return;
        }

        Object value = writer.getMember() == null ? null : writer.getMember().getValue(pojo);
        if (value != null) {
            writer.serializeAsField(pojo, jgen, provider);
            return;
        }

        if (dirty) {
            jgen.writeFieldName(writer.getName());
            provider.defaultSerializeNull(jgen);
            return;
        }

        if (!jgen.canOmitFields()) {
            writer.serializeAsOmittedField(pojo, jgen, provider);
        }
    }
}
