package com.skkil.sync.project.mapper;

import com.skkil.sync.project.dto.data.ProjectFollowerDto;
import com.skkil.sync.project.dto.response.GetProjectFollowersResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProjectFollowerMapper {

  GetProjectFollowersResponse.Follower toFollower(ProjectFollowerDto dto);
}
