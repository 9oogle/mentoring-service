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

public class ReadScenario {

    private final SimulationConfig cfg;

    public ReadScenario(SimulationConfig cfg) {
        this.cfg = cfg;
    }

    public PopulationBuilder population() {
        int vus  = cfg.vusFor("read", 0.6);
        int half = Math.max(1, vus / 2);

        Iterator<Map<String, Object>> idFeeder = Stream
                .generate(() -> Map.<String, Object>of("mentoringId", cfg.readMentoringId))
                .iterator();

        ScenarioBuilder scn = scenario("읽기 (목록/상세)")
                .exec(session -> session.set("doDetail", ThreadLocalRandom.current().nextDouble() >= 0.7))
                .doIf(s -> !(boolean) s.get("doDetail")).then(
                        exec(http("GET 멘토링 목록")
                                .get("/api/v1/mentorings")
                                .queryParam("page", "0")
                                .queryParam("size", "10")
                                .check(status().is(200)))
                )
                .doIf(s -> (boolean) s.get("doDetail")).then(
                        feed(idFeeder).exec(
                                http("GET 멘토링 상세")
                                        .get("/api/v1/mentorings/#{mentoringId}")
                                        .check(status().is(200)))
                )
                .pause(Duration.ofMillis(100));

        return scn.injectClosed(
                rampConcurrentUsers(0).to(half).during(Duration.ofSeconds(20)),
                constantConcurrentUsers(vus).during(Duration.ofSeconds(60)),
                rampConcurrentUsers(vus).to(0).during(Duration.ofSeconds(20))
        );
    }
}