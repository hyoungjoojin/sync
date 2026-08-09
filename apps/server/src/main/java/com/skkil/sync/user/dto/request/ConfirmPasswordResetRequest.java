package com.skkil.sync.user.dto.request;

import com.skkil.sync.user.constant.PasswordConstants;
import com.skkil.sync.user.constant.PasswordResetConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ConfirmPasswordResetRequest(
    @NotBlank @Size(max = PasswordResetConstants.PASSWORD_RESET_TOKEN_MAX_LENGTH) String token,
    @NotNull
        @Size(
            min = PasswordConstants.PASSWORD_MIN_LENGTH,
            max = PasswordConstants.PASSWORD_MAX_LENGTH)
        String newPassword) {}
