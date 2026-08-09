package com.skkil.sync.media.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.skkil.sync.media.enums.MediaStatus;
import com.skkil.sync.media.model.Media;
import com.skkil.sync.media.repository.MediaRepository;
import com.skkil.sync.user.model.User;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MediaGarbageCollectionServiceTests {

  private static final Long UPLOADER_ID = 1L;

  @Mock private MediaRepository mediaRepository;
  @Mock private S3Client s3Client;

  @Test
  @DisplayName("[markStalePendingMediaDeleted] 기준 시각보다 24시간 이전에 생성된 PENDING 미디어를 DELETED로 바꾼다")
  void markStalePendingMediaDeleted_queriesWithCutoff24HoursAgo() {
    when(mediaRepository.markStalePendingMediaDeleted(any())).thenReturn(0);

    Instant beforeRun = Instant.now();
    service().markStalePendingMediaDeleted();
    Instant afterRun = Instant.now();

    ArgumentCaptor<Instant> cutoffCaptor = ArgumentCaptor.forClass(Instant.class);
    verify(mediaRepository).markStalePendingMediaDeleted(cutoffCaptor.capture());

    Instant cutoff = cutoffCaptor.getValue();
    assertThat(cutoff).isBefore(beforeRun.minusSeconds(23 * 3600));
    assertThat(cutoff).isAfter(afterRun.minusSeconds(25 * 3600));
  }

  @Test
  @DisplayName("[reclaimDeletedMedia] DELETED 미디어는 S3 객체를 지우고 행을 완전히 삭제한다")
  void reclaimDeletedMedia_deletedMedia_deletesObjectAndPurgesRow() {
    Media deleted = deletedMedia(10L);
    when(mediaRepository.findByStatus(MediaStatus.DELETED)).thenReturn(List.of(deleted));

    service().reclaimDeletedMedia();

    verify(s3Client)
        .deleteObject(
            DeleteObjectRequest.builder()
                .bucket(deleted.getBucket())
                .key(deleted.getKey())
                .build());
    verify(mediaRepository).delete(deleted);
  }

  @Test
  @DisplayName("[reclaimDeletedMedia] S3 삭제에 실패한 미디어는 행을 남겨두고 다음 미디어를 계속 처리한다")
  void reclaimDeletedMedia_s3DeleteFails_skipsRowAndContinues() {
    Media failing = deletedMedia(11L);
    Media succeeding = deletedMedia(12L);
    when(mediaRepository.findByStatus(MediaStatus.DELETED))
        .thenReturn(List.of(failing, succeeding));
    when(s3Client.deleteObject(
            DeleteObjectRequest.builder()
                .bucket(failing.getBucket())
                .key(failing.getKey())
                .build()))
        .thenThrow(NoSuchKeyException.builder().message("boom").build());

    service().reclaimDeletedMedia();

    verify(mediaRepository, never()).delete(failing);
    verify(mediaRepository, times(1)).delete(succeeding);
  }

  private MediaGarbageCollectionService service() {
    return new MediaGarbageCollectionService(mediaRepository, s3Client);
  }

  private Media deletedMedia(Long id) {
    Media media =
        Media.builder()
            .uploader(new User(UPLOADER_ID))
            .mediaType("image/png")
            .bucket("test-bucket")
            .key("test-key-" + id)
            .fileName("test-file")
            .fileSize(1024L)
            .build();
    media.setId(id);
    media.markAsDeleted();

    return media;
  }
}
