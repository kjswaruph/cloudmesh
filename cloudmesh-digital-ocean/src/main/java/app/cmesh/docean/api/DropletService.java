package app.cmesh.docean.api;

import app.cmesh.docean.model.Droplet;
import java.util.List;
import java.util.Optional;

public interface DropletService {

    Droplet createDroplet(Droplet droplet);
    Optional<Droplet> getDroplet(long id);
    List<Droplet> list();
    void powerOn(long id);
    void powerOff(long id);
    void deleteDroplet(long id);
}
