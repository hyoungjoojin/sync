package com.skkil.sync.notification.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;

import com.epages.restdocs.apispec.FieldDescriptors;
import com.skkil.sync.common.util.pagination.snippets.OffsetPaginationResponseSnippets;
import com.skkil.sync.notification.constant.NotificationEntityType;
import com.skkil.sync.notification.constant.NotificationStatus;
import com.skkil.sync.notification.constant.NotificationType;
import com.skkil.sync.notification.dto.data.NotificationSummary;
import com.skkil.sync.notification.dto.response.GetNotificationsResponse;
import com.skkil.sync.notification.model.NewCommentPayload;
import com.skkil.sync.user.dto.summary.UserSummary;
import java.time.Instant;
import java.util.List;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

public class GetNotificationsResponseSnippets {

  public static GetNotificationsResponse getGetNotificationsResponse() {
    NotificationSummary notification =
        new NotificationSummary(
            1L,
            NotificationType.NEW_COMMENT,
            NotificationStatus.UNREAD,
            Instant.parse("2026-01-01T00:00:00Z"),
            new UserSummary("actor-handle", "Actor Name", "http://example.com/a.jpg"),
            NotificationEntityType.COMMENT,
            3L,
            new NewCommentPayload("actor-handle", "Actor Name", "Post Title", "post-slug"));

    return new GetNotificationsResponse(
        OffsetPaginationResponseSnippets.of(List.of(notification)), 1);
  }

  public static ResponseFieldsSnippet getNotificationsResponseFields() {
    FieldDescriptors fields =
        OffsetPaginationResponseSnippets.getPaginationResponseFields("notifications");

    fields =
        fields.andWithPrefix(
            "notifications.content[]",
            fieldWithPath(".id").type(JsonFieldType.NUMBER).description("Notification ID"),
            fieldWithPath(".type").type(JsonFieldType.STRING).description("Notification type"),
            fieldWithPath(".status").type(JsonFieldType.STRING).description("Read status"),
            fieldWithPath(".createdAt")
                .type(JsonFieldType.STRING)
                .description("Creation timestamp"),
            fieldWithPath(".actor.handle")
                .type(JsonFieldType.STRING)
                .description("Actor handle")
                .optional(),
            fieldWithPath(".actor.name")
                .type(JsonFieldType.STRING)
                .description("Actor name")
                .optional(),
            fieldWithPath(".actor.profileImageUrl")
                .type(JsonFieldType.STRING)
                .description("Actor profile image URL")
                .optional(),
            fieldWithPath(".entityType")
                .type(JsonFieldType.STRING)
                .description("Type of the linked entity")
                .optional(),
            fieldWithPath(".entityId")
                .type(JsonFieldType.NUMBER)
                .description("ID of the linked entity")
                .optional(),
            subsectionWithPath(".payload")
                .type(JsonFieldType.OBJECT)
                .description("Type-specific rendering payload (shape depends on \"type\")"));

    fields =
        fields.andWithPrefix(
            "",
            fieldWithPath("unreadCount")
                .type(JsonFieldType.NUMBER)
                .description("Total unread notification count"));

    return responseFields(fields.getFieldDescriptors());
  }
}
