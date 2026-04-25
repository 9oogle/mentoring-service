package com.goggles.mentoring_service.presentation;


import com.goggles.common.exception.ConflictException;
import com.goggles.common.exception.ForbiddenException;
import com.goggles.common.exception.NotFoundException;
import com.goggles.mentoring_service.domain.common.MentoringValidationException;
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

	@ExceptionHandler(ConflictException.class)
	public ProblemDetail handleConflictException(ConflictException e) {
		ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
		problemDetail.setTitle("Conflict");
		problemDetail.setDetail(e.getMessage());
		return problemDetail;
	}

	@ExceptionHandler(ForbiddenException.class)
	public ProblemDetail handleForbiddenException(ForbiddenException e) {
		ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
		problemDetail.setTitle("Forbidden");
		problemDetail.setDetail(e.getMessage());
		return problemDetail;
	}

	@ExceptionHandler(NotFoundException.class)
	public ProblemDetail handleNotFoundException(NotFoundException e) {
		ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
		problemDetail.setTitle("Not Found");
		problemDetail.setDetail(e.getMessage());
		return problemDetail;
	}
}
