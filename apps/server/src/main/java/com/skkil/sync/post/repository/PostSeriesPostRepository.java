package com.skkil.sync.post.repository;

import com.skkil.sync.post.model.PostSeriesPost;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostSeriesPostRepository extends JpaRepository<PostSeriesPost, Long> {

  List<PostSeriesPost> findBySeriesIdOrderByPositionAscIdAsc(Long seriesId);

  boolean existsBySeriesIdAndPostId(Long seriesId, Long postId);

  boolean existsByPostId(Long postId);

  long countBySeriesId(Long seriesId);

  Optional<PostSeriesPost> findByIdAndSeriesId(Long id, Long seriesId);

  Optional<PostSeriesPost> findByPostId(Long postId);

  @Query(
      "SELECT COALESCE(MAX(sp.position), 0) FROM PostSeriesPost sp WHERE sp.series.id = :seriesId")
  int findMaxPosition(@Param("seriesId") Long seriesId);

  @Modifying(flushAutomatically = true)
  @Query(
      "UPDATE PostSeriesPost sp SET sp.position = sp.position + 1"
          + " WHERE sp.series.id = :seriesId AND sp.position >= :from")
  void shiftUpFrom(@Param("seriesId") Long seriesId, @Param("from") int from);

  @Modifying(flushAutomatically = true)
  @Query(
      "UPDATE PostSeriesPost sp SET sp.position = sp.position - 1"
          + " WHERE sp.series.id = :seriesId AND sp.position > :position")
  void shiftDownAfter(@Param("seriesId") Long seriesId, @Param("position") int position);

  @Modifying(flushAutomatically = true)
  @Query(
      "UPDATE PostSeriesPost sp SET sp.position = sp.position + 1"
          + " WHERE sp.series.id = :seriesId AND sp.position >= :from AND sp.position < :to")
  void shiftUpBetween(
      @Param("seriesId") Long seriesId, @Param("from") int from, @Param("to") int to);

  @Modifying(flushAutomatically = true)
  @Query(
      "UPDATE PostSeriesPost sp SET sp.position = sp.position - 1"
          + " WHERE sp.series.id = :seriesId AND sp.position > :from AND sp.position <= :to")
  void shiftDownBetween(
      @Param("seriesId") Long seriesId, @Param("from") int from, @Param("to") int to);
}
