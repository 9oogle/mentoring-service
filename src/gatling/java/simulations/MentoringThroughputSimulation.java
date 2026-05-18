package simulations;

import config.SimulationConfig;
import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import scenarios.*;
import setup.TestDataSetup;
import util.SimLog;

import java.util.ArrayList;
import java.util.List;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class MentoringThroughputSimulation extends Simulation {

    private final SimulationConfig cfg = new SimulationConfig();

    {
        new TestDataSetup(cfg).prepare();
    }

    HttpProtocolBuilder httpProtocol = http
            .baseUrls(cfg.baseUrls)
            .acceptHeader("application/json");

    {
        List<PopulationBuilder> populations = new ArrayList<>();

        boolean isStress = cfg.isActive("stress");

        if (isStress) {
            populations.add(new ReadScenario(cfg).stressPopulation());
            populations.add(new WriteScenario(cfg).stressPopulation());
            populations.add(new SearchScenario(cfg).stressPopulation());
        } else {
            if (cfg.isActive("read"))   populations.add(new ReadScenario(cfg).population());
            if (cfg.isActive("write"))  populations.add(new WriteScenario(cfg).population());
            if (cfg.isActive("search")) populations.add(new SearchScenario(cfg).population());
            if (cfg.isActive("lock"))   populations.add(new LockScenario(cfg).population());
        }

        if (populations.isEmpty()) {
            throw new IllegalArgumentException(
                    "유효한 시나리오 없음. -Dscenarios=read,write,search,lock,stress 중 하나 이상 지정하세요.");
        }

        if (isStress) {
            SimLog.info("stress 테스트 시작: %dVU씩 %d단계, 단계당 %d초 유지 → 최대 %dVU",
                    cfg.stressStepVUs, cfg.stressSteps, cfg.stressStepSecs,
                    cfg.stressStepVUs * cfg.stressSteps);
        }
        SimLog.info("시나리오 %d개 시작: %s", populations.size(), cfg.activeScenarios);

        setUp(populations.toArray(new PopulationBuilder[0]))
                .protocols(httpProtocol)
                .assertions(
                        // stress 모드: 리포트 완성이 목표이므로 임계값을 높게 설정
                        global().responseTime().percentile(95).lt(isStress ? 30_000 : 5_000),
                        global().failedRequests().percent().lt(isStress ? 50.0 : 5.0)
                );
    }
}