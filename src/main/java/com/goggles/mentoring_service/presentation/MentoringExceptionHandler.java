package com.goggles.mentoring_service.presentation;


import com.goggles.mentoring_service.domain.exception.MentoringValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MentoringExceptionHandler {

	@ExceptionHandler(MentoringValidationException.class)
	public ProblemDetail handleValidationException(MentoringValidationException e) {
		ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
		problemDetail.setTitle("Invalid mentoring request");
		problemDetail.setDetail(e.getMessage());
		return problemDetail;
	}
}
