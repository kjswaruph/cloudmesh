package app.cmesh.docean.impl;

import app.cmesh.docean.api.RegionService;
import app.cmesh.docean.http.HttpExecutor;
import app.cmesh.docean.model.Region;
import app.cmesh.docean.model.RegionsResponse;
import java.util.List;
import java.util.Map;

public class RegionServiceImpl implements RegionService {

    private final HttpExecutor http;

    public RegionServiceImpl(HttpExecutor http) {
        this.http = http;
    }

    @Override
    public List<Region> list() {
        return http.get("/regions", Map.of(), RegionsResponse.class).regions();
    }
}
