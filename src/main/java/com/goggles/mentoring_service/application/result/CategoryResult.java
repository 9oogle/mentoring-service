package com.goggles.mentoring_service.application.result;

import com.goggles.mentoring_service.domain.category.MentoringCategory;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class CategoryResult {

  @Getter
  @AllArgsConstructor
  public static class Name {
    private final String name;
    private final String code;

    public static Name of(String name, String code) {
      return new Name(name, code);
    }
  }

  @Getter
  public static class Info extends Name {
    private final UUID categoryId;
    private final Integer sortOrder;
    private final boolean active;

    public Info(UUID categoryId, String name, String code, Integer sortOrder, boolean active) {
      super(name, code);
      this.categoryId = categoryId;
      this.sortOrder = sortOrder;
      this.active = active;
    }

    public static Info of(MentoringCategory mentoringCategory) {
      return new Info(
          mentoringCategory.getMentoringCategoryId().categoryId(),
          mentoringCategory.getName(),
          mentoringCategory.getCode(),
          mentoringCategory.getSortOrder(),
          mentoringCategory.isActive());
    }

    public static List<Info> of(List<MentoringCategory> categories) {
      return categories.stream()
          .map(Info::of)
          .toList();
    }
  }

}
