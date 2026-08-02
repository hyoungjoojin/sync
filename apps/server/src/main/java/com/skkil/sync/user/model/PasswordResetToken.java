package com.skkil.sync.user.model;

import com.skkil.sync.common.domain.BaseEntity;
import com.skkil.sync.user.constant.PasswordResetConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Entity
@Table(name = "password_reset_tokens")
@Getter
public class PasswordResetToken extends BaseEntity {

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", unique = true, nullable = false)
  private User user;

  @Column(name = "token_hash", nullable = false, unique = true, length = 64)
  private String tokenHash;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  protected PasswordResetToken() {}

  @Builder
  public PasswordResetToken(User user, String tokenHash) {
    this.user = user;
    this.tokenHash = tokenHash;
    this.expiresAt = Instant.now().plus(PasswordResetConstants.PASSWORD_RESET_TOKEN_TTL);
  }

  public boolean isExpired() {
    return Instant.now().isAfter(expiresAt);
  }

  public void refresh(String tokenHash) {
    this.tokenHash = tokenHash;
    this.expiresAt = Instant.now().plus(PasswordResetConstants.PASSWORD_RESET_TOKEN_TTL);
  }

  public boolean isWithinResendCooldown() {
    if (updatedAt == null) {
      return false;
    }

    return Instant.now()
        .isBefore(updatedAt.plus(PasswordResetConstants.PASSWORD_RESET_RESEND_COOLDOWN));
  }
}
