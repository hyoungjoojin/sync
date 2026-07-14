package com.skkil.sync.notification.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;

import com.skkil.sync.notification.constant.NotificationEntityType;
import com.skkil.sync.notification.constant.NotificationStatus;
import com.skkil.sync.notification.constant.NotificationType;
import com.skkil.sync.notification.dto.data.NotificationSummary;
import com.skkil.sync.notification.dto.response.GetNotificationsResponse;
import com.skkil.sync.notification.model.NewCommentPayload;
import com.skkil.sync.user.dto.summary.UserSummary;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
        new PageImpl<>(List.of(notification), PageRequest.ofSize(10), 1), 1);
  }

  public static ResponseFieldsSnippet getNotificationsResponseFields() {
    return responseFields(
        fieldWithPath("notifications.content[].id")
            .type(JsonFieldType.NUMBER)
            .description("Notification ID"),
        fieldWithPath("notifications.content[].type")
            .type(JsonFieldType.STRING)
            .description("Notification type"),
        fieldWithPath("notifications.content[].status")
            .type(JsonFieldType.STRING)
            .description("Read status"),
        fieldWithPath("notifications.content[].createdAt")
            .type(JsonFieldType.STRING)
            .description("Creation timestamp"),
        fieldWithPath("notifications.content[].actor.handle")
            .type(JsonFieldType.STRING)
            .description("Actor handle")
            .optional(),
        fieldWithPath("notifications.content[].actor.name")
            .type(JsonFieldType.STRING)
            .description("Actor name")
            .optional(),
        fieldWithPath("notifications.content[].actor.profileImageUrl")
            .type(JsonFieldType.STRING)
            .description("Actor profile image URL")
            .optional(),
        fieldWithPath("notifications.content[].entityType")
            .type(JsonFieldType.STRING)
            .description("Type of the linked entity")
            .optional(),
        fieldWithPath("notifications.content[].entityId")
            .type(JsonFieldType.NUMBER)
            .description("ID of the linked entity")
            .optional(),
        subsectionWithPath("notifications.content[].payload")
            .type(JsonFieldType.OBJECT)
            .description("Type-specific rendering payload (shape depends on \"type\")"),
        subsectionWithPath("notifications.pageable")
            .type(JsonFieldType.OBJECT)
            .description("Pagination request info"),
        fieldWithPath("notifications.last").type(JsonFieldType.BOOLEAN).description("Is last page"),
        fieldWithPath("notifications.totalPages")
            .type(JsonFieldType.NUMBER)
            .description("Total number of pages"),
        fieldWithPath("notifications.totalElements")
            .type(JsonFieldType.NUMBER)
            .description("Total number of notifications"),
        fieldWithPath("notifications.first")
            .type(JsonFieldType.BOOLEAN)
            .description("Is first page"),
        fieldWithPath("notifications.size").type(JsonFieldType.NUMBER).description("Page size"),
        fieldWithPath("notifications.number")
            .type(JsonFieldType.NUMBER)
            .description("Current page number"),
        subsectionWithPath("notifications.sort")
            .type(JsonFieldType.OBJECT)
            .description("Sort info"),
        fieldWithPath("notifications.numberOfElements")
            .type(JsonFieldType.NUMBER)
            .description("Number of elements in the current page"),
        fieldWithPath("notifications.empty")
            .type(JsonFieldType.BOOLEAN)
            .description("Whether the page is empty"),
        fieldWithPath("unreadCount")
            .type(JsonFieldType.NUMBER)
            .description("Total unread notification count"));
  }
}
