package com.goggles.mentoring_service.infrastructure.lock;

import com.goggles.mentoring_service.infrastructure.exception.DistributionLockException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;

class LockKeyParser {

	private static final ExpressionParser PARSER = new SpelExpressionParser();

	static String parse(String expression, ProceedingJoinPoint joinPoint) {
		if (!expression.contains("#")) {
			return expression;
		}
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		String[] paramNames = signature.getParameterNames();
		Object[] args = joinPoint.getArgs();

		EvaluationContext context = SimpleEvaluationContext.forReadOnlyDataBinding()
				.withInstanceMethods()
				.build();
		for (int i = 0; i < paramNames.length; i++) {
			context.setVariable(paramNames[i], args[i]);
		}

		String result = PARSER.parseExpression(expression).getValue(context, String.class);
		if (result == null) {
			throw new DistributionLockException(
					"@DistributedLock key expression evaluated to null: " + expression);
		}
		return result;
	}
}
