package com.goggles.mentoring_service.domain.mentoring.exception;

import com.goggles.mentoring_service.domain.common.MentoringValidationException;

public class MentoringPolicyViolationException extends MentoringValidationException {

	private MentoringPolicyViolationException(String message) {
		super(message);
	}

	public static MentoringPolicyViolationException invalidPrice() {
		return new MentoringPolicyViolationException("가격은 0원 이상이어야 합니다.");
	}

	public static MentoringPolicyViolationException groupMentoringMinParticipants() {
		return new MentoringPolicyViolationException("그룹 멘토링은 최대 참여자 수가 2명 이상이어야 합니다.");
	}

	public static MentoringPolicyViolationException oneOnOneMaxParticipants() {
		return new MentoringPolicyViolationException("1:1 멘토링의 최대 참여자 수는 1명이어야 합니다.");
	}

	public static MentoringPolicyViolationException selfSelectMinSessions() {
		return new MentoringPolicyViolationException("자유 선택 예약은 세션 수가 2회 이상이어야 합니다.");
	}
}