package com.goggles.mentoring_service.presentation;

import com.goggles.mentoring_service.application.command.MentoringCommand;
import com.goggles.mentoring_service.application.service.MentoringService;
import com.goggles.mentoring_service.presentation.dto.MentoringRequest;
import com.goggles.mentoring_service.presentation.dto.MentoringResponse;
import com.goggles.mentoring_service.presentation.support.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/mentorings")
@RequiredArgsConstructor
public class MentoringController {

	private final MentoringService mentoringService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public MentoringResponse.Create createMentoring(UserContext userContext,
			@Valid @RequestBody MentoringRequest.Create request) {
		MentoringCommand.Create command = request.toCommand(userContext);
		UUID mentoringId = mentoringService.createMentoring(command);
		return new MentoringResponse.Create(mentoringId);
	}
}