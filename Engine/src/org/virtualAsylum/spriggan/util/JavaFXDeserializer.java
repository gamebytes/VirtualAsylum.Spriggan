package org.virtualAsylum.spriggan.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

import java.io.IOException;

/**
 * Created by Morgan on 11/03/14.
 */
public abstract class JavaFXDeserializer<PropertyContentType, PropertyType extends Property<PropertyContentType>>
    extends JsonDeserializer<PropertyType> {

    @Override
    public abstract PropertyType deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException;

    public static class String extends JavaFXDeserializer<java.lang.String, SimpleStringProperty> {

        @Override
        public SimpleStringProperty deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            return new SimpleStringProperty(jsonParser.readValueAs(java.lang.String.class));
        }
    }

    public static class Boolean extends JavaFXDeserializer<java.lang.Boolean, SimpleBooleanProperty> {
        @Override
        public SimpleBooleanProperty deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            return new SimpleBooleanProperty(jsonParser.readValueAs(java.lang.Boolean.class));
        }
    }
}
