package com.skkil.sync.collection.security;

import com.skkil.sync.auth.AuthenticatedUser;
import com.skkil.sync.collection.model.Collection;
import com.skkil.sync.collection.repository.CollectionRepository;
import com.skkil.sync.common.security.CustomPermissionEvaluator;
import com.skkil.sync.common.security.PermissionOperation;
import com.skkil.sync.common.security.enums.PermissionEvaluatorType;
import com.skkil.sync.project.model.Project;
import com.skkil.sync.project.model.Teammate;
import com.skkil.sync.project.repository.TeammateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CollectionPermissionEvaluator implements CustomPermissionEvaluator<String> {

  private final CollectionRepository collectionRepository;
  private final TeammateRepository teammateRepository;

  public CollectionPermissionEvaluator(
      CollectionRepository collectionRepository, TeammateRepository teammateRepository) {
    this.collectionRepository = collectionRepository;
    this.teammateRepository = teammateRepository;
  }

  @Override
  public PermissionEvaluatorType type() {
    return PermissionEvaluatorType.COLLECTION;
  }

  @Override
  public boolean hasPermission(
      AuthenticatedUser user, String externalId, PermissionOperation permission) {
    Collection collection =
        collectionRepository.findByExternalIdWithProject(externalId).orElse(null);
    if (collection == null) {
      log.debug("Collection with external id {} not found", externalId);
      return false;
    }

    return switch (permission) {
      case READ -> canRead(user, collection);
      // 편집·삭제·게시글 추가/제거는 모두 동일한 "관리 권한"을 요구한다.
      case EDIT, DELETE -> canManage(user, collection);

      default -> {
        log.debug("Unsupported permission operation: {}", permission);
        yield false;
      }
    };
  }

  private boolean canRead(AuthenticatedUser user, Collection collection) {
    if (collection.isPersonal()) {
      if (collection.isPublic()) {
        return true;
      }
      return isCreator(user, collection);
    }

    // WORKSPACE 컬렉션: 프로젝트가 공개이고 컬렉션도 공개일 때만 비팀원에게 노출된다.
    Project project = collection.getProject();
    if (collection.isPublic() && project != null && project.isPublic()) {
      return true;
    }

    if (user == null) {
      return false;
    }

    if (isCreator(user, collection)) {
      return true;
    }

    return isTeammate(user, collection);
  }

  private boolean canManage(AuthenticatedUser user, Collection collection) {
    if (user == null) {
      log.debug("Unauthenticated user cannot manage collection");
      return false;
    }

    if (collection.isPersonal()) {
      // 개인 컬렉션은 생성자만 관리할 수 있다.
      return isCreator(user, collection);
    }

    // WORKSPACE 컬렉션은 프로젝트를 관리할 수 있는 팀원(admin·owner)이 큐레이션한다.
    Project project = collection.getProject();
    if (project == null) {
      return false;
    }

    return teammateRepository
        .findByProjectIdAndUserId(project.getId(), user.userId())
        .map(Teammate::canManageProject)
        .orElse(false);
  }

  private boolean isCreator(AuthenticatedUser user, Collection collection) {
    return user != null && user.userId().equals(collection.getCreator().getId());
  }

  private boolean isTeammate(AuthenticatedUser user, Collection collection) {
    Project project = collection.getProject();
    if (project == null) {
      return false;
    }
    return teammateRepository.findByProjectIdAndUserId(project.getId(), user.userId()).isPresent();
  }
}
