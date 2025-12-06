package app.cmesh.docean.impl;

import app.cmesh.docean.api.DropletService;
import app.cmesh.docean.http.HttpExecutor;
import app.cmesh.docean.model.Droplet;
import app.cmesh.docean.model.DropletsResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DropletServiceImpl implements DropletService {

    private HttpExecutor http;

    public DropletServiceImpl(HttpExecutor http) {
        this.http = http;
    }

    @Override
    public Droplet createDroplet(Droplet droplet) {
        return http.post("/droplets", droplet, DropletsResponse.class).droplet();
    }

    @Override
    public Optional<Droplet> getDroplet(long id) {
        try{
            return Optional.ofNullable(http.get("/droplets/"+id, Map.of(), DropletsResponse.class).droplet());
        }catch (Exception e){
            return Optional.empty();
        }
    }

    @Override
    public List<Droplet> list() {
        return http.get("/droplets", Map.of(), DropletsResponse.class).droplets();
    }

    @Override
    public void powerOn(long id) {
        http.post("/droplets/"+id+"/actions", Map.of("type", "power_on"),Void.class);
    }

    @Override
    public void powerOff(long id) {
        http.post("/droplets/"+id+"/actions",Map.of("type", "power_off"),Void.class);
    }

    @Override
    public void deleteDroplet(long id) {
        http.delete("/droplets/" + id, Void.class);
    }
}
