package com.skkil.sync.user.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.skkil.sync.common.util.restdocs.RestDocsUtils;
import com.skkil.sync.user.constant.Role;
import com.skkil.sync.user.dto.summary.AdminUserSummary;
import java.time.Instant;
import java.util.List;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

public class AdminUserSummarySnippets {

  public static AdminUserSummary getAdminUserSummary() {
    return AdminUserSummary.builder()
        .id(1L)
        .handle("john-doe")
        .name("John Doe")
        .email("john@example.com")
        .profileImageUrl("https://example.com/john.png")
        .role(Role.USER)
        .isOnboarded(true)
        .isEmailVerified(true)
        .followerCount(12L)
        .followingCount(7L)
        .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
        .deletedAt(null)
        .build();
  }

  public static ResponseFieldsSnippet getAdminUserSummaryResponseFields() {
    return responseFields(getAdminUserSummaryFields("").toArray(new FieldDescriptor[0]));
  }

  public static List<FieldDescriptor> getAdminUserSummaryFields(String prefix) {
    return List.of(
        fieldWithPath(prefix + "id").type(JsonFieldType.NUMBER).description("유저 ID"),
        fieldWithPath(prefix + "handle").type(JsonFieldType.STRING).description("유저 핸들").optional(),
        fieldWithPath(prefix + "name").type(JsonFieldType.STRING).description("유저 이름"),
        fieldWithPath(prefix + "email").type(JsonFieldType.STRING).description("유저 이메일"),
        fieldWithPath(prefix + "profileImageUrl")
            .type(JsonFieldType.STRING)
            .description("유저 프로필 이미지 URL")
            .optional(),
        fieldWithPath(prefix + "role")
            .type(RestDocsUtils.ENUM_TYPE)
            .description("플랫폼 역할")
            .attributes(RestDocsUtils.getEnumAttributes(Role.class)),
        fieldWithPath(prefix + "isOnboarded").type(JsonFieldType.BOOLEAN).description("온보딩 완료 여부"),
        fieldWithPath(prefix + "isEmailVerified")
            .type(JsonFieldType.BOOLEAN)
            .description("이메일 인증 여부"),
        fieldWithPath(prefix + "followerCount").type(JsonFieldType.NUMBER).description("팔로워 수"),
        fieldWithPath(prefix + "followingCount").type(JsonFieldType.NUMBER).description("팔로잉 수"),
        fieldWithPath(prefix + "createdAt").type(JsonFieldType.STRING).description("가입 시각"),
        fieldWithPath(prefix + "deletedAt")
            .type(JsonFieldType.STRING)
            .description("탈퇴 시각 (탈퇴하지 않았다면 null)")
            .optional());
  }
}
