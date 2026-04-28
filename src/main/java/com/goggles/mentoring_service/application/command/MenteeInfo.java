package com.goggles.mentoring_service.application.command;

import com.goggles.mentoring_service.domain._common.UserType;
import java.util.UUID;

public record MenteeInfo(UUID menteeId, UserType menteeUserType, String menteeName) {}
