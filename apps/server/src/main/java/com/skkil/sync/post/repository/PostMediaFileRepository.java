package com.skkil.sync.post.repository;

import com.skkil.sync.media.enums.MediaType;
import com.skkil.sync.post.model.PostMediaFile;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PostMediaFileRepository extends JpaRepository<PostMediaFile, Long> {

  List<PostMediaFile> findAllByPostIdOrderBySortOrderAsc(Long postId);

  @Modifying
  @Query("DELETE FROM PostMediaFile postMediaFile WHERE postMediaFile.post.id = :postId")
  void deleteAllByPostId(Long postId);

  @Query(
      """
      SELECT postMediaFile FROM PostMediaFile postMediaFile
      JOIN FETCH postMediaFile.media media
      WHERE postMediaFile.post.id IN :postIds AND media.mediaType IN :mediaTypes
      ORDER BY postMediaFile.post.id ASC, postMediaFile.sortOrder ASC
      """)
  List<PostMediaFile> findByPostIdInAndMediaTypeIn(List<Long> postIds, List<MediaType> mediaTypes);
}
