package com.skkil.sync.post.repository;

import com.skkil.sync.post.model.PostMediaFile;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostMediaFileRepository extends JpaRepository<PostMediaFile, Long> {

  List<PostMediaFile> findAllByPostIdOrderBySortOrderAsc(Long postId);

  List<PostMediaFile> findByPostIdInAndSortOrderLessThanOrderByPostIdAscSortOrderAsc(
      List<Long> postIds, int sortOrderExclusiveUpperBound);

  int countByPostId(Long postId);
}
