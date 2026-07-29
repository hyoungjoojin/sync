package com.skkil.sync.post.service;

import static org.mockito.Mockito.verify;

import com.skkil.sync.post.repository.PostBookmarkRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostBookmarkServiceTests {

  @Mock private PostBookmarkRepository postBookmarkRepository;

  @InjectMocks private PostBookmarkService postBookmarkService;

  @Test
  @DisplayName("[bookmarkPost] 북마크를 생성함")
  void bookmarkPost_insertsBookmark() {
    Long userId = 1L;
    Long postId = 2L;

    postBookmarkService.bookmarkPost(userId, postId);

    verify(postBookmarkRepository).insertIfAbsent(userId, postId);
  }

  @Test
  @DisplayName("[unbookmarkPost] 북마크를 삭제함")
  void unbookmarkPost_deletesBookmark() {
    Long userId = 1L;
    Long postId = 2L;

    postBookmarkService.unbookmarkPost(userId, postId);

    verify(postBookmarkRepository).deleteByUser_IdAndPost_Id(userId, postId);
  }
}
