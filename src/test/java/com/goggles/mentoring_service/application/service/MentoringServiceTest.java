package com.goggles.mentoring_service.application.service;

import com.goggles.common.pagination.CommonPageRequest;
import com.goggles.mentoring_service.application.command.MentoringCommand;
import com.goggles.mentoring_service.application.result.MentoringResult;
import com.goggles.mentoring_service.domain.category.MentoringCategory;
import com.goggles.mentoring_service.domain.category.MentoringCategoryId;
import com.goggles.mentoring_service.domain.category.exception.CategoryNotFoundException;
import com.goggles.mentoring_service.domain.category.repository.MentoringCategoryRepository;
import com.goggles.mentoring_service.domain.mentoring.*;
import com.goggles.mentoring_service.domain.mentoring.exception.MentoringNotFoundException;
import com.goggles.mentoring_service.domain.mentoring.repository.MentoringRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.goggles.mentoring_service.domain.mentoring.MentoringFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MentoringServiceTest {

	private static final Logger log = LoggerFactory.getLogger(MentoringServiceTest.class);

	@InjectMocks
	private MentoringService mentoringService;

	@Mock
	private MentoringRepository mentoringRepository;

	@Mock
	private MentoringCategoryRepository categoryRepository;

	@Mock
	private MentoringCategory category;

	@Mock
	private MentoringCategoryId mockCategoryId;

	@Test
	void createMentoring_success() {
		given(categoryRepository.findById(any())).willReturn(Optional.of(category));
		given(category.getMentoringCategoryId()).willReturn(mockCategoryId);
		given(mockCategoryId.categoryId()).willReturn(CATEGORY_ID);
		given(category.getName()).willReturn(CATEGORY_NAME);
		given(category.getCode()).willReturn(CATEGORY_CODE);

		UUID result = mentoringService.createMentoring(defaultCommand());

		assertThat(result).isNotNull();
		verify(mentoringRepository).save(any());
	}

	@Test
	void createMentoring_category_not_found() {
		given(categoryRepository.findById(any())).willReturn(Optional.empty());

		assertThatThrownBy(() -> mentoringService.createMentoring(defaultCommand())).isInstanceOf(
				CategoryNotFoundException.class);
	}

	@Test
	void getMentoring_success() {
		Mentoring mentoring = defaultMentoringBuilder().build();
		given(mentoringRepository.findById(any())).willReturn(Optional.of(mentoring));

		UUID id = mentoring.getMentoringId()
				.mentoringId();
		MentoringResult.Detail result = mentoringService.getMentoring(id);

		log.info("==== 멘토링 상세 조회 결과 ====");
		log.info("id       : {}", result.mentoringId());
		log.info("title    : {}", result.title());
		log.info("status   : {}", result.status());
		log.info("price    : {}원", result.price());
		log.info("mentor   : {} / {}", result.mentor()
				.name(), result.mentor()
				.field());
		log.info("category : {} ({})", result.category()
				.name(), result.category()
				.code());

		assertThat(result.mentoringId()).isEqualTo(id);
		assertThat(result.title()).isEqualTo(TITLE);
		assertThat(result.status()).isEqualTo(MentoringStatus.INACTIVE);
		assertThat(result.price()).isEqualTo(PRICE);
		assertThat(result.mentor()
				.name()).isEqualTo(MENTOR_NAME);
		assertThat(result.mentor()
				.field()).isEqualTo(MENTOR_FIELD);
		assertThat(result.category()
				.name()).isEqualTo(CATEGORY_NAME);
		assertThat(result.category()
				.code()).isEqualTo(CATEGORY_CODE);
	}

	@Test
	void getMentoring_not_found() {
		UUID unknownId = UUID.randomUUID();
		given(mentoringRepository.findById(any())).willReturn(Optional.empty());

		log.info("존재하지 않는 멘토링 조회 시도: {}", unknownId);
		assertThatThrownBy(() -> mentoringService.getMentoring(unknownId)).isInstanceOf(
				MentoringNotFoundException.class);
		log.info("MentoringNotFoundException 발생 확인");
	}

	@Test
	void getMentoringSchedules_success() {
		Mentoring mentoring = defaultMentoringBuilder().repeatPatterns(
						List.of(repeatPattern(DayOfWeek.MONDAY), repeatPattern(DayOfWeek.WEDNESDAY)))
				.sessions(List.of(session(SESSION_DATE_1), session(SESSION_DATE_2)))
				.build();
		given(mentoringRepository.findById(any())).willReturn(Optional.of(mentoring));

		MentoringResult.Schedules result = mentoringService.getMentoringSchedules(
				mentoring.getMentoringId()
						.mentoringId());

		log.info("==== 멘토링 스케쥴 조회 결과 ====");
		log.info("반복 패턴 ({}):", result.repeatPatterns()
				.size());
		result.repeatPatterns()
				.forEach(p -> log.info("  {} {} ~ {}", p.dayOfWeek(), p.startTime(), p.endTime()));
		log.info("세션 ({}):", result.sessions()
				.size());
		result.sessions()
				.forEach(s -> log.info("  {} {} ~ {} [{}]", s.date(), s.startTime(), s.endTime(),
						s.status()));

		assertThat(result.repeatPatterns()).hasSize(2);
		assertThat(result.repeatPatterns()
				.get(0)
				.dayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
		assertThat(result.repeatPatterns()
				.get(1)
				.dayOfWeek()).isEqualTo(DayOfWeek.WEDNESDAY);
		assertThat(result.sessions()).hasSize(2);
		assertThat(result.sessions()
				.get(0)
				.date()).isEqualTo(SESSION_DATE_1);
		assertThat(result.sessions()
				.get(1)
				.date()).isEqualTo(SESSION_DATE_2);
	}

	@Test
	void getMentoringSchedules_not_found() {
		UUID unknownId = UUID.randomUUID();
		given(mentoringRepository.findById(any())).willReturn(Optional.empty());

		log.info("존재하지 않는 멘토링 스케쥴 조회 시도: {}", unknownId);
		assertThatThrownBy(() -> mentoringService.getMentoringSchedules(unknownId)).isInstanceOf(
				MentoringNotFoundException.class);
		log.info("MentoringNotFoundException 발생 확인");
	}

	@Test
	void searchMentorings_success() {
		Mentoring m1 = defaultMentoringBuilder().build();
		Mentoring m2 = defaultMentoringBuilder().title("리액트 멘토링")
				.price(30_000)
				.build();
		Page<Mentoring> page = new PageImpl<>(List.of(m1, m2), PageRequest.of(0, 10), 2);
		given(mentoringRepository.findAll(any(), any())).willReturn(page);

		MentoringSearchCondition condition =
				new MentoringSearchCondition(null, null, null, null, null, null);
		Page<MentoringResult.Summary> result =
				mentoringService.searchMentorings(condition, CommonPageRequest.of(0, 10));

		log.info("==== 멘토링 목록 조회 결과 ====");
		log.info("총 {}건 / {}페이지", result.getTotalElements(), result.getTotalPages());
		result.getContent()
				.forEach(s -> log.info("  [{}] {} | {}원 | 멘토: {} | 카테고리: {}", s.status(), s.title(),
						s.price(), s.mentorName(), s.categoryName()));

		assertThat(result.getTotalElements()).isEqualTo(2);
		assertThat(result.getContent()).hasSize(2);
		assertThat(result.getContent()
				.get(0)
				.title()).isEqualTo(TITLE);
		assertThat(result.getContent()
				.get(1)
				.title()).isEqualTo("리액트 멘토링");
		assertThat(result.getContent()
				.get(1)
				.price()).isEqualTo(30_000);
	}

	@Test
	void searchMentorings_empty_result() {
		Page<Mentoring> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
		given(mentoringRepository.findAll(any(), any())).willReturn(emptyPage);

		MentoringSearchCondition condition =
				new MentoringSearchCondition("없는키워드", null, null, null, null, null);
		Page<MentoringResult.Summary> result =
				mentoringService.searchMentorings(condition, CommonPageRequest.of(0, 10));

		log.info("키워드 '{}' 검색 결과: {}건", condition.keyword(), result.getTotalElements());
		assertThat(result.getContent()).isEmpty();
		assertThat(result.getTotalElements()).isZero();
	}

	private MentoringCommand.Create defaultCommand() {
		return new MentoringCommand.Create(MENTOR_ID, MENTOR_NAME, MENTOR_FIELD, MENTOR_EMAIL,
				MENTOR_TYPE, CATEGORY_ID, TITLE, SUBTITLE, DESCRIPTION, DURATION,
				MentoringType.ONE_ON_ONE, Format.SINGLE, SESSION_COUNT, MAX_PARTICIPANTS, false,
				PRICE, null, List.of(sessionSlot(SESSION_DATE_1)),
				List.of(timeSchedules(DayOfWeek.MONDAY)));
	}
}
