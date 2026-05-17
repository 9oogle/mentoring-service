package util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public final class HttpHelper {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private HttpHelper() {}

    public static String post(String url, String body, Map<String, String> headers) {
        try {
            HttpRequest.Builder b = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(body.strip()));
            headers.forEach(b::header);
            return CLIENT.send(b.build(), HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            throw new RuntimeException("POST 실패: " + url, e);
        }
    }

    public static String get(String url, Map<String, String> headers) {
        try {
            HttpRequest.Builder b = HttpRequest.newBuilder()
                    .uri(URI.create(url)).GET();
            headers.forEach(b::header);
            return CLIENT.send(b.build(), HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            throw new RuntimeException("GET 실패: " + url, e);
        }
    }

    public static String extractField(String json, String field) {
        String marker = "\"" + field + "\":\"";
        int s = json.indexOf(marker);
        if (s < 0) return "";
        s += marker.length();
        int e = json.indexOf("\"", s);
        return e > s ? json.substring(s, e) : "";
    }

    public static String extractFirstInArray(String json, String arrayKey, String field) {
        int start = json.indexOf("\"" + arrayKey + "\":[");
        return start < 0 ? "" : extractField(json.substring(start), field);
    }
}