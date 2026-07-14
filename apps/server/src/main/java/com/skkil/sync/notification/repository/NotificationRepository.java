package com.skkil.sync.notification.repository;

import com.skkil.sync.notification.constant.NotificationStatus;
import com.skkil.sync.notification.model.Notification;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

  @Query(
      """
      SELECT n FROM Notification n
      LEFT JOIN FETCH n.actor
      WHERE n.user.id = :userId
      AND (:cursor IS NULL OR n.id < :cursor)
      ORDER BY n.id DESC
      """)
  public Page<Notification> findByUser(Long userId, Pageable pageable, Long cursor);

  Optional<Notification> findByIdAndUser_Id(Long id, Long userId);

  long countByUser_IdAndStatus(Long userId, NotificationStatus status);

  @Modifying
  @Query(
      """
      UPDATE Notification n SET n.status = :newStatus, n.updatedAt = CURRENT_TIMESTAMP
      WHERE n.user.id = :userId AND n.status = :oldStatus
      """)
  void markAllAsRead(
      @Param("userId") Long userId,
      @Param("oldStatus") NotificationStatus oldStatus,
      @Param("newStatus") NotificationStatus newStatus);
}
