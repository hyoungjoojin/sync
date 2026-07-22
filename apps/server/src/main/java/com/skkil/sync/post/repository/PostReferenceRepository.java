package com.skkil.sync.post.repository;

import com.skkil.sync.post.model.PostReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostReferenceRepository extends JpaRepository<PostReference, Long> {

  @Modifying
  @Query("DELETE FROM PostReference pr WHERE pr.sourcePost.id = :sourcePostId")
  void deleteBySourcePostId(@Param("sourcePostId") Long sourcePostId);
}
