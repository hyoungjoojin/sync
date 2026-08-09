package com.skkil.sync.user.repository;

import com.skkil.sync.user.model.PasswordResetToken;
import com.skkil.sync.user.model.User;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

  Optional<PasswordResetToken> findByUser(User user);

  Optional<PasswordResetToken> findByTokenHash(String tokenHash);

  void deleteByUser(User user);

  int deleteByExpiresAtBefore(Instant instant);
}
