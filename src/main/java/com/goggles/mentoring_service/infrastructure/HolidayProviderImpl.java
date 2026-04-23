package com.goggles.mentoring_service.infrastructure;

import com.goggles.mentoring_service.domain.mentoring.HolidayProvider;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Set;

@Component
public class HolidayProviderImpl implements HolidayProvider {

    // 양력 고정 공휴일 (음력 기반 공휴일은 추후 외부 API 연동 시 추가)
    private static final Set<MonthDay> FIXED_HOLIDAYS = Set.of(
            MonthDay.of(1, 1),   // 새해
            MonthDay.of(3, 1),   // 삼일절
            MonthDay.of(5, 5),   // 어린이날
            MonthDay.of(6, 6),   // 현충일
            MonthDay.of(8, 15),  // 광복절
            MonthDay.of(10, 3),  // 개천절
            MonthDay.of(10, 9),  // 한글날
            MonthDay.of(12, 25)  // 크리스마스
    );

    @Override
    public boolean isHoliday(LocalDate date) {
        return FIXED_HOLIDAYS.contains(MonthDay.from(date));
    }
}