package app.cmesh.docean.http;

import java.util.Map;

public interface HttpExecutor {

    <T> T get(String path, Map<String, String> queryParams, Class<T> responseType);

    <T> T post(String path, Object body, Class<T> responseType);

    <T> T delete(String path, Class<T> responseType);
}
