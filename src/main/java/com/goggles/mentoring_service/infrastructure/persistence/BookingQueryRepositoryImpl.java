package com.goggles.mentoring_service.infrastructure.persistence;

import com.goggles.mentoring_service.domain.booking.BookingSearchCondition;
import com.goggles.mentoring_service.domain.booking.BookingSort;
import com.goggles.mentoring_service.domain._common.UserType;
import com.goggles.mentoring_service.domain.booking.MentoringBooking;
import com.goggles.mentoring_service.domain.booking.QBookedTime;
import com.goggles.mentoring_service.domain.booking.QMentoringBooking;
import com.goggles.mentoring_service.infrastructure.persistence.jpa.BookingQueryRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BookingQueryRepositoryImpl implements BookingQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<MentoringBooking> findByUser(BookingSearchCondition condition, Pageable pageable) {
    QMentoringBooking b = QMentoringBooking.mentoringBooking;
    QBookedTime bt = new QBookedTime("bt");

    BooleanBuilder where = buildWhere(b, condition);
    OrderSpecifier<?> order = buildOrder(b, bt, condition.sort());

    boolean sortBySession = condition.sort() != null && condition.sort().isSortBySession();

    List<MentoringBooking> content;
    Long total;

    if (sortBySession) {
      content =
          queryFactory
              .selectFrom(b)
              .leftJoin(b.bookedTimes, bt)
              .where(where)
              .groupBy(b.mentoringBookingId)
              .orderBy(order)
              .offset(pageable.getOffset())
              .limit(pageable.getPageSize())
              .fetch();
    } else {
      content =
          queryFactory
              .selectFrom(b)
              .where(where)
              .orderBy(order)
              .offset(pageable.getOffset())
              .limit(pageable.getPageSize())
              .fetch();
    }

    total = queryFactory.select(b.count()).from(b).where(where).fetchOne();

    return new PageImpl<>(content, pageable, total == null ? 0 : total);
  }

  private BooleanBuilder buildWhere(QMentoringBooking b, BookingSearchCondition condition) {
    BooleanBuilder where = new BooleanBuilder();

    if (condition.userType() == UserType.STUDENT) {
      where.and(b.mentee.id.eq(condition.userId()));
    } else if (condition.userType() == UserType.INSTRUCTOR) {
      where.and(b.bookedMentoring.mentorId.eq(condition.userId()));
    }

    if (condition.status() != null) {
      where.and(b.status.eq(condition.status()));
    }

    return where;
  }

  private OrderSpecifier<?> buildOrder(QMentoringBooking b, QBookedTime bt, BookingSort sort) {
    if (sort == null) return b.createdAt.desc();
    return switch (sort) {
      case SESSION_DATE_ASC -> bt.sessionDate.min().asc();
      case SESSION_DATE_DESC -> bt.sessionDate.max().desc();
      case CREATED_AT_ASC -> b.createdAt.asc();
      case CREATED_AT_DESC -> b.createdAt.desc();
    };
  }
}
