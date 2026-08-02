package com.skkil.sync.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record RequestPasswordResetRequest(@NotNull @Email String email) {}
