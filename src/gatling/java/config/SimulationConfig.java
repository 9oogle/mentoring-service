package config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SimulationConfig {

    public static final String MENTOR_ID = "00000000-0000-0000-0000-000000000001";

    public final String[]    baseUrls;
    public final String      baseUrl;
    public final int         peakVUs;
    public final Set<String> activeScenarios;

    public final int stressStepVUs;
    public final int stressSteps;
    public final int stressStepSecs;

    public String categoryId;
    public String readMentoringId;
    public String lockMentoringId;

    public SimulationConfig() {
        this.baseUrls = Arrays.stream(
                System.getProperty("baseUrls", System.getProperty("baseUrl", "http://localhost:8081")).split(",")
        ).map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);
        this.baseUrl         = baseUrls[0];
        this.peakVUs         = Integer.parseInt(System.getProperty("peakVUs", "50"));
        this.activeScenarios = new HashSet<>(
                Arrays.asList(System.getProperty("scenarios", "read,write,lock").split(","))
        );
        this.stressStepVUs   = Integer.parseInt(System.getProperty("stressStepVUs",  "20"));
        this.stressSteps     = Integer.parseInt(System.getProperty("stressSteps",    "10"));
        this.stressStepSecs  = Integer.parseInt(System.getProperty("stressStepSecs", "30"));
    }

    public boolean isActive(String name) {
        return activeScenarios.contains(name);
    }

    /** 해당 시나리오만 단독 실행 중이면 peakVUs 전체, 아니면 fraction 비율 */
    public int vusFor(String name, double fraction) {
        boolean only = activeScenarios.size() == 1 && activeScenarios.contains(name);
        return only ? peakVUs : Math.max(1, (int) (peakVUs * fraction));
    }
}
