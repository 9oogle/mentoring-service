package com.goggles.mentoring_service.application.service;

import com.goggles.mentoring_service.application.command.MentoringCommand;
import com.goggles.mentoring_service.domain.category.MentoringCategory;
import com.goggles.mentoring_service.domain.category.MentoringCategoryId;
import com.goggles.mentoring_service.domain.category.exception.CategoryNotFoundException;
import com.goggles.mentoring_service.domain.category.repository.MentoringCategoryRepository;
import com.goggles.mentoring_service.domain.mentoring.Format;
import com.goggles.mentoring_service.domain.mentoring.MentoringType;
import com.goggles.mentoring_service.domain.mentoring.repository.MentoringRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

        assertThatThrownBy(() -> mentoringService.createMentoring(defaultCommand()))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    private MentoringCommand.Create defaultCommand() {
        return new MentoringCommand.Create(
                MENTOR_ID, MENTOR_NAME, MENTOR_FIELD, MENTOR_EMAIL, MENTOR_TYPE,
                CATEGORY_ID,
                TITLE, SUBTITLE, DESCRIPTION,
                DURATION,
                MentoringType.ONE_ON_ONE,
                Format.SINGLE,
                SESSION_COUNT, MAX_PARTICIPANTS, false, PRICE, null,
                List.of(sessionSlot(SESSION_DATE_1)),
                List.of(timeSchedules(DayOfWeek.MONDAY))
        );
    }
}