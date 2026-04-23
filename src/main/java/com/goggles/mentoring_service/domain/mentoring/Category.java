package com.goggles.mentoring_service.domain.mentoring;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.UUID;

@Embeddable
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {
	private UUID categoryId;
	@Column(length = 50, name = "category_name")
	private String name;
	@Column(length = 10, name = "category_code")
	private String code;
}
