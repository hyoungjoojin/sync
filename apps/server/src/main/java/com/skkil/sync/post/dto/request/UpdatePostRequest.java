package com.skkil.sync.post.dto.request;

import com.skkil.sync.post.constants.PostConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePostRequest(
    @NotBlank @Size(max = PostConstants.MAX_CONTENT_JSON_LENGTH) String content,
    @NotBlank @Size(max = PostConstants.MAX_CONTENT_TEXT_LENGTH) String text) {}
