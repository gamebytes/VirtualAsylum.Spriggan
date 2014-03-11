package org.virtualAsylum.spriggan.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

import java.io.IOException;

/**
 * Created by Morgan on 11/03/14.
 */
public class JavaFXSerializer<PropertyContentType, PropertyType extends Property<PropertyContentType>>
    extends JsonSerializer<PropertyType>{

    @Override
    public void serialize(PropertyType propertyType, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        jsonGenerator.writeObject(propertyType.getValue());
    }

    public static class String extends JavaFXSerializer<java.lang.String, SimpleStringProperty>{}

    public static class Boolean extends JavaFXSerializer<java.lang.Boolean, SimpleBooleanProperty>{}
}
