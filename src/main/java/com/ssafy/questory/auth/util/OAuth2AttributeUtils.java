package com.ssafy.questory.auth.util;

import java.util.Map;

public final class OAuth2AttributeUtils {
    private OAuth2AttributeUtils() {}

    @SuppressWarnings("unchecked")
    public static Map<String, Object> asMap(Object value) {
        if (value == null) return null;
        if (!(value instanceof Map)) {
            throw new IllegalArgumentException("Expected Map but got: " + value.getClass());
        }
        return (Map<String, Object>) value;
    }

    public static String asString(Object value) {
        if (value == null) return null;
        if (value instanceof String s) return s;
        if (value instanceof Number n) return String.valueOf(n);
        if (value instanceof Boolean b) return String.valueOf(b);
        return String.valueOf(value);
    }
}
