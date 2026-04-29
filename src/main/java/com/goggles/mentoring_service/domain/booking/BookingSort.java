package com.goggles.mentoring_service.domain.booking;

public enum BookingSort {
  SESSION_DATE_ASC,
  SESSION_DATE_DESC,
  CREATED_AT_ASC,
  CREATED_AT_DESC;

	public boolean isSortBySession() {
		return this == SESSION_DATE_ASC || this == SESSION_DATE_DESC;
	}
}