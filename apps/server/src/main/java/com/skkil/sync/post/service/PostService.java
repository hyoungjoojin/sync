package com.skkil.sync.post.service;

import com.skkil.sync.common.util.text.Slugify;
import com.skkil.sync.media.model.Media;
import com.skkil.sync.post.dto.request.CreatePostRequest;
import com.skkil.sync.post.dto.request.CreateProjectPostRequest;
import com.skkil.sync.post.dto.request.UpdatePostRequest;
import com.skkil.sync.post.dto.request.UpdatePostSummaryRequest;
import com.skkil.sync.post.dto.response.CreatePostResponse;
import com.skkil.sync.post.event.PostCreatedEvent;
import com.skkil.sync.post.exception.InvalidPostPublishRequestException;
import com.skkil.sync.post.exception.PostNotFoundException;
import com.skkil.sync.post.model.Post;
import com.skkil.sync.post.model.PostStatus;
import com.skkil.sync.post.model.PostType;
import com.skkil.sync.post.repository.PostRepository;
import com.skkil.sync.project.model.Project;
import com.skkil.sync.project.service.ProjectDomainService;
import com.skkil.sync.user.model.User;
import com.skkil.sync.user.service.domain.UserDomainService;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {

  private final UserDomainService userDomainService;
  private final ProjectDomainService projectDomainService;

  private final TagService tagService;
  private final PostContentMediaService contentMediaService;
  private final ApplicationEventPublisher eventPublisher;

  private final PostRepository postRepository;

  public PostService(
      UserDomainService userDomainService,
      ProjectDomainService projectDomainService,
      TagService tagService,
      PostContentMediaService contentMediaService,
      PostRepository postRepository,
      ApplicationEventPublisher eventPublisher) {
    this.userDomainService = userDomainService;
    this.projectDomainService = projectDomainService;
    this.tagService = tagService;
    this.contentMediaService = contentMediaService;
    this.postRepository = postRepository;
    this.eventPublisher = eventPublisher;
  }

  @Transactional
  public CreatePostResponse createPost(Long authorId, CreatePostRequest request) {
    return createPost(
        authorId,
        request.title(),
        request.type(),
        request.status(),
        request.content().text(),
        request.content().json(),
        request.content().mediaIds(),
        request.tags(),
        null,
        null);
  }

  @Transactional
  @PreAuthorize("hasPermission(#handle, 'PROJECT', 'CREATE')")
  public CreatePostResponse createProjectPost(
      Long authorId, String handle, CreateProjectPostRequest request) {
    Project project = projectDomainService.getProjectByHandle(handle);

    return createPost(
        authorId,
        request.title(),
        request.type(),
        request.status(),
        request.content().text(),
        request.content().json(),
        request.content().mediaIds(),
        request.tags(),
        request.projectTags(),
        project);
  }

  private CreatePostResponse createPost(
      Long authorId,
      String title,
      PostType type,
      PostStatus requestedStatus,
      String contentText,
      String contentJson,
      List<Long> mediaIds,
      List<String> tags,
      @Nullable List<String> projectTags,
      @Nullable Project project) {
    PostStatus status = resolveStatus(requestedStatus);
    validateCreatePostRequest(title, type, status);

    User author = userDomainService.getUserReference(authorId);

    String slug = createSlug(author, title);

    List<Media> mediaFiles = contentMediaService.resolveMediaFilesForCreate(authorId, mediaIds);

    Post.PostBuilder postBuilder =
        Post.builder()
            .slug(slug)
            .author(author)
            .type(type)
            .status(status)
            .title(title)
            .content(contentJson);

    if (project != null) {
      postBuilder.project(project);
    }

    Post post = postBuilder.build();
    post.updateContent(contentJson, contentText, mediaFiles.size());
    tagService.addTagsToPost(post, project, tags, projectTags == null ? List.of() : projectTags);

    post = postRepository.save(post);

    contentMediaService.savePostMediaFiles(post, mediaFiles);

    if (post.isPublished()) {
      postRepository.incrementActivityCount(
          author.getId(), LocalDate.ofInstant(post.getCreatedAt(), ZoneId.systemDefault()));
    }

    if (post.isPublished() && post.isPublic()) {
      eventPublisher.publishEvent(new PostCreatedEvent(post.getId(), contentText));
    }

    return new CreatePostResponse(post.getSlug());
  }

  private static PostStatus resolveStatus(PostStatus requestedStatus) {
    return requestedStatus == null ? PostStatus.PUBLISHED : requestedStatus;
  }

  private static void validateCreatePostRequest(String title, PostType type, PostStatus status) {
    if (status != PostStatus.PUBLISHED) {
      return;
    }

    if (requiresTitle(type) && isBlank(title)) {
      throw new InvalidPostPublishRequestException(
          "Published article and question posts require a title.");
    }
  }

  private static boolean requiresTitle(PostType type) {
    return type != PostType.SHORT;
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private static String createSlug(User author, String title) {
    if (isBlank(title)) {
      return String.format("%s-%d", author.getHandle(), System.currentTimeMillis());
    }

    return Slugify.slugify(title);
  }

  @Transactional
  @PreAuthorize("hasPermission(#postId, 'POST', 'EDIT')")
  public void updatePost(Long postId, UpdatePostRequest request) {
    Post post =
        postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException(postId));

    post.updateContent(
        request.content(), request.text(), contentMediaService.getMediaCountForPost(postId));
  }

  @Transactional
  @PreAuthorize("hasPermission(#postId, 'POST', 'EDIT')")
  public void updatePostSummary(Long postId, UpdatePostSummaryRequest request) {
    Post post =
        postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException(postId));

    post.updateSummary(request.summary());
  }

  @Transactional
  @PreAuthorize("hasPermission(#postId, 'POST', 'DELETE')")
  public void deletePost(Long postId) {
    if (!postRepository.existsById(postId)) {
      throw new PostNotFoundException(postId);
    }

    postRepository.deleteById(postId);
  }
}
