package com.skkil.sync.user.mapper;

import com.skkil.sync.media.model.Media;
import com.skkil.sync.media.service.domain.MediaDomainService;
import com.skkil.sync.user.dto.summary.AdminUserSummary;
import com.skkil.sync.user.model.User;
import java.net.URL;
import org.springframework.stereotype.Component;

@Component
public class AdminUserAssembler {

  private final MediaDomainService mediaDomainService;

  public AdminUserAssembler(MediaDomainService mediaDomainService) {
    this.mediaDomainService = mediaDomainService;
  }

  public AdminUserSummary toAdminUserSummary(User user) {
    Media profileImage = user.getProfileImage();
    URL url =
        profileImage != null ? mediaDomainService.generatePresignedGetUrl(profileImage) : null;

    return AdminUserSummary.builder()
        .id(user.getId())
        .handle(user.getHandle())
        .name(user.getFullName())
        .email(user.getEmail())
        .profileImageUrl(url != null ? url.toExternalForm() : null)
        .role(user.getRole())
        .isOnboarded(Boolean.TRUE.equals(user.getIsOnboarded()))
        .isEmailVerified(Boolean.TRUE.equals(user.getIsEmailVerified()))
        .followerCount(user.getFollowerCount())
        .followingCount(user.getFollowingCount())
        .createdAt(user.getCreatedAt())
        .deletedAt(user.getDeletedAt())
        .build();
  }
}
