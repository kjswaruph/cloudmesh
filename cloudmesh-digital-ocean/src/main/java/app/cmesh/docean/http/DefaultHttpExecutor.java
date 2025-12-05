package app.cmesh.docean.http;

import app.cmesh.docean.DigitalOceanConfig;
import app.cmesh.docean.internal.DoApiException;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;
import tools.jackson.databind.ObjectMapper;

public class DefaultHttpExecutor implements HttpExecutor {

    private final DigitalOceanConfig config;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    public DefaultHttpExecutor(DigitalOceanConfig config, HttpClient httpClient) {
        this.config = config;
        this.httpClient = httpClient;
        this.mapper = new ObjectMapper();
    }

    @Override
    public <T> T get(String path, Map<String, String> queryParams, Class<T> responseType) {
        URI uri = buildUri(path, queryParams);
        HttpRequest request = baseRequest(uri).GET().build();
        return execute(request, responseType);
    }

    @Override
    public <T> T post(String path, Object body, Class<T> responseType) {
        URI uri = buildUri(path, Map.of());
        byte[] requestBody = writeBody(body);
        HttpRequest request = baseRequest(uri)
                .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
                .header("Content-Type", "application/json")
                .build();
        return execute(request, responseType);
    }

    @Override
    public <T> T delete(String path, Class<T> responseType) {
        URI uri = buildUri(path, Map.of());
        HttpRequest request = baseRequest(uri).DELETE().build();
        return execute(request, responseType);
    }

    private HttpRequest.Builder baseRequest(URI uri) {
        return HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "Bearer " + config.apiToken())
                .header("Accept", "application/json");
    }

    private URI buildUri(String path, Map<String, String> queryParams) {
        String qp = "";
        if (!queryParams.isEmpty()) {
            qp = queryParams.entrySet().stream()
                    .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
                    .collect(Collectors.joining("&"));
        }
        String full = config.baseUrl() + path + (qp.isEmpty() ? "" : "?" + qp);
        return URI.create(full);
    }

    private String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private byte[] writeBody(Object body) {
        return mapper.writeValueAsBytes(body);
    }

    private <T> T execute(HttpRequest request, Class<T> responseType) {
        try {
            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int status = resp.statusCode();
            String body = resp.body();

            if (status >= 200 && status < 300) {
                if (responseType == Void.class) {
                    return null;
                }
                return mapper.readValue(body, responseType);
            } else {
                throw DoApiException.from(status, body);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("HTTP call failed", e);
        }
    }
}
