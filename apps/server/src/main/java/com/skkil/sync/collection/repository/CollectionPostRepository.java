package com.skkil.sync.collection.repository;

import com.skkil.sync.collection.model.CollectionPost;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CollectionPostRepository extends JpaRepository<CollectionPost, Long> {

  boolean existsByCollectionIdAndPostId(Long collectionId, Long postId);

  Optional<CollectionPost> findByIdAndCollectionId(Long id, Long collectionId);

  /** 주어진 컬렉션들 중 해당 게시글을 담고 있는 컬렉션의 id 만 추린다. */
  @Query(
      "SELECT cp.collection.id FROM CollectionPost cp"
          + " WHERE cp.post.id = :postId AND cp.collection.id IN :collectionIds")
  List<Long> findCollectionIdsByPostIdAndCollectionIdIn(
      @Param("postId") Long postId, @Param("collectionIds") List<Long> collectionIds);
}
