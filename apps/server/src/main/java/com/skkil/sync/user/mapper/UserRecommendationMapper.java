package com.skkil.sync.user.mapper;

import com.skkil.sync.user.dto.response.GetUserRecommendationsResponse;
import com.skkil.sync.user.model.User;
import java.net.URL;
import java.util.Map;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserRecommendationMapper {

  default GetUserRecommendationsResponse.User toRecommendationUser(
      User user, Map<Long, URL> profileImageUrls) {
    var profileImage = user.getProfileImage();
    var url = profileImage != null ? profileImageUrls.get(profileImage.getId()) : null;
    return GetUserRecommendationsResponse.User.builder()
        .userId(user.getId().toString())
        .handle(user.getHandle())
        .name(user.getFullName())
        .profileImageUrl(url != null ? url.toString() : null)
        .build();
  }
}
