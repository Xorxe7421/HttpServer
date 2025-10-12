package org.pavl.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BodySerializer extends JsonSerializer<Object> {

    @Override
    public void serialize(Object object, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (object != null) {
            jsonGenerator.writeString(new String((byte[]) object, StandardCharsets.UTF_8));
        } else {
            jsonGenerator.writeNull();
        }
    }
}
