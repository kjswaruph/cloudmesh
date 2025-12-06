package app.cmesh.docean.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public record Size(
        String slug,
        int memory,
        int vcpus,
        int disk,
        double transfer,
        @JsonProperty("price_monthly") BigDecimal priceMonthly,
        @JsonProperty("price_hourly") BigDecimal priceHourly,
        List<String> regions,
        boolean available
) {
}
