package app.cmesh.controller;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
public class HelloController {

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        Map<String, Object> res = new HashMap<>();
        res.put("ok", true);
        res.put("ts", Instant.now().toString());
        res.put("service", "gateway-test");
        return res;
    }
}
