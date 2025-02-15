package com.example.receiptprocessor.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;

public class TestUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Loads JSON data from a file in the classpath and converts it to an object of the specified type.
     */
    public static <T> T loadJson(String filename, Class<T> clazz) throws Exception {
        InputStream is = TestUtils.class.getClassLoader().getResourceAsStream(filename);
        return objectMapper.readValue(is, clazz);
    }
}
