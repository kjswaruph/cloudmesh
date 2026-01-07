package app.cmesh.aws;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.costexplorer.CostExplorerClient;
import software.amazon.awssdk.services.costexplorer.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
public class AwsCostService {

    private final StsService stsService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public AwsCostService(StsService stsService) {
        this.stsService = stsService;
    }

    public List<DailyCostEntry> getCostAndUsage(
            ConnectedAwsAccount account,
            LocalDate startDate,
            LocalDate endDate) {
        log.info("Fetching AWS costs for {} from {} to {}",
                account.roleArn(), startDate, endDate);

        try (CostExplorerClient costExplorer = CostExplorerClient.builder()
                .credentialsProvider(stsService.credentialsFor(account))
                .region(software.amazon.awssdk.regions.Region.US_EAST_1) // Cost Explorer only in us-east-1
                .build()) {

            GetCostAndUsageRequest request = GetCostAndUsageRequest.builder()
                    .timePeriod(DateInterval.builder()
                            .start(startDate.format(DATE_FORMATTER))
                            .end(endDate.format(DATE_FORMATTER))
                            .build())
                    .granularity(Granularity.DAILY)
                    .metrics("UnblendedCost")
                    .groupBy(
                            GroupDefinition.builder()
                                    .type(GroupDefinitionType.DIMENSION)
                                    .key("SERVICE")
                                    .build())
                    .build();

            GetCostAndUsageResponse response = costExplorer.getCostAndUsage(request);

            return parseCostResponse(response);

        } catch (Exception e) {
            log.error("Failed to fetch AWS costs: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch AWS costs: " + e.getMessage(), e);
        }
    }

    private List<DailyCostEntry> parseCostResponse(GetCostAndUsageResponse response) {
        List<DailyCostEntry> entries = new ArrayList<>();

        for (ResultByTime resultByTime : response.resultsByTime()) {
            LocalDate date = LocalDate.parse(resultByTime.timePeriod().start(), DATE_FORMATTER);

            for (Group group : resultByTime.groups()) {
                String service = group.keys().get(0);
                String amountStr = group.metrics().get("UnblendedCost").amount();
                BigDecimal amount = new BigDecimal(amountStr);
                String currency = group.metrics().get("UnblendedCost").unit();

                if (amount.compareTo(BigDecimal.ZERO) > 0) {
                    entries.add(new DailyCostEntry(
                            date,
                            service,
                            amount,
                            currency,
                            new HashMap<>()
                    ));
                }
            }
        }

        log.info("Parsed {} cost entries", entries.size());
        return entries;
    }

    public record DailyCostEntry(
            LocalDate date,
            String service,
            BigDecimal amount,
            String currency,
            Map<String, String> tags) {
    }
}
