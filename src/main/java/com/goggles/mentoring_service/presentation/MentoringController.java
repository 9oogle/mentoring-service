package com.goggles.mentoring_service.presentation;

import com.goggles.common.pagination.CommonPageRequest;
import com.goggles.common.pagination.CommonPageResponse;
import com.goggles.mentoring_service.application.command.MentoringCommand;
import com.goggles.mentoring_service.application.result.MentoringResult;
import com.goggles.mentoring_service.application.service.MentoringService;
import com.goggles.mentoring_service.presentation.dto.MentoringRequest;
import com.goggles.mentoring_service.presentation.dto.MentoringResponse;
import com.goggles.mentoring_service.presentation.support.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

	@GetMapping("/{mentoringId}")
	public MentoringResponse.Detail getMentoring(@PathVariable UUID mentoringId) {
		MentoringResult.Detail detailDto = mentoringService.getMentoring(mentoringId);
		return MentoringResponse.Detail.of(detailDto);
	}

	@GetMapping("/{mentoringId}/schedules")
	public MentoringResponse.Schedules getMentoringSchedules(@PathVariable UUID mentoringId) {
		MentoringResult.Schedules schedule = mentoringService.getMentoringSchedules(mentoringId);
		return MentoringResponse.Schedules.of(schedule);
	}

	@GetMapping
	public CommonPageResponse<MentoringResponse.Summary> searchMentorings(
			@ModelAttribute MentoringRequest.Search request, CommonPageRequest pageRequest) {
		Page<MentoringResult.Summary> summary =
				mentoringService.searchMentorings(request.toCondition(), pageRequest);
		return CommonPageResponse.of(summary.map(MentoringResponse.Summary::of));
	}

	@PatchMapping("/{mentoringId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateMentoring(UserContext userContext, @PathVariable UUID mentoringId,
			@Valid @RequestBody MentoringRequest.Update request) {
		mentoringService.updateMentoring(mentoringId, request.toCommand(userContext));
	}


	@PatchMapping("/{mentoringId}/deactivate")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deactivateMentoring(UserContext userContext, @PathVariable UUID mentoringId) {
		mentoringService.deactivateMentoring(mentoringId, userContext.userId(),
				userContext.userType());
	}

	@DeleteMapping("/{mentoringId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteMentoring(UserContext userContext, @PathVariable UUID mentoringId) {
		mentoringService.deleteMentoring(mentoringId, userContext.userId(), userContext.userType());
	}

}