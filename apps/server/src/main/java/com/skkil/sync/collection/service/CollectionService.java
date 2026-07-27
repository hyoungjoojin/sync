package com.skkil.sync.collection.service;

import com.skkil.sync.auth.AuthenticatedUser;
import com.skkil.sync.collection.dto.data.CollectionPostDto;
import com.skkil.sync.collection.dto.request.AddPostToCollectionRequest;
import com.skkil.sync.collection.dto.request.CreateCollectionRequest;
import com.skkil.sync.collection.dto.request.UpdateCollectionRequest;
import com.skkil.sync.collection.dto.response.CreateCollectionResponse;
import com.skkil.sync.collection.dto.response.GetCollectionPostsResponse;
import com.skkil.sync.collection.dto.response.GetCollectionsResponse;
import com.skkil.sync.collection.dto.summary.CollectionSummary;
import com.skkil.sync.collection.exception.CollectionNotFoundException;
import com.skkil.sync.collection.exception.CollectionPostAlreadyExistsException;
import com.skkil.sync.collection.exception.CollectionPostNotFoundException;
import com.skkil.sync.collection.mapper.CollectionAssembler;
import com.skkil.sync.collection.model.Collection;
import com.skkil.sync.collection.model.CollectionPost;
import com.skkil.sync.collection.repository.CollectionPostQueryRepository;
import com.skkil.sync.collection.repository.CollectionPostRepository;
import com.skkil.sync.collection.repository.CollectionRepository;
import com.skkil.sync.collection.repository.pagination.CollectionPostCursorPaginationProvider;
import com.skkil.sync.common.util.pagination.dto.request.CursorPaginationRequest;
import com.skkil.sync.common.util.pagination.dto.response.CursorPaginationResponse;
import com.skkil.sync.common.util.pagination.service.PaginationService;
import com.skkil.sync.common.util.text.Slugify;
import com.skkil.sync.post.exception.PostNotFoundException;
import com.skkil.sync.post.model.Post;
import com.skkil.sync.post.service.PostDomainService;
import com.skkil.sync.project.model.Project;
import com.skkil.sync.project.repository.TeammateRepository;
import com.skkil.sync.project.service.ProjectDomainService;
import com.skkil.sync.user.model.User;
import com.skkil.sync.user.service.domain.UserDomainService;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CollectionService {

  private final UserDomainService userDomainService;
  private final ProjectDomainService projectDomainService;
  private final PostDomainService postDomainService;

  private final CollectionRepository collectionRepository;
  private final CollectionPostRepository collectionPostRepository;
  private final CollectionPostQueryRepository collectionPostQueryRepository;
  private final TeammateRepository teammateRepository;

  private final PaginationService paginationService;
  private final CollectionPostCursorPaginationProvider collectionPostCursorPaginationProvider;
  private final CollectionAssembler collectionAssembler;

  public CollectionService(
      UserDomainService userDomainService,
      ProjectDomainService projectDomainService,
      PostDomainService postDomainService,
      CollectionRepository collectionRepository,
      CollectionPostRepository collectionPostRepository,
      CollectionPostQueryRepository collectionPostQueryRepository,
      TeammateRepository teammateRepository,
      PaginationService paginationService,
      CollectionPostCursorPaginationProvider collectionPostCursorPaginationProvider,
      CollectionAssembler collectionAssembler) {
    this.userDomainService = userDomainService;
    this.projectDomainService = projectDomainService;
    this.postDomainService = postDomainService;
    this.collectionRepository = collectionRepository;
    this.collectionPostRepository = collectionPostRepository;
    this.collectionPostQueryRepository = collectionPostQueryRepository;
    this.teammateRepository = teammateRepository;
    this.paginationService = paginationService;
    this.collectionPostCursorPaginationProvider = collectionPostCursorPaginationProvider;
    this.collectionAssembler = collectionAssembler;
  }

  @Transactional
  public CreateCollectionResponse createPersonalCollection(
      Long userId, CreateCollectionRequest request) {
    User creator = userDomainService.getUserReference(userId);

    Collection collection =
        Collection.builder()
            .externalId(Slugify.slugify(request.name()))
            .creator(creator)
            .name(request.name())
            .description(request.description())
            .isPublic(request.isPublic() == null || request.isPublic())
            .build();

    return new CreateCollectionResponse(collectionRepository.save(collection).getExternalId());
  }

  @Transactional
  @PreAuthorize("hasPermission(#handle, 'PROJECT', 'CREATE')")
  public CreateCollectionResponse createProjectCollection(
      Long userId, String handle, CreateCollectionRequest request) {
    User creator = userDomainService.getUserReference(userId);
    Project project = projectDomainService.getProjectByHandle(handle);

    Collection collection =
        Collection.builder()
            .externalId(Slugify.slugify(request.name()))
            .creator(creator)
            .project(project)
            .name(request.name())
            .description(request.description())
            .isPublic(request.isPublic() == null || request.isPublic())
            .build();

    return new CreateCollectionResponse(collectionRepository.save(collection).getExternalId());
  }

  @Transactional
  @PreAuthorize("hasPermission(#externalId, 'COLLECTION', 'EDIT')")
  public void updateCollection(String externalId, UpdateCollectionRequest request) {
    Collection collection = getCollectionByExternalId(externalId);

    String name = request.name() == null ? collection.getName() : request.name();
    String description =
        request.description() == null ? collection.getDescription() : request.description();
    boolean isPublic = request.isPublic() == null ? collection.isPublic() : request.isPublic();

    collection.update(name, description, isPublic);
  }

  @Transactional
  @PreAuthorize("hasPermission(#externalId, 'COLLECTION', 'DELETE')")
  public void deleteCollection(String externalId) {
    Collection collection = getCollectionByExternalId(externalId);
    collectionRepository.delete(collection);
  }

  @Transactional
  @PreAuthorize("hasPermission(#externalId, 'COLLECTION', 'EDIT')")
  public void addPost(Long userId, String externalId, AddPostToCollectionRequest request) {
    Collection collection = getCollectionByExternalId(externalId);

    Post post = resolvePost(userId, request.projectHandle(), request.postHandle());
    Long postId = post.getId();

    if (collectionPostRepository.existsByCollectionIdAndPostId(collection.getId(), postId)) {
      throw new CollectionPostAlreadyExistsException(collection.getId(), postId);
    }

    CollectionPost collectionPost =
        CollectionPost.builder().collection(collection).post(post).build();
    collectionPostRepository.save(collectionPost);
    collection.incrementPostCount();
  }

  private Post resolvePost(Long userId, @Nullable String projectHandle, String postHandle) {
    Post post = postDomainService.getPostBySlug(postHandle);

    String actualProjectHandle = post.getProject() == null ? null : post.getProject().getHandle();
    if (!Objects.equals(actualProjectHandle, projectHandle)) {
      throw new PostNotFoundException(postHandle);
    }

    // 읽기 경로와 동일한 단일 열람 게이트. 볼 수 없는 게시글은 없는 것과 구분되지 않게 한다.
    if (!postDomainService.isPostReadable(userId, post.getId())) {
      throw new PostNotFoundException(postHandle);
    }

    return post;
  }

  @Transactional
  @PreAuthorize("hasPermission(#externalId, 'COLLECTION', 'EDIT')")
  public void removeItem(String externalId, Long collectionPostId) {
    Collection collection = getCollectionByExternalId(externalId);

    CollectionPost collectionPost =
        collectionPostRepository
            .findByIdAndCollectionId(collectionPostId, collection.getId())
            .orElseThrow(
                () -> new CollectionPostNotFoundException(collection.getId(), collectionPostId));

    collectionPostRepository.delete(collectionPost);
    collection.decrementPostCount();
  }

  @Transactional(readOnly = true)
  @PreAuthorize("hasPermission(#externalId, 'COLLECTION', 'READ')")
  public CollectionSummary getCollection(String externalId) {
    Collection collection = getCollectionByExternalId(externalId);
    return collectionAssembler.toSummary(collection);
  }

  @Transactional(readOnly = true)
  @PreAuthorize("hasPermission(#externalId, 'COLLECTION', 'READ')")
  public GetCollectionPostsResponse getCollectionPosts(
      String externalId, @Nullable AuthenticatedUser viewer, CursorPaginationRequest pagination) {
    Collection collection = getCollectionByExternalId(externalId);
    Long requesterId = viewer == null ? null : viewer.userId();

    CursorPaginationResponse<CollectionPostDto> page =
        paginationService.paginate(
            collectionPostQueryRepository.getCollectionPosts(collection.getId()),
            collectionPostCursorPaginationProvider,
            pagination);

    return collectionAssembler.toGetCollectionPostsResponse(page, requesterId);
  }

  @Transactional(readOnly = true)
  public GetCollectionsResponse getUserCollections(
      @Nullable Long viewerId, Long userId, @Nullable String postHandle) {
    List<Collection> collections =
        collectionRepository.findByCreatorId(userId).stream()
            .filter(Collection::isPersonal)
            .filter(collection -> collection.isPublic() || userId.equals(viewerId))
            .toList();

    Set<Long> containingCollectionIds =
        resolveContainingCollectionIds(viewerId, postHandle, collections);
    return collectionAssembler.toGetCollectionsResponse(collections, containingCollectionIds);
  }

  /**
   * 한 프로젝트의 컬렉션 목록을 조회한다. 팀원은 공개·비공개 모든 컬렉션을 볼 수 있고, 그 외에는 공개 프로젝트의 공개 컬렉션만 볼 수 있다. ({@code
   * CollectionPermissionEvaluator#canRead} 의 WORKSPACE 규칙과 동일하다.)
   */
  @Transactional(readOnly = true)
  public GetCollectionsResponse getProjectCollections(
      @Nullable Long viewerId, String handle, @Nullable String postHandle) {
    Project project = projectDomainService.getProjectByHandle(handle);

    boolean isTeammate =
        viewerId != null
            && teammateRepository.findByProjectIdAndUserId(project.getId(), viewerId).isPresent();

    List<Collection> collections =
        collectionRepository.findByProjectId(project.getId()).stream()
            .filter(collection -> isTeammate || (project.isPublic() && collection.isPublic()))
            .toList();

    Set<Long> containingCollectionIds =
        resolveContainingCollectionIds(viewerId, postHandle, collections);
    return collectionAssembler.toGetCollectionsResponse(collections, containingCollectionIds);
  }

  /**
   * {@code postHandle} 컨텍스트가 주어졌을 때, {@code collections} 중 그 게시글을 담고 있는 컬렉션의 id 집합을 반환한다. {@code
   * postHandle} 이 없으면 {@code null} 을 반환해 응답의 {@code containsPost} 를 생략한다. 게시글은 addPost 와 동일한 열람
   * 게이트를 거치며, 볼 수 없거나 존재하지 않으면 {@link PostNotFoundException} 을 던진다.
   */
  private @Nullable Set<Long> resolveContainingCollectionIds(
      @Nullable Long viewerId, @Nullable String postHandle, List<Collection> collections) {
    if (postHandle == null) {
      return null;
    }

    Post post = postDomainService.getPostBySlug(postHandle);
    if (!postDomainService.isPostReadable(viewerId, post.getId())) {
      throw new PostNotFoundException(postHandle);
    }

    List<Long> collectionIds = collections.stream().map(Collection::getId).toList();
    if (collectionIds.isEmpty()) {
      return Set.of();
    }

    return new HashSet<>(
        collectionPostRepository.findCollectionIdsByPostIdAndCollectionIdIn(
            post.getId(), collectionIds));
  }

  private Collection getCollectionByExternalId(String externalId) {
    return collectionRepository
        .findByExternalId(externalId)
        .orElseThrow(() -> new CollectionNotFoundException(externalId));
  }
}
