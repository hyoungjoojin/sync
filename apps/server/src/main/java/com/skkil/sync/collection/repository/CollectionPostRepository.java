package com.skkil.sync.collection.repository;

import com.skkil.sync.collection.dto.data.CollectionMembershipDto;
import com.skkil.sync.collection.model.CollectionPost;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CollectionPostRepository extends JpaRepository<CollectionPost, Long> {

  boolean existsByCollectionIdAndPostId(Long collectionId, Long postId);

  Optional<CollectionPost> findByIdAndCollectionId(Long id, Long collectionId);

  /** 주어진 컬렉션들 중 해당 게시글을 담고 있는 컬렉션의 멤버십(컬렉션 외부 식별자, 항목 id)만 추린다. */
  @Query(
      "SELECT new com.skkil.sync.collection.dto.data.CollectionMembershipDto(cp.collection.externalId,"
          + " cp.id) FROM CollectionPost cp"
          + " WHERE cp.post.id = :postId AND cp.collection.id IN :collectionIds")
  List<CollectionMembershipDto> findMembershipsByPostIdAndCollectionIdIn(
      @Param("postId") Long postId, @Param("collectionIds") List<Long> collectionIds);
}
