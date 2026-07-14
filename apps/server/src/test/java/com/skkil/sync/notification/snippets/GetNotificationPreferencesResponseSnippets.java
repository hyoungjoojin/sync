package com.skkil.sync.notification.snippets;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.skkil.sync.notification.dto.response.GetNotificationPreferencesResponse;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

public class GetNotificationPreferencesResponseSnippets {

  public static GetNotificationPreferencesResponse getGetNotificationPreferencesResponse() {
    return new GetNotificationPreferencesResponse(true);
  }

  public static ResponseFieldsSnippet getNotificationPreferencesResponseFields() {
    return responseFields(
        fieldWithPath("inAppEnabled")
            .type(JsonFieldType.BOOLEAN)
            .description("Whether in-app notifications are enabled"));
  }
}
