package com.skkil.sync.post.mapper;

import com.skkil.sync.post.dto.response.GetTagsResponse;
import com.skkil.sync.post.model.Tag;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TagMapper {

  GetTagsResponse.Tag toTag(Tag tag);
}
