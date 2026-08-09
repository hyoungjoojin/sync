package com.skkil.sync.post.controller;

import com.skkil.sync.post.dto.request.CreateTagRequest;
import com.skkil.sync.post.dto.response.CreateTagResponse;
import com.skkil.sync.post.dto.response.GetTagsResponse;
import com.skkil.sync.post.service.TagService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminTagController {

  private final TagService tagService;

  public AdminTagController(TagService tagService) {
    this.tagService = tagService;
  }

  @GetMapping("/admin/tags")
  @ResponseStatus(HttpStatus.OK)
  public GetTagsResponse getAllTags() {
    return tagService.getAllTagsForAdmin();
  }

  @PostMapping("/admin/tags")
  @ResponseStatus(HttpStatus.CREATED)
  public CreateTagResponse createTag(@RequestBody @Validated CreateTagRequest request) {
    return tagService.createTag(request);
  }

  @PatchMapping("/admin/tags/{name}/verify")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void verifyTag(@PathVariable String name) {
    tagService.verifyTag(name);
  }

  @PatchMapping("/admin/tags/{name}/unverify")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void unverifyTag(@PathVariable String name) {
    tagService.unverifyTag(name);
  }

  @DeleteMapping("/admin/tags/{name}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteTag(@PathVariable String name) {
    tagService.rejectTag(name);
  }
}
