package com.goggles.mentoring_service.infrastructure.persistence;

import com.goggles.mentoring_service.domain.mentoring.*;
import com.goggles.mentoring_service.infrastructure.Escape;
import com.goggles.mentoring_service.infrastructure.persistence.jpa.MentoringQueryRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MentoringQueryRepositoryImpl implements MentoringQueryRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<Mentoring> search(MentoringSearchCondition condition, Pageable pageable) {
		QMentoring m = QMentoring.mentoring;
		BooleanBuilder where = buildWhere(m, condition);

		List<Mentoring> content = queryFactory.selectFrom(m)
				.where(where)
				.orderBy(buildOrder(m, condition))
				.offset(pageable.getOffset())
				.limit(pageable.getPageSize())
				.fetch();

		Long total = queryFactory.select(m.count())
				.from(m)
				.where(where)
				.fetchOne();

		return new PageImpl<>(content, pageable, total == null ? 0 : total);
	}

	private BooleanBuilder buildWhere(QMentoring m, MentoringSearchCondition condition) {
		BooleanBuilder where = new BooleanBuilder();

		if (StringUtils.hasText(condition.keyword())) {
			String pattern = Escape.contains(condition.keyword());
			where.and(m.title.lower()
					.like(pattern)
					.or(m.subtitle.lower()
							.like(pattern))
					.or(m.description.lower()
							.like(pattern))
					.or(m.mentor.name.lower()
							.like(pattern))
					.or(m.mentoringCategory.name.lower()
							.like(pattern)));
		}

		if (condition.categoryId() != null) {
			where.and(m.mentoringCategory.categoryId.eq(condition.categoryId()));
		}

		if (condition.mentorId() != null) {
			where.and(m.mentor.id.eq(condition.mentorId()));
		}

		MentoringStatus filterStatus =
				condition.status() != null ? condition.status() : MentoringStatus.ACTIVE;
		where.and(m.status.eq(filterStatus));

		if (condition.mentoringType() != null) {
			where.and(m.mentoringType.eq(condition.mentoringType()));
		}

		where.and(m.endDate.isNull()
				.or(m.endDate.goe(LocalDate.now())));

		return where;
	}

	private OrderSpecifier<?>[] buildOrder(QMentoring m, MentoringSearchCondition condition) {
		OrderSpecifier<?> primary = switch (condition.sortBy()) {
			case PRICE_ASC -> m.price.asc();
			case PRICE_DESC -> m.price.desc();
			case DURATION_ASC -> m.duration.asc();
			case DURATION_DESC -> m.duration.desc();
			default -> m.createdAt.desc();
		};
		if (condition.sortBy() == MentoringSort.CREATED_AT) {
			return new OrderSpecifier<?>[]{primary};
		}
		return new OrderSpecifier<?>[]{primary, m.createdAt.desc()};
	}
}