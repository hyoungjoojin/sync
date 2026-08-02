package com.skkil.sync.common.integration.captcha.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record CaptchaVerifyResponse(
    boolean success,
    @Nullable Double score,
    @Nullable String action,
    @JsonProperty("challenge_ts") @Nullable String challengeTimestamp,
    @Nullable String hostname,
    @JsonProperty("error-codes") @Nullable List<String> errorCodes) {}
