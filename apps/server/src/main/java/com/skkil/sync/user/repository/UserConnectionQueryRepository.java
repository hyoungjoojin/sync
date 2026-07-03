package com.skkil.sync.user.repository;

import static com.skkil.sync.jooq.tables.UserFollowRelationships.USER_FOLLOW_RELATIONSHIPS;
import static com.skkil.sync.jooq.tables.Users.USERS;

import com.skkil.sync.common.util.pagination.interfaces.CursorPaginationDataFetcher;
import com.skkil.sync.user.dto.data.UserConnectionDto;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.SelectFieldOrAsterisk;
import org.springframework.stereotype.Repository;

@Repository
public class UserConnectionQueryRepository {

  private final DSLContext dsl;

  public UserConnectionQueryRepository(DSLContext dsl) {
    this.dsl = dsl;
  }

  public CursorPaginationDataFetcher<UserConnectionDto> getFollowing(Long userId) {
    return (condition, orderFields, size) ->
        dsl.select(connection())
            .from(USER_FOLLOW_RELATIONSHIPS)
            .join(USERS)
            .on(USER_FOLLOW_RELATIONSHIPS.FOLLOWEE_ID.eq(USERS.ID))
            .where(condition.and(USER_FOLLOW_RELATIONSHIPS.FOLLOWER_ID.eq(userId)))
            .orderBy(orderFields)
            .limit(size)
            .fetchInto(UserConnectionDto.class);
  }

  public CursorPaginationDataFetcher<UserConnectionDto> getFollowers(Long userId) {
    return (condition, orderFields, size) ->
        dsl.select(connection())
            .from(USER_FOLLOW_RELATIONSHIPS)
            .join(USERS)
            .on(USER_FOLLOW_RELATIONSHIPS.FOLLOWER_ID.eq(USERS.ID))
            .where(condition.and(USER_FOLLOW_RELATIONSHIPS.FOLLOWEE_ID.eq(userId)))
            .orderBy(orderFields)
            .limit(size)
            .fetchInto(UserConnectionDto.class);
  }

  private List<SelectFieldOrAsterisk> connection() {
    return List.of(
        USER_FOLLOW_RELATIONSHIPS.ID.as("relationshipId"),
        USERS.ID.as("userId"),
        USERS.HANDLE.as("handle"),
        USERS.FULL_NAME.as("name"));
  }
}
