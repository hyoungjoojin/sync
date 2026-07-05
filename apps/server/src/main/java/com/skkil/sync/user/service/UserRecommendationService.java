package com.skkil.sync.user.service;

import com.skkil.sync.media.service.domain.MediaDomainService;
import com.skkil.sync.user.dto.response.GetUserRecommendationsResponse;
import com.skkil.sync.user.mapper.UserRecommendationMapper;
import com.skkil.sync.user.model.User;
import com.skkil.sync.user.repository.UserRecommendationQueryRepository;
import com.skkil.sync.user.repository.UserRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserRecommendationService {

  private static final int CANDIDATES_PER_SIGNAL = 35;

  // Explore 페이지의 캐러셀에 노출할 추천 사용자 수
  public static final int RECOMMENDATION_LIMIT = 15;

  private final UserRecommendationQueryRepository userRecommendationQueryRepository;
  private final UserRepository userRepository;
  private final MediaDomainService mediaDomainService;
  private final UserRecommendationMapper userRecommendationMapper;

  public UserRecommendationService(
      UserRecommendationQueryRepository userRecommendationQueryRepository,
      UserRepository userRepository,
      MediaDomainService mediaDomainService,
      UserRecommendationMapper userRecommendationMapper) {
    this.userRecommendationQueryRepository = userRecommendationQueryRepository;
    this.userRepository = userRepository;
    this.mediaDomainService = mediaDomainService;
    this.userRecommendationMapper = userRecommendationMapper;
  }

  @Transactional(readOnly = true)
  public GetUserRecommendationsResponse getRecommendations(Long userId) {
    var projectOverlapIds =
        userRecommendationQueryRepository.findProjectOverlapCandidateIds(
            userId, CANDIDATES_PER_SIGNAL);
    var trendingIds =
        userRecommendationQueryRepository.findTrendingCandidateIds(userId, CANDIDATES_PER_SIGNAL);

    var candidateIds = new LinkedHashSet<Long>();
    candidateIds.addAll(projectOverlapIds);
    candidateIds.addAll(trendingIds);

    var orderedIds = candidateIds.stream().limit(RECOMMENDATION_LIMIT).toList();

    Map<Long, User> usersById =
        userRepository.findAllById(orderedIds).stream()
            .collect(Collectors.toMap(User::getId, user -> user));

    var users = orderedIds.stream().map(usersById::get).filter(user -> user != null).toList();

    var profileImageUrls = mediaDomainService.generatePublicGetUrls(users, User::getProfileImage);

    List<GetUserRecommendationsResponse.User> recommendations =
        users.stream()
            .map(user -> userRecommendationMapper.toRecommendationUser(user, profileImageUrls))
            .toList();

    return new GetUserRecommendationsResponse(recommendations);
  }
}
