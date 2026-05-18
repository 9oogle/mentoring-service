package scenarios;

import config.SimulationConfig;
import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;

import java.time.Duration;
import java.util.UUID;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class LockScenario {

    private final SimulationConfig cfg;

    public LockScenario(SimulationConfig cfg) {
        this.cfg = cfg;
    }

    public PopulationBuilder population() {
        int burst = cfg.vusFor("lock", 0.6);

        ScenarioBuilder scn = scenario("분산락 동시 예약")
                .exec(session -> session
                        .set("menteeId", UUID.randomUUID().toString())
                        .set("orderId",  UUID.randomUUID().toString())
                )
                .exec(http("POST 예약 생성 (분산락)")
                        .post("/api/v1/mentoring-bookings")
                        .header("Content-Type", "application/json")
                        .header("X-User-Id",   "#{menteeId}")
                        .header("X-User-Role", "STUDENT")
                        .header("X-User-Name", "Gatling멘티")
                        .body(StringBody(s -> """
                                {"mentoringId":"%s",
                                "timeSlots":[{"date":"2027-08-01","startTime":"10:00:00","endTime":"11:00:00"}],
                                "requestMessage":"Gatling 분산락 테스트",
                                "orderId":"%s"}
                                """.formatted(cfg.lockMentoringId, s.get("orderId"))
                        ))
                        // 201(예약 성공) 또는 409(락 충돌/슬롯 선점) 모두 정상
                        .check(status().in(201, 409))
                );

        return scn.injectOpen(
                nothingFor(Duration.ofSeconds(25)),
                atOnceUsers(burst),
                nothingFor(Duration.ofSeconds(75))
        );
    }
}