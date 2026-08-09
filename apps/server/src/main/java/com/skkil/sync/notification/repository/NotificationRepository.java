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

  /**
   * 정렬 기준은 {@code idx_notifications_user_created_id (user_id, created_at DESC, id DESC)}와 정확히 일치해야
   * 한다. {@code id DESC}만으로 정렬하면 플래너가 이 인덱스 대신 기본 키를 역방향으로 훑으면서 다른 사용자의 행을 대량으로 버리게 된다. 같은 트랜잭션에서
   * 생성된 알림은 {@code created_at}이 동일하므로 {@code id DESC}가 순서를 확정한다.
   */
  @Query(
      """
      SELECT n FROM Notification n
      LEFT JOIN FETCH n.actor
      WHERE n.user.id = :userId
      ORDER BY n.createdAt DESC, n.id DESC
      """)
  public Page<Notification> findByUser(Long userId, Pageable pageable);

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
