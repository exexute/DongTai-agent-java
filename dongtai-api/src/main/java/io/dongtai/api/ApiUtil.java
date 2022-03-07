package io.dongtai.api;

public class ApiUtil {
    public static boolean allowedContentType(String contentType) {
        return contentType != null && (contentType.contains("application/json")
                || contentType.contains("application/xml"));
    }
}
