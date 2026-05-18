package scenarios;

import config.SimulationConfig;
import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class WriteScenario {

    private final SimulationConfig cfg;

    public WriteScenario(SimulationConfig cfg) {
        this.cfg = cfg;
    }

    public PopulationBuilder population() {
        int vus  = cfg.vusFor("write", 0.2);
        int half = Math.max(1, vus / 2);

        ScenarioBuilder scn = scenario("멘토링 생성")
                .exec(session -> {
                    int month = ThreadLocalRandom.current().nextInt(12) + 1;
                    return session
                            .set("createTitle", "[Gatling Write] " + System.nanoTime())
                            .set("createDate",  String.format("2032-%02d-01", month));
                })
                .exec(http("POST 멘토링 생성")
                        .post("/api/v1/mentorings")
                        .header("Content-Type", "application/json")
                        .header("X-User-Id",   SimulationConfig.MENTOR_ID)
                        .header("X-User-Role", "INSTRUCTOR")
                        .header("X-User-Name", "Gatling멘토")
                        .body(StringBody(s -> """
                                {"categoryId":"%s","title":"%s","duration":"MINUTES_60",
                                "mentoringType":"ONE_ON_ONE","format":"SINGLE","sessionCount":1,
                                "maxParticipants":1,"excludeHolidays":false,"price":0,
                                "sessions":[{"date":"%s","startTime":"09:00:00","endTime":"10:00:00"}],
                                "repeatPatterns":[{"dayOfWeek":"WEDNESDAY","startTime":"09:00:00","endTime":"10:00:00"}]}
                                """.formatted(cfg.categoryId, s.get("createTitle"), s.get("createDate"))
                        ))
                        .check(status().is(201))
                )
                .pause(Duration.ofMillis(300));

        return scn.injectClosed(
                rampConcurrentUsers(0).to(half).during(Duration.ofSeconds(20)),
                constantConcurrentUsers(vus).during(Duration.ofSeconds(60)),
                rampConcurrentUsers(vus).to(0).during(Duration.ofSeconds(20))
        );
    }
}