package scenarios;

import config.SimulationConfig;
import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;

import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class SearchScenario {

    private static final String[] KEYWORDS = {
            "java", "spring", "python", "react", "docker",
            "aws", "system design", "typescript", "msa", "algorithm"
    };

    private final SimulationConfig cfg;

    public SearchScenario(SimulationConfig cfg) {
        this.cfg = cfg;
    }

    public PopulationBuilder population() {
        int vus  = cfg.vusFor("search", 0.3);
        int half = Math.max(1, vus / 2);

        return buildScn().injectClosed(
                rampConcurrentUsers(0).to(half).during(Duration.ofSeconds(20)),
                constantConcurrentUsers(vus).during(Duration.ofSeconds(60)),
                rampConcurrentUsers(vus).to(0).during(Duration.ofSeconds(20))
        );
    }

    public PopulationBuilder stressPopulation() {
        int step = Math.max(1, (int) (cfg.stressStepVUs * 0.2));
        return buildScn().injectClosed(
                incrementConcurrentUsers(step)
                        .times(cfg.stressSteps)
                        .eachLevelLasting(Duration.ofSeconds(cfg.stressStepSecs))
                        .separatedByRampsLasting(Duration.ofSeconds(5))
                        .startingFrom(step)
        );
    }

    private ScenarioBuilder buildScn() {
        Iterator<Map<String, Object>> keywordFeeder = Stream
                .generate(() -> {
                    String kw = KEYWORDS[ThreadLocalRandom.current().nextInt(KEYWORDS.length)];
                    return Map.<String, Object>of("keyword", kw);
                })
                .iterator();
        return scenario("키워드 검색")
                .feed(keywordFeeder)
                .exec(http("GET 키워드 검색")
                        .get("/api/v1/mentorings")
                        .queryParam("keyword", "#{keyword}")
                        .queryParam("page", "0")
                        .queryParam("size", "20")
                        .check(status().is(200)))
                .pause(Duration.ofMillis(50));
    }
}