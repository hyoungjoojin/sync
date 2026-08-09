package com.skkil.sync.project.service;

import com.skkil.sync.media.model.Media;
import com.skkil.sync.media.repository.MediaRepository;
import com.skkil.sync.post.repository.PostRepository;
import com.skkil.sync.post.repository.PostTagRepository;
import com.skkil.sync.post.repository.TagRepository;
import com.skkil.sync.project.model.Project;
import com.skkil.sync.project.repository.ProjectRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectDeletionService {

  private final ProjectRepository projectRepository;

  private final PostRepository postRepository;

  private final PostTagRepository postTagRepository;

  private final TagRepository tagRepository;

  private final MediaRepository mediaRepository;

  public ProjectDeletionService(
      ProjectRepository projectRepository,
      PostRepository postRepository,
      PostTagRepository postTagRepository,
      TagRepository tagRepository,
      MediaRepository mediaRepository) {
    this.projectRepository = projectRepository;
    this.postRepository = postRepository;
    this.postTagRepository = postTagRepository;
    this.tagRepository = tagRepository;
    this.mediaRepository = mediaRepository;
  }

  @Transactional
  public void delete(Project project) {
    Long projectId = project.getId();
    List<Long> affectedGlobalTagIds = postTagRepository.findGlobalTagIdsByProjectId(projectId);

    deleteProjectMedia(project);
    projectRepository.delete(project);
    projectRepository.flush();

    if (!affectedGlobalTagIds.isEmpty()) {
      tagRepository.recomputePostCounts(affectedGlobalTagIds);
    }
  }

  private void deleteProjectMedia(Project project) {
    List<Long> mediaIds = new ArrayList<>(postRepository.findMediaIdsByProjectId(project.getId()));

    Media icon = project.getIcon();
    if (icon != null) {
      mediaIds.add(icon.getId());
    }

    if (!mediaIds.isEmpty()) {
      mediaRepository.markDeletedByIds(mediaIds);
    }
  }
}
