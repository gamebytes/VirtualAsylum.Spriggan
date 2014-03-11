package org.virtualAsylum.spriggan;

import com.fasterxml.jackson.databind.annotation.*;
import javafx.beans.property.SimpleBooleanProperty;
import org.virtualAsylum.spriggan.util.JavaFXDeserializer;
import org.virtualAsylum.spriggan.util.JavaFXSerializer;

/**
 * Created by Morgan on 11/03/14.
 */
public class Settings {
    @JsonDeserialize(using=JavaFXDeserializer.Boolean.class)
    @JsonSerialize(using= JavaFXSerializer.Boolean.class)
    public final SimpleBooleanProperty
            automaticUpdate = new SimpleBooleanProperty(this, "automaticUpdate", false);
}
