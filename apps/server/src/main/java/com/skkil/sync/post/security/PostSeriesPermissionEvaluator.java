package com.skkil.sync.post.security;

import com.skkil.sync.auth.AuthenticatedUser;
import com.skkil.sync.common.security.CustomPermissionEvaluator;
import com.skkil.sync.common.security.PermissionOperation;
import com.skkil.sync.common.security.enums.PermissionEvaluatorType;
import com.skkil.sync.post.model.PostSeries;
import com.skkil.sync.post.repository.PostSeriesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PostSeriesPermissionEvaluator implements CustomPermissionEvaluator<String> {

  private final PostSeriesRepository seriesRepository;

  public PostSeriesPermissionEvaluator(PostSeriesRepository seriesRepository) {
    this.seriesRepository = seriesRepository;
  }

  @Override
  public PermissionEvaluatorType type() {
    return PermissionEvaluatorType.POST_SERIES;
  }

  @Override
  public boolean hasPermission(
      AuthenticatedUser user, String externalId, PermissionOperation permission) {
    PostSeries series = seriesRepository.findByExternalId(externalId).orElse(null);
    if (series == null) {
      log.debug("PostSeries with external id {} not found", externalId);
      return false;
    }

    return switch (permission) {
      case CREATE, EDIT, DELETE -> isCreator(user, series);

      default -> {
        log.debug("Unsupported permission operation: {}", permission);
        yield false;
      }
    };
  }

  private boolean isCreator(AuthenticatedUser user, PostSeries series) {
    return user != null && user.userId().equals(series.getCreator().getId());
  }
}
