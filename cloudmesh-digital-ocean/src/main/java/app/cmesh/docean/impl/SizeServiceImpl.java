package app.cmesh.docean.impl;

import app.cmesh.docean.api.SizeService;
import app.cmesh.docean.http.HttpExecutor;
import app.cmesh.docean.model.Size;
import app.cmesh.docean.model.SizesResponse;
import java.util.List;
import java.util.Map;

public class SizeServiceImpl implements SizeService {
    private final HttpExecutor http;

    public SizeServiceImpl(HttpExecutor http) {
        this.http = http;
    }

    @Override
    public List<Size> list() {
        return http.get("/sizes", Map.of(), SizesResponse.class).sizes();
    }
}
