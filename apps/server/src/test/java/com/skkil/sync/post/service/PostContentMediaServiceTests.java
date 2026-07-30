package com.skkil.sync.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import com.skkil.sync.media.dto.MediaDto;
import com.skkil.sync.media.enums.MediaType;
import com.skkil.sync.media.model.Media;
import com.skkil.sync.media.service.domain.MediaDomainService;
import com.skkil.sync.post.model.Post;
import com.skkil.sync.post.model.PostMediaFile;
import com.skkil.sync.post.repository.PostMediaFileRepository;
import com.skkil.sync.user.model.User;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostContentMediaServiceTests {

  @Mock private MediaDomainService mediaService;
  @Mock private PostMediaFileRepository postMediaFileRepository;

  private PostContentMediaService contentMediaService;

  @Test
  @DisplayName("[resolveMediaFilesForCreate] mediaId 목록을 순서대로 연결한다")
  void resolveMediaFilesForCreate_mediaIdsGiven_linksMediaInOrder() {
    contentMediaService = new PostContentMediaService(mediaService, postMediaFileRepository);

    Long authorId = 1L;
    Media firstImage = createMedia(10L);
    Media secondImage = createMedia(11L);

    when(mediaService.linkMedia(authorId, 10L)).thenReturn(firstImage);
    when(mediaService.linkMedia(authorId, 11L)).thenReturn(secondImage);

    List<Media> mediaFiles =
        contentMediaService.resolveMediaFilesForCreate(authorId, List.of(10L, 11L));

    assertThat(mediaFiles).containsExactly(firstImage, secondImage);
  }

  @Test
  @DisplayName("[resolveMediaFilesForCreate] mediaId 목록이 비어있으면 빈 목록 반환")
  void resolveMediaFilesForCreate_noMediaIds_returnsEmptyList() {
    contentMediaService = new PostContentMediaService(mediaService, postMediaFileRepository);

    List<Media> mediaFiles = contentMediaService.resolveMediaFilesForCreate(1L, List.of());

    assertThat(mediaFiles).isEmpty();
  }

  @Test
  @DisplayName("[getPreviewMediaForPosts] 이미지만 골라 게시물별로 최대 3개까지 돌려준다")
  void getPreviewMediaForPosts_returnsUpToThreeImagesPerPost() throws Exception {
    contentMediaService = new PostContentMediaService(mediaService, postMediaFileRepository);

    Post post = Post.builder().slug("post").build();
    post.setId(1L);

    List<Media> images =
        List.of(createMedia(20L), createMedia(21L), createMedia(22L), createMedia(23L));
    List<PostMediaFile> postMediaFiles = new ArrayList<>();
    for (int sortOrder = 0; sortOrder < images.size(); sortOrder++) {
      postMediaFiles.add(new PostMediaFile(post, images.get(sortOrder), sortOrder));
    }

    when(postMediaFileRepository.findByPostIdInAndMediaTypeIn(List.of(1L), MediaType.imageTypes()))
        .thenReturn(postMediaFiles);
    when(mediaService.generatePresignedGetUrls(anyList()))
        .thenReturn(
            Map.of(
                20L, new URL("https://example.com/20.jpg"),
                21L, new URL("https://example.com/21.jpg"),
                22L, new URL("https://example.com/22.jpg")));

    Map<Long, List<MediaDto>> previewMedia =
        contentMediaService.getPreviewMediaForPosts(List.of(1L));

    assertThat(previewMedia.get(1L)).extracting(MediaDto::id).containsExactly(20L, 21L, 22L);
    assertThat(previewMedia.get(1L))
        .extracting(MediaDto::fileName, MediaDto::fileSize, MediaDto::mediaType)
        .containsExactly(
            tuple("test-20.jpg", 100L, "image/jpeg"),
            tuple("test-21.jpg", 100L, "image/jpeg"),
            tuple("test-22.jpg", 100L, "image/jpeg"));
  }

  private Media createMedia(Long id) {
    Media media =
        Media.builder()
            .uploader(new User(1L))
            .mediaType("image/jpeg")
            .bucket("test-bucket")
            .key("test-key-" + id)
            .fileName("test-" + id + ".jpg")
            .fileSize(100L)
            .build();
    media.setId(id);
    return media;
  }
}
