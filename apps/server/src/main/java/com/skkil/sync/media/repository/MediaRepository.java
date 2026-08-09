package com.skkil.sync.media.repository;

import com.skkil.sync.media.enums.MediaStatus;
import com.skkil.sync.media.model.Media;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface MediaRepository extends JpaRepository<Media, Long> {

  List<Media> findAllByIdIn(List<Long> mediaIds);

  List<Media> findByStatus(MediaStatus status);

  @Modifying
  @Query(
      value =
          "UPDATE media_files SET status = 'DELETED', updated_at = CURRENT_TIMESTAMP "
              + "WHERE id IN (:mediaIds)",
      nativeQuery = true)
  void markDeletedByIds(List<Long> mediaIds);

  @Modifying
  @Query(
      value =
          "UPDATE media_files SET status = 'DELETED', updated_at = CURRENT_TIMESTAMP "
              + "WHERE status = 'PENDING' AND created_at < :cutoff",
      nativeQuery = true)
  int markStalePendingMediaDeleted(Instant cutoff);
}
