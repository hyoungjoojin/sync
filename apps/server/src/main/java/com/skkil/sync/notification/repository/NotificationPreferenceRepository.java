package com.skkil.sync.notification.repository;

import com.skkil.sync.notification.model.NotificationPreference;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationPreferenceRepository
    extends JpaRepository<NotificationPreference, Long> {

  Optional<NotificationPreference> findByUserId(Long userId);
}
