package com.skkil.sync.user.dto.request;

import com.skkil.sync.user.constant.PasswordConstants;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

public record ChangePasswordRequest(
    @Size(max = PasswordConstants.PASSWORD_MAX_LENGTH) @Nullable String currentPassword,
    @NotNull
        @Size(
            min = PasswordConstants.PASSWORD_MIN_LENGTH,
            max = PasswordConstants.PASSWORD_MAX_LENGTH)
        String newPassword) {}
