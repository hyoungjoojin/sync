package com.skkil.sync.user.constant;

import java.time.Duration;

public class PasswordResetConstants {

  public static final int PASSWORD_RESET_TOKEN_BYTE_LENGTH = 32;

  public static final int PASSWORD_RESET_TOKEN_MAX_LENGTH = 255;

  public static final Duration PASSWORD_RESET_TOKEN_TTL = Duration.ofMinutes(30);

  public static final Duration PASSWORD_RESET_RESEND_COOLDOWN = Duration.ofSeconds(60);
}
