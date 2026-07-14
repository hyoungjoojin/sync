package com.skkil.sync.notification.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;

import com.skkil.sync.notification.dto.request.UpdateNotificationPreferencesRequest;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.RequestFieldsSnippet;

public class UpdateNotificationPreferencesRequestSnippets {

  public static UpdateNotificationPreferencesRequest getUpdateNotificationPreferencesRequest() {
    return new UpdateNotificationPreferencesRequest(false);
  }

  public static RequestFieldsSnippet getUpdateNotificationPreferencesRequestFields() {
    return requestFields(
        fieldWithPath("inAppEnabled")
            .type(JsonFieldType.BOOLEAN)
            .description("Whether in-app notifications should be enabled"));
  }
}
