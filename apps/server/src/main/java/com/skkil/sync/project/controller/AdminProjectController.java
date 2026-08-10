package com.skkil.sync.project.controller;

import com.skkil.sync.project.dto.summary.AdminProjectSummary;
import com.skkil.sync.project.service.AdminProjectService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class AdminProjectController {

  private final AdminProjectService adminProjectService;

  public AdminProjectController(AdminProjectService adminProjectService) {
    this.adminProjectService = adminProjectService;
  }

  @GetMapping("/admin/projects")
  @ResponseStatus(HttpStatus.OK)
  public AdminProjectSummary searchProject(
      @RequestParam @NotBlank @Size(min = 1, max = 100) String query) {
    return adminProjectService.searchProject(query);
  }

  @DeleteMapping("/admin/projects/{handle}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteProject(@PathVariable String handle) {
    adminProjectService.deleteProject(handle);
  }
}
