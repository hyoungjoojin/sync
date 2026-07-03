package com.skkil.sync.user.mapper;

import com.skkil.sync.user.dto.data.UserConnectionDto;
import com.skkil.sync.user.dto.response.GetConnectionsResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserConnectionMapper {

  GetConnectionsResponse.Connection toConnection(UserConnectionDto dto);
}
