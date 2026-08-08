package com.skkil.sync.post.service;

import com.skkil.sync.auth.agent.AuthorizedAgentClient;
import com.skkil.sync.common.integration.email.EmailService;
import com.skkil.sync.common.integration.email.dto.EmailMessage;
import com.skkil.sync.post.dto.request.CreateAgentPostRequest;
import com.skkil.sync.post.dto.response.AgentPostCreationResult;
import com.skkil.sync.post.model.Post;
import com.skkil.sync.project.model.Project;
import com.skkil.sync.project.service.ProjectDomainService;
import com.skkil.sync.user.model.User;
import com.skkil.sync.user.service.domain.UserDomainService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

/**
 * 에이전트 초안 생성의 유일한 진입점. REST 컨트롤러와 MCP 도구가 모두 여기로 들어오므로, 요청 검증도 컨트롤러가 아니라 이 경계에서 한다 — MCP 쪽에는
 * {@code @Validated} 를 걸어 줄 컨트롤러가 없어서 여기서 하지 않으면 검증이 통째로 빠진다.
 */
@ConditionalOnProperty(name = "app.agent.enabled", havingValue = "true", matchIfMissing = true)
@Service
@Validated
@Slf4j
public class AgentPostService {

  private final PostService postService;
  private final ProjectDomainService projectDomainService;
  private final UserDomainService userDomainService;
  private final EmailService emailService;
  private final SpringTemplateEngine templateEngine;
  private final String frontendBaseUrl;

  public AgentPostService(
      PostService postService,
      ProjectDomainService projectDomainService,
      UserDomainService userDomainService,
      EmailService emailService,
      SpringTemplateEngine templateEngine,
      @Value("${app.frontend.base-url}") String frontendBaseUrl) {
    this.postService = postService;
    this.projectDomainService = projectDomainService;
    this.userDomainService = userDomainService;
    this.emailService = emailService;
    this.templateEngine = templateEngine;
    this.frontendBaseUrl = frontendBaseUrl;
  }

  @Transactional
  @PreAuthorize(
      "#request.projectHandle == null"
          + " or hasPermission(#request.projectHandle, 'PROJECT', 'CREATE')")
  public AgentPostCreationResult createDraftPost(
      Long authorId, @Valid CreateAgentPostRequest request, AuthorizedAgentClient client) {
    Project project =
        request.projectHandle() == null
            ? null
            : projectDomainService.getProjectByHandle(request.projectHandle());

    Post post =
        postService.createMarkdownDraft(
            authorId,
            request.title(),
            request.type(),
            request.bodyMarkdown(),
            request.tagsOrEmpty(),
            request.projectTagsOrEmpty(),
            project,
            client.registeredClientId());

    String reviewUrl = buildReviewUrl(post.getSlug(), request.projectHandle());

    notifyAuthor(authorId, post, reviewUrl, client);

    return AgentPostCreationResult.builder()
        .slug(post.getSlug())
        .reviewUrl(reviewUrl)
        .type(post.getType())
        .title(post.getTitle())
        .createdAt(post.getCreatedAt())
        .build();
  }

  private String buildReviewUrl(String slug, @Nullable String projectHandle) {
    if (projectHandle == null) {
      return frontendBaseUrl + "/posts/" + slug + "/edit";
    }

    return frontendBaseUrl + "/projects/" + projectHandle + "/posts/" + slug + "/edit";
  }

  private void notifyAuthor(
      Long authorId, Post post, String reviewUrl, AuthorizedAgentClient client) {
    try {
      User author = userDomainService.getUserReference(authorId);

      Context context = new Context();
      context.setVariable("reviewUrl", reviewUrl);
      context.setVariable("clientName", client.clientName());
      context.setVariable("title", post.getTitle());
      context.setVariable("preview", post.getPreview());
      context.setVariable("frontendBaseUrl", frontendBaseUrl);

      EmailMessage email =
          EmailMessage.builder()
              .to(author.getEmail())
              .subject("[SYNC] AI 에이전트가 글 초안을 작성했습니다")
              .text(templateEngine.process("email/agent-post-drafted", context))
              .build();

      emailService
          .sendMessage(email)
          .exceptionally(
              e -> {
                log.error("Failed to send agent draft email for post {}", post.getId(), e);
                return null;
              });
    } catch (RuntimeException e) {
      log.error("Failed to prepare agent draft email for post {}", post.getId(), e);
    }
  }
}
