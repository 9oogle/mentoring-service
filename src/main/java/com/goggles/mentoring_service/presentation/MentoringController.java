package com.goggles.mentoring_service.presentation;

import com.goggles.common.pagination.CommonPageRequest;
import com.goggles.common.pagination.CommonPageResponse;
import com.goggles.mentoring_service.application.command.MentoringCommand;
import com.goggles.mentoring_service.application.query.MentoringSearchCondition;
import com.goggles.mentoring_service.application.query.MentoringSort;
import com.goggles.mentoring_service.application.result.MentoringResult;
import com.goggles.mentoring_service.application.service.MentoringService;
import com.goggles.mentoring_service.domain.mentoring.MentoringStatus;
import com.goggles.mentoring_service.domain.mentoring.MentoringType;
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
		MentoringResult.Detail detailDto= mentoringService.getMentoring(mentoringId);
		return  MentoringResponse.Detail.of(detailDto);
	}
	@GetMapping
	public CommonPageResponse<MentoringResponse.Summary> searchMentorings(
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) UUID categoryId,
			@RequestParam(required = false) UUID mentorId,
			@RequestParam(required = false) MentoringStatus status,
			@RequestParam(required = false) MentoringType mentoringType,
			@RequestParam(required = false) MentoringSort sortBy,
			CommonPageRequest pageRequest) {
		MentoringSearchCondition condition = new MentoringSearchCondition(keyword, categoryId, mentorId, status, mentoringType, sortBy);
		Page<MentoringResult.Summary> summary = mentoringService.searchMentorings(condition,
				pageRequest);
		return CommonPageResponse.of(summary.map(MentoringResponse.Summary::of));
	}
}