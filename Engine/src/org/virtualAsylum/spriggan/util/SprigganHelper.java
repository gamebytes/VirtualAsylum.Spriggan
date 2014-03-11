package org.virtualAsylum.spriggan.util;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;

import java.io.*;

/**
 * Created by Morgan on 11/03/14.
 */
public class SprigganHelper {
    //JSON
    static final ObjectMapper mapper = new ObjectMapper()
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(SerializationFeature.INDENT_OUTPUT, true);

    public static <T extends Object> T jsonRead(File file, Class<T> cls) {
        try {
            return mapper.readValue(file, cls);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    public static <T extends Object> T jsonRead(String path, Class<T> cls) {
        File file = new File(path);
        return jsonRead(file, cls);
    }
    public static void jsonWrite(File file, Object obj) {
        try {
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(file, obj);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    public static void jsonWrite(String path, Object obj) {
        File file = new File(path);
        jsonWrite(file, obj);
    }
    public static String jsonSerialize(Object obj){
        try {
            return mapper.writer().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
    public static <T extends Object> T jsonDeserialize(String string, Class<T> cls){
        try {
            return mapper.readValue(string, cls);
        } catch (IOException e) {
            return null;
        }
    }
}
