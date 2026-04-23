package com.goggles.mentoring_service.domain.booking.exception;

import com.goggles.mentoring_service.domain.common.MentoringValidationException;
import com.goggles.mentoring_service.domain.common.UserType;

import java.util.UUID;

public class InvalidMenteeUserTypeException extends MentoringValidationException {

	public InvalidMenteeUserTypeException(UUID menteeId, UserType userType) {
		super(String.format("멘티는 STUDENT 타입이어야 합니다. USER_ID : %s USER_TYPE: %s", menteeId,
				userType));
	}
}