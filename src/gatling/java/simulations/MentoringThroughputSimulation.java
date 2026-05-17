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

        if (cfg.isActive("read"))   populations.add(new ReadScenario(cfg).population());

        if (populations.isEmpty()) {
            throw new IllegalArgumentException(
                    "유효한 시나리오 없음. -Dscenarios=read,write,search,lock 중 하나 이상 지정하세요.");
        }

        SimLog.info("시나리오 %d개 시작: %s", populations.size(), cfg.activeScenarios);

        setUp(populations.toArray(new PopulationBuilder[0]))
                .protocols(httpProtocol)
                .assertions(
                        global().responseTime().percentile(95).lt(5000),
                        global().failedRequests().percent().lt(5.0)
                );
    }
}