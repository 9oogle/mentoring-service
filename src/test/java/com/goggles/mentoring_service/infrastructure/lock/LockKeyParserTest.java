package com.goggles.mentoring_service.infrastructure.lock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;

import com.goggles.mentoring_service.infrastructure.exception.DistributionLockException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LockKeyParserTest {

	@Test
	void parse_staticKey_returnsAsIs() {
		ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

		String result = LockKeyParser.parse("booking:fixed-key", joinPoint);

		assertThat(result).isEqualTo("booking:fixed-key");
	}

	@Test
	void parse_spelWithStringConcatenation_resolvesCorrectly() {
		String mentoringId = "abc-123";
		ProceedingJoinPoint joinPoint = mockJoinPoint(new String[]{"command"}, new Object[]{new StubCommand(mentoringId)});

		String result = LockKeyParser.parse("'booking:' + #command.id()", joinPoint);

		assertThat(result).isEqualTo("booking:abc-123");
	}

	@Test
	void parse_spelAccessingField_resolvesCorrectly() {
		ProceedingJoinPoint joinPoint = mockJoinPoint(new String[]{"value"}, new Object[]{"hello"});

		String result = LockKeyParser.parse("#value", joinPoint);

		assertThat(result).isEqualTo("hello");
	}

	@Test
	void parse_spelEvaluatesToNull_throwsException() {
		ProceedingJoinPoint joinPoint = mockJoinPoint(new String[]{"command"}, new Object[]{new StubCommand(null)});

		assertThatThrownBy(() -> LockKeyParser.parse("#command.id()", joinPoint))
				.isInstanceOf(DistributionLockException.class)
				.hasMessageContaining("null");
	}

	private ProceedingJoinPoint mockJoinPoint(String[] paramNames, Object[] args) {
		MethodSignature signature = mock(MethodSignature.class);
		when(signature.getParameterNames()).thenReturn(paramNames);

		ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
		when(joinPoint.getSignature()).thenReturn(signature);
		when(joinPoint.getArgs()).thenReturn(args);
		return joinPoint;
	}

	static class StubCommand {
		private final String idValue;

		StubCommand(String idValue) {
			this.idValue = idValue;
		}

		public String id() {
			return idValue;
		}
	}
}