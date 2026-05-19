package setup;

import config.SimulationConfig;
import util.HttpHelper;
import util.SimLog;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class TestDataSetup {

    private final SimulationConfig cfg;

    public TestDataSetup(SimulationConfig cfg) {
        this.cfg = cfg;
    }

    public void prepare() {
        SimLog.setup("테스트 데이터 준비 중...");

        cfg.categoryId      = prop("categoryId",      this::findOrCreateCategory);
        cfg.readMentoringId = prop("mentoringId",     () -> createMentoring("[Gatling 읽기]",   "2031-06-01"));
        cfg.lockMentoringId = prop("lockMentoringId", () -> createMentoring("[Gatling 분산락]", "2027-08-01"));

        SimLog.setup("categoryId:      %s", cfg.categoryId);
        SimLog.setup("readMentoringId: %s", cfg.readMentoringId);
        SimLog.setup("lockMentoringId: %s (슬롯: 2027-08-01 10:00)", cfg.lockMentoringId);
        SimLog.setup("대상 서버 (%d대): %s", cfg.baseUrls.length, String.join(", ", cfg.baseUrls));
    }

    private String prop(String key, Supplier<String> fallback) {
        String v = System.getProperty(key, "").strip();
        return v.isEmpty() ? fallback.get() : v;
    }

    private String findOrCreateCategory() {
        Map<String, String> headers = adminHeaders();
        String body = HttpHelper.get(cfg.baseUrl + "/api/v1/admin/mentoring-categories", headers);
        String id   = HttpHelper.extractFirstInArray(body, "categories", "categoryId");
        if (!id.isEmpty()) return id;

        Map<String, String> h = new HashMap<>(headers);
        h.put("Content-Type", "application/json");
        String resp = HttpHelper.post(
                cfg.baseUrl + "/api/v1/admin/mentoring-categories",
                """
                {"title":"Gatling 부하테스트","code":"GATLING_TEST","sortOrder":96}
                """,
                h
        );
        id = HttpHelper.extractField(resp, "categoryId");
        if (id.isEmpty()) throw new RuntimeException("카테고리 생성 실패: " + resp);
        return id;
    }

    private String createMentoring(String title, String date) {
        String body = """
                {"categoryId":"%s","title":"%s","duration":"MINUTES_60",
                "mentoringType":"ONE_ON_ONE","format":"SINGLE","sessionCount":1,
                "maxParticipants":1,"excludeHolidays":false,"price":0,
                "sessions":[{"date":"%s","startTime":"10:00:00","endTime":"11:00:00"}],
                "repeatPatterns":[{"dayOfWeek":"TUESDAY","startTime":"10:00:00","endTime":"11:00:00"}]}
                """.formatted(cfg.categoryId, title, date);
        String resp = HttpHelper.post(cfg.baseUrl + "/api/v1/mentorings", body, instructorHeaders());
        String id   = HttpHelper.extractField(resp, "mentoringId");
        if (id.isEmpty()) throw new RuntimeException("멘토링 생성 실패: " + resp);
        return id;
    }

    private Map<String, String> adminHeaders() {
        return Map.of(
                "X-User-Id",   SimulationConfig.MENTOR_ID,
                "X-User-Role", "MASTER",
                "X-User-Name", "Setup"
        );
    }

    private Map<String, String> instructorHeaders() {
        return Map.of(
                "Content-Type", "application/json",
                "X-User-Id",    SimulationConfig.MENTOR_ID,
                "X-User-Role",  "INSTRUCTOR",
                "X-User-Name",  "Setup"
        );
    }
}