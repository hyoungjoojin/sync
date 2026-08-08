package com.skkil.sync.post.mapper;

import com.skkil.sync.post.dto.response.GetPostReportsResponse;
import com.skkil.sync.post.model.Post;
import com.skkil.sync.post.model.PostReport;
import com.skkil.sync.user.dto.summary.UserSummary;
import com.skkil.sync.user.model.User;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class PostReportAssembler {

  public GetPostReportsResponse.Report toReport(
      PostReport report, Map<Long, UserSummary> userSummaries) {
    Post post = report.getPost();
    User reviewer = report.getReviewedBy();

    return new GetPostReportsResponse.Report(
        report.getId(),
        toPost(post),
        userSummaries.get(report.getReporter().getId()),
        report.getReason(),
        report.getDescription(),
        report.getStatus(),
        report.getCreatedAt(),
        reviewer == null ? null : userSummaries.get(reviewer.getId()),
        report.getReviewedAt(),
        report.getResolutionNote());
  }

  /**
   * 신고된 글의 본문. 에이전트가 만든 초안은 Tiptap JSON 이 아니라 Markdown 으로만 저장되므로, JSON 이 없으면 Markdown 을 그대로 넘긴다 —
   * 검토하는 관리자에게 빈 본문을 보여 주면 무엇이 신고됐는지 알 수 없다.
   */
  private GetPostReportsResponse.Post toPost(Post post) {
    String content =
        post.getJsonContent() == null ? post.getMarkdownContent() : post.getJsonContent();

    return new GetPostReportsResponse.Post(
        post.getId(),
        post.getSlug(),
        post.getTitle(),
        content == null ? "" : content,
        post.getVisibility());
  }
}
