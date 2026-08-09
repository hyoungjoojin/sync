package com.skkil.sync.collection.mapper;

import com.skkil.sync.collection.dto.data.CollectionMembershipDto;
import com.skkil.sync.collection.dto.data.CollectionPostDto;
import com.skkil.sync.collection.dto.response.GetCollectionPostsResponse;
import com.skkil.sync.collection.dto.response.GetCollectionsResponse;
import com.skkil.sync.collection.dto.summary.CollectionSummary;
import com.skkil.sync.collection.model.Collection;
import com.skkil.sync.common.util.pagination.dto.response.CursorPaginationResponse;
import com.skkil.sync.post.dto.summary.PostSummary;
import com.skkil.sync.post.service.PostDomainService;
import com.skkil.sync.project.model.Project;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component
public class CollectionAssembler {

  private final PostDomainService postDomainService;

  public CollectionAssembler(PostDomainService postDomainService) {
    this.postDomainService = postDomainService;
  }

  public GetCollectionsResponse toGetCollectionsResponse(List<Collection> collections) {
    return toGetCollectionsResponse(collections, null);
  }

  public GetCollectionsResponse toGetCollectionsResponse(
      List<Collection> collections, @Nullable List<CollectionMembershipDto> memberships) {
    return new GetCollectionsResponse(
        collections.stream().map(this::toSummary).toList(),
        memberships == null
            ? null
            : memberships.stream()
                .map(
                    membership ->
                        new GetCollectionsResponse.Membership(
                            membership.collectionExternalId(), membership.collectionPostId()))
                .toList());
  }

  public CollectionSummary toSummary(Collection collection) {
    Project project = collection.getProject();
    return new CollectionSummary(
        collection.getExternalId(),
        collection.getName(),
        collection.getDescription(),
        collection.getScope(),
        collection.isPublic(),
        collection.getPostCount(),
        collection.getCreator().getId(),
        project == null ? null : project.getHandle());
  }

  public GetCollectionPostsResponse toGetCollectionPostsResponse(
      CursorPaginationResponse<CollectionPostDto> page, @Nullable Long requesterId) {
    return new GetCollectionPostsResponse(
        page.mapWithLookup(
            CollectionPostDto::postId,
            postIds -> postDomainService.getReadablePostSummaries(requesterId, postIds),
            this::toItem));
  }

  private GetCollectionPostsResponse.Item toItem(
      CollectionPostDto dto, Map<Long, PostSummary> visibleSummaries) {
    Long postId = dto.postId();
    // post 가 null 이면 열람 불가(비공개·숨김)이거나 원본이 삭제된 경우다. 뷰어에게는 둘을 구분해 노출하지 않는다 — 어느 쪽이든 정보를 드러내지 않는
    // 불투명한 "unavailable" tombstone 이며, 삭제와 비공개를 나누면 오히려 슬롯의 메타데이터가 새어나간다.
    PostSummary summary = postId == null ? null : visibleSummaries.get(postId);
    return new GetCollectionPostsResponse.Item(dto.collectionPostId(), summary);
  }
}
