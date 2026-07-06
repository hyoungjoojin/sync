package com.skkil.sync.common.devtools.seed;

import com.skkil.sync.post.model.PostType;
import com.skkil.sync.user.model.User;
import com.skkil.sync.user.repository.UserRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@Slf4j
class DataSeeder implements ApplicationRunner {

  private final UserRepository userRepository;
  private final UserSeeder userSeeder;
  private final ProjectSeeder projectSeeder;
  private final PostSeeder postSeeder;
  private final SocialGraphSeeder socialGraphSeeder;

  DataSeeder(
      UserRepository userRepository,
      UserSeeder userSeeder,
      ProjectSeeder projectSeeder,
      PostSeeder postSeeder,
      SocialGraphSeeder socialGraphSeeder) {
    this.userRepository = userRepository;
    this.userSeeder = userSeeder;
    this.projectSeeder = projectSeeder;
    this.postSeeder = postSeeder;
    this.socialGraphSeeder = socialGraphSeeder;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (!shouldRun()) {
      log.info("Data seeding skipped");
      return;
    }

    log.info("Seeding local dev data");

    User alice =
        userSeeder.seed(
            "alice@example.com",
            "password1234!",
            "alicekim",
            "앨리스 김",
            "백엔드 엔지니어",
            "분산 시스템에 관심이 많습니다.");

    User bob =
        userSeeder.seed(
            "bob@example.com",
            "password1234!",
            "bobleedev",
            "밥 이",
            "프론트엔드 엔지니어",
            "React와 디자인 시스템을 좋아합니다.");

    User carol =
        userSeeder.seed(
            "carol@example.com",
            "password1234!",
            "carolpark",
            "캐롤 박",
            "프로덕트 디자이너",
            "UX 리서치를 담당하고 있습니다.");

    User dave =
        userSeeder.seed(
            "dave@example.com", "password1234!", "davechoi", "데이브 최", "ML 엔지니어", "추천 시스템을 만듭니다.");

    projectSeeder.seed(alice, "sync", "Sync", "팀 협업을 위한 소셜 프로젝트 플랫폼입니다.");
    projectSeeder.addTeammate(alice, "sync", "bobleedev");

    projectSeeder.seed(carol, "recipe-app", "Recipe App", "레시피 공유 커뮤니티 사이드 프로젝트입니다.");

    String syncLaunchPost =
        postSeeder.seed(
            alice,
            "Sync 프로젝트를 시작합니다",
            PostType.LONG,
            "Sync는 개발자들이 사이드 프로젝트를 함께 만들고 공유할 수 있는 플랫폼입니다.",
            List.of("spring-boot"),
            "sync");

    postSeeder.seed(
        carol, "Recipe App 진행 상황", PostType.SHORT, "이번 주에는 검색 기능을 붙였습니다.", List.of(), "recipe-app");

    String recruitmentPost =
        postSeeder.seed(
            dave,
            "프론트엔드 개발자를 찾습니다",
            PostType.QUESTION,
            "React 경험이 있는 프론트엔드 개발자를 찾고 있습니다.",
            List.of("react"),
            null);

    socialGraphSeeder.like(bob, syncLaunchPost);
    socialGraphSeeder.like(dave, syncLaunchPost);
    socialGraphSeeder.comment(bob, syncLaunchPost, "멋진 프로젝트네요! 응원합니다.");
    socialGraphSeeder.comment(carol, recruitmentPost, "저도 참여하고 싶어요.");

    socialGraphSeeder.followUser(bob, alice);
    socialGraphSeeder.followUser(carol, alice);
    socialGraphSeeder.followProject(dave, "sync");
    socialGraphSeeder.followProject(bob, "recipe-app");

    log.info("Finished seeding local dev data");
  }

  private boolean shouldRun() {
    return userRepository.count() == 0;
  }
}
