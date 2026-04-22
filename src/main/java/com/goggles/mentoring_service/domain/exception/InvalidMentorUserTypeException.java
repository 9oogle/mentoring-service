package com.goggles.mentoring_service.domain.exception;

import com.goggles.common.exception.BadRequestException;
import com.goggles.mentoring_service.domain.UserType;

import java.util.UUID;

public class InvalidMentorUserTypeException extends BadRequestException {

	public InvalidMentorUserTypeException(UUID mentorId, UserType userType) {
		super("멘토는 INSTRUCTOR 타입이어야 합니다. USER ID:{} USER_TYPE: {}" + mentorId, userType.name());
	}
}
