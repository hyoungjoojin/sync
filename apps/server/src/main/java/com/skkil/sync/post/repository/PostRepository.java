package com.skkil.sync.post.repository;

import com.skkil.sync.post.model.Post;
import com.skkil.sync.post.model.PostVisibility;
import com.skkil.sync.project.model.Project;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PostRepository extends JpaRepository<Post, Long> {

  Optional<Post> findBySlug(String slug);

  Optional<Post> findByIdAndVisibility(Long id, PostVisibility visibility);

  @Query("SELECT p FROM Post p LEFT JOIN FETCH p.project WHERE p.id = :id")
  Optional<Post> findByIdWithProject(Long id);

  long countByProjectAndPinnedAtIsNotNull(Project project);

  @Modifying
  @Query(
      value =
          "INSERT INTO post_activities(user_id, date, count) VALUES (:userId, :date, 1) "
              + "ON CONFLICT (user_id, date) DO UPDATE SET count = post_activities.count + 1",
      nativeQuery = true)
  void incrementActivityCount(Long userId, LocalDate date);

  @Modifying
  @Query(
      "UPDATE Post p SET p.isSeriesPost = false WHERE p.id IN "
          + "(SELECT sp.post.id FROM PostSeriesPost sp WHERE sp.series.id = :seriesId)")
  void clearSeriesFlagBySeriesId(Long seriesId);

  @Query(
      value =
          """
          SELECT cover_media_id
          FROM posts
          WHERE project_id = :projectId AND cover_media_id IS NOT NULL

          UNION

          SELECT pmf.media_id
          FROM post_media_files pmf
          JOIN posts p ON p.id = pmf.post_id
          WHERE p.project_id = :projectId
          """,
      nativeQuery = true)
  List<Long> findMediaIdsByProjectId(Long projectId);
}
