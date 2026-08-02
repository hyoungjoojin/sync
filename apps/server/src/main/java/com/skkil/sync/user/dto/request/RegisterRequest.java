package com.skkil.sync.user.dto.request;

import com.skkil.sync.user.constant.PasswordConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

public record RegisterRequest(
    @NotNull @Email String email,
    @NotNull
        @Size(
            min = PasswordConstants.PASSWORD_MIN_LENGTH,
            max = PasswordConstants.PASSWORD_MAX_LENGTH)
        String password,
    @Nullable String captchaToken) {}
