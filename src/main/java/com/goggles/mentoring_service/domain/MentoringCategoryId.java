package com.goggles.mentoring_service.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record MentoringCategoryId(@JdbcTypeCode(SqlTypes.UUID)
								  @Column(length=50, name="mentoring_category_id")
	UUID categoryId
) implements Serializable {

	public static MentoringCategoryId of(){
		return new MentoringCategoryId(UUID.randomUUID());
	}

	public static MentoringCategoryId of(String mentoringCategoryIdString){
		return new MentoringCategoryId(UUID.fromString(mentoringCategoryIdString));
	}
}