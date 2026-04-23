package com.goggles.mentoring_service.application.command;


import java.time.DayOfWeek;
import java.time.LocalTime;

public record TimeSchedules(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {}