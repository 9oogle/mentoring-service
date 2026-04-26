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

	public static MentoringPolicyViolationException sessionOutsideBusinessHours() {
		return new MentoringPolicyViolationException("세션은 오전 6시부터 오후 10시 사이에만 등록할 수 있습니다.");
	}

	public static MentoringPolicyViolationException sessionDurationMismatch() {
		return new MentoringPolicyViolationException("세션 시간이 멘토링 소요 시간과 일치하지 않습니다.");
	}

	public static MentoringPolicyViolationException sessionBeyondEndDate() {
		return new MentoringPolicyViolationException("세션 날짜가 멘토링 종료일을 초과할 수 없습니다.");
	}

	public static MentoringPolicyViolationException generateSessionsOnlyForAutoRepeat() {
		return new MentoringPolicyViolationException("세션 자동 생성은 MULTI 포맷 멘토링만 가능합니다.");
	}
}