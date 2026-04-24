package com.goggles.mentoring_service.domain.mentoring.exception;

import com.goggles.mentoring_service.domain._common.MentoringValidationException;
import com.goggles.mentoring_service.domain._common.UserType;

import java.util.UUID;

public class InvalidMentorUserTypeException extends MentoringValidationException {

	public InvalidMentorUserTypeException(UUID mentorId, UserType userType) {
		super(String.format("멘토는 INSTRUCTOR 타입이어야 합니다.USER_ID : %s USER_TYPE: %s", mentorId,
				userType));
	}
}
