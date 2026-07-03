package com.skkil.sync.project.repository;

import static com.skkil.sync.jooq.tables.ProjectFollowRelationships.PROJECT_FOLLOW_RELATIONSHIPS;
import static com.skkil.sync.jooq.tables.Users.USERS;

import com.skkil.sync.common.util.pagination.interfaces.CursorPaginationDataFetcher;
import com.skkil.sync.project.dto.data.ProjectFollowerDto;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.SelectFieldOrAsterisk;
import org.springframework.stereotype.Repository;

@Repository
public class ProjectFollowerQueryRepository {

  private final DSLContext dsl;

  public ProjectFollowerQueryRepository(DSLContext dsl) {
    this.dsl = dsl;
  }

  public CursorPaginationDataFetcher<ProjectFollowerDto> getFollowers(Long projectId) {
    return (condition, orderFields, size) ->
        dsl.select(follower())
            .from(PROJECT_FOLLOW_RELATIONSHIPS)
            .join(USERS)
            .on(PROJECT_FOLLOW_RELATIONSHIPS.FOLLOWER_ID.eq(USERS.ID))
            .where(condition.and(PROJECT_FOLLOW_RELATIONSHIPS.PROJECT_ID.eq(projectId)))
            .orderBy(orderFields)
            .limit(size)
            .fetchInto(ProjectFollowerDto.class);
  }

  private List<SelectFieldOrAsterisk> follower() {
    return List.of(
        PROJECT_FOLLOW_RELATIONSHIPS.ID.as("relationshipId"),
        USERS.ID.as("userId"),
        USERS.HANDLE.as("handle"),
        USERS.FULL_NAME.as("name"));
  }
}
