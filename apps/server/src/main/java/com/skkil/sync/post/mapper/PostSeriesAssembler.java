package com.skkil.sync.post.mapper;

import com.skkil.sync.post.dto.response.GetPostSeriesListResponse;
import com.skkil.sync.post.dto.response.GetPostSeriesResponse;
import com.skkil.sync.post.dto.summary.PostSeriesSummary;
import com.skkil.sync.post.dto.summary.PostSummary;
import com.skkil.sync.post.model.PostSeries;
import com.skkil.sync.post.model.PostSeriesPost;
import com.skkil.sync.post.service.PostDomainService;
import com.skkil.sync.project.model.Project;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component
public class PostSeriesAssembler {

  private final PostDomainService postDomainService;

  public PostSeriesAssembler(PostDomainService postDomainService) {
    this.postDomainService = postDomainService;
  }

  public PostSeriesSummary toSummary(PostSeries series) {
    Project project = series.getProject();
    return new PostSeriesSummary(
        series.getExternalId(),
        series.getName(),
        series.getScope(),
        series.getPostCount(),
        series.getCreator().getId(),
        project == null ? null : project.getHandle());
  }

  public GetPostSeriesListResponse toGetSeriesListResponse(List<PostSeries> seriesList) {
    return new GetPostSeriesListResponse(seriesList.stream().map(this::toSummary).toList());
  }

  public List<GetPostSeriesResponse.Post> toSeriesPostItems(
      List<PostSeriesPost> items, @Nullable Long requesterId) {
    List<Long> postIds = items.stream().map(item -> item.getPost().getId()).toList();
    Map<Long, PostSummary> visibleSummaries =
        postDomainService.getReadablePostSummaries(requesterId, postIds);

    return items.stream().map(item -> toItem(item, visibleSummaries)).toList();
  }

  private GetPostSeriesResponse.Post toItem(
      PostSeriesPost item, Map<Long, PostSummary> visibleSummaries) {
    PostSummary summary = visibleSummaries.get(item.getPost().getId());
    // 열람할 수 없는 항목은 불투명 slot 이므로 제목뿐 아니라 이동 경로(slug)도 노출하지 않는다.
    String slug = summary == null ? null : summary.slug();
    String title = summary == null ? null : summary.title();
    return new GetPostSeriesResponse.Post(item.getId(), item.getPosition(), slug, title);
  }
}
