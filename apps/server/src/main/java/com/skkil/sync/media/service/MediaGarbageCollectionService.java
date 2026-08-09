package com.skkil.sync.media.service;

import com.skkil.sync.media.enums.MediaStatus;
import com.skkil.sync.media.model.Media;
import com.skkil.sync.media.repository.MediaRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

@Service
@Slf4j
public class MediaGarbageCollectionService {

  private static final Duration PENDING_MEDIA_TTL = Duration.ofHours(24);

  private final MediaRepository mediaRepository;
  private final S3Client s3Client;

  public MediaGarbageCollectionService(MediaRepository mediaRepository, S3Client s3Client) {
    this.mediaRepository = mediaRepository;
    this.s3Client = s3Client;
  }

  @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
  @Transactional
  public void markStalePendingMediaDeleted() {
    Instant cutoff = Instant.now().minus(PENDING_MEDIA_TTL);
    int marked = mediaRepository.markStalePendingMediaDeleted(cutoff);

    if (marked > 0) {
      log.info("Marked {} stale pending media files as deleted", marked);
    }
  }

  /**
   * Every path that retires a media file — project deletion, icon/avatar replacement, or the
   * pending-timeout sweep above — only ever flips its status to DELETED. This is the single place
   * that actually reclaims the S3 object; the row is only hard-deleted once that succeeds, so a
   * failed delete leaves the row behind for the next run to retry.
   */
  @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
  public void reclaimDeletedMedia() {
    List<Media> deletedMedia = mediaRepository.findByStatus(MediaStatus.DELETED);

    int reclaimed = 0;
    for (Media media : deletedMedia) {
      if (deleteObject(media)) {
        mediaRepository.delete(media);
        reclaimed++;
      }
    }

    if (reclaimed > 0) {
      log.info("Reclaimed {} of {} deleted media files", reclaimed, deletedMedia.size());
    }
  }

  private boolean deleteObject(Media media) {
    try {
      s3Client.deleteObject(
          DeleteObjectRequest.builder().bucket(media.getBucket()).key(media.getKey()).build());

      return true;
    } catch (RuntimeException exception) {
      log.warn("Failed to delete S3 object for media with ID {}", media.getId(), exception);

      return false;
    }
  }
}
