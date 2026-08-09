package com.skkil.sync.mcp;

import com.skkil.sync.auth.AuthenticatedUser;
import com.skkil.sync.auth.agent.AuthorizedAgentClient;
import com.skkil.sync.auth.agent.AuthorizedAgentClientResolver;
import com.skkil.sync.post.dto.request.CreateAgentPostRequest;
import com.skkil.sync.post.dto.response.AgentPostCreationResult;
import com.skkil.sync.post.model.PostType;
import com.skkil.sync.post.service.AgentPostService;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(name = "app.agent.enabled", havingValue = "true", matchIfMissing = true)
@Component
public class CreatePostTool {

  private final AgentPostService agentPostService;
  private final RegisteredClientRepository registeredClientRepository;

  public CreatePostTool(
      AgentPostService agentPostService, RegisteredClientRepository registeredClientRepository) {
    this.agentPostService = agentPostService;
    this.registeredClientRepository = registeredClientRepository;
  }

  @McpTool(
      name = "create_post",
      title = "Create a draft post on SYNC",
      description =
          """
          Create a draft post on SYNC on behalf of the authenticated user. The post is \
          NEVER published automatically — it always lands as a draft that the user must \
          review and publish themselves on sync.skkil.org. Returns a review link to show \
          the user.

          `bodyMarkdown` is Markdown (GitHub-flavored: headings, lists including task \
          lists, tables, fenced code blocks with a language, bold/italic/strikethrough, \
          links, blockquotes). Images and embeds are NOT supported — do not include image \
          syntax.\
          """)
  public AgentPostCreationResult createPost(
      @McpToolParam(
              required = true,
              description =
                  "Post shape. SHORT is a brief channel-style note and needs no title. "
                      + "LONG is an article. QUESTION asks something answerable.")
          PostType type,
      @McpToolParam(
              required = false,
              description = "Title. Required for LONG and QUESTION; omit for SHORT.")
          @Nullable String title,
      @McpToolParam(required = true, description = "Post body as GitHub-flavored Markdown.")
          String bodyMarkdown,
      @McpToolParam(required = false, description = "Global tag names to attach.")
          @Nullable List<String> tags,
      @McpToolParam(
              required = false,
              description =
                  "Handle of the project to post into. Omit for a personal post. "
                      + "The user must already be a member of that project.")
          @Nullable String projectHandle,
      @McpToolParam(
              required = false,
              description = "Project-scoped tag names. Ignored when projectHandle is omitted.")
          @Nullable List<String> projectTags) {
    CreateAgentPostRequest request =
        CreateAgentPostRequest.builder()
            .type(type)
            .title(title)
            .bodyMarkdown(bodyMarkdown)
            .tags(tags)
            .projectHandle(projectHandle)
            .projectTags(projectTags)
            .build();

    return agentPostService.createDraftPost(currentUserId(), request, currentClient());
  }

  /**
   * 도구 호출을 인증한 사용자. 세션이 아니라 액세스 토큰의 {@code sub} 클레임에서 나온 주체이며, 도구 인자에는 사용자를 지정하는 값이 없다.
   *
   * <p>WebMVC 동기 서버라 도구 핸들러가 인증된 요청과 같은 스레드에서 돌고, 따라서 {@code SecurityContextHolder} 가 그대로 보인다.
   */
  private Long currentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
      throw new IllegalStateException("MCP 도구 호출에 인증된 사용자가 없습니다");
    }

    return user.userId();
  }

  private AuthorizedAgentClient currentClient() {
    AuthorizedAgentClient client =
        AuthorizedAgentClientResolver.current(registeredClientRepository);
    if (client == null) {
      throw new IllegalStateException("MCP 도구 호출에 에이전트 클라이언트 정보가 없습니다");
    }

    return client;
  }
}
