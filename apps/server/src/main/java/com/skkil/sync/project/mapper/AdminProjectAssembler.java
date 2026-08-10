package com.skkil.sync.project.mapper;

import com.skkil.sync.media.model.Media;
import com.skkil.sync.media.service.domain.MediaDomainService;
import com.skkil.sync.project.dto.summary.AdminProjectSummary;
import com.skkil.sync.project.model.Project;
import com.skkil.sync.project.model.Teammate;
import com.skkil.sync.project.repository.TeammateRepository;
import com.skkil.sync.user.dto.summary.UserSummary;
import com.skkil.sync.user.mapper.UserAssembler;
import java.net.URL;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AdminProjectAssembler {

  private final MediaDomainService mediaDomainService;

  private final TeammateRepository teammateRepository;

  private final UserAssembler userAssembler;

  public AdminProjectAssembler(
      MediaDomainService mediaDomainService,
      TeammateRepository teammateRepository,
      UserAssembler userAssembler) {
    this.mediaDomainService = mediaDomainService;
    this.teammateRepository = teammateRepository;
    this.userAssembler = userAssembler;
  }

  public AdminProjectSummary toAdminProjectSummary(Project project) {
    Media icon = project.getIcon();
    URL iconUrl = icon != null ? mediaDomainService.generatePresignedGetUrl(icon) : null;

    long teammateCount = teammateRepository.countByProjectId(project.getId());

    return AdminProjectSummary.builder()
        .id(project.getId())
        .handle(project.getHandle())
        .name(project.getName())
        .description(project.getDescription())
        .website(project.getWebsite())
        .isPublic(project.isPublic())
        .joinPolicy(project.getJoinPolicy())
        .followerCount(project.getFollowerCount())
        .teammateCount(teammateCount)
        .owner(findOwner(project.getId()))
        .createdAt(project.getCreatedAt())
        .iconUrl(iconUrl != null ? iconUrl.toExternalForm() : null)
        .build();
  }

  private UserSummary findOwner(Long projectId) {
    return teammateRepository.findOwnersByProjectIds(List.of(projectId)).stream()
        .findFirst()
        .map(Teammate::getUser)
        .map(user -> userAssembler.toUserSummary(user.getId()))
        .orElse(null);
  }
}
