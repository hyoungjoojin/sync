package com.skkil.sync.user.controller;

import com.skkil.sync.auth.AuthenticatedUser;
import com.skkil.sync.user.dto.summary.AdminUserSummary;
import com.skkil.sync.user.service.AdminUserService;
import com.skkil.sync.user.service.UserService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class AdminUserController {

  private final UserService userService;

  private final AdminUserService adminUserService;

  public AdminUserController(UserService userService, AdminUserService adminUserService) {
    this.userService = userService;
    this.adminUserService = adminUserService;
  }

  @GetMapping("/admin/users")
  @ResponseStatus(HttpStatus.OK)
  public AdminUserSummary searchUser(
      @RequestParam @NotBlank @Size(min = 1, max = 100) String query) {
    return adminUserService.searchUser(query);
  }

  @PatchMapping("/admin/users/{handle}/promote")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void promoteToAdmin(@PathVariable String handle) {
    userService.promoteToAdmin(handle);
  }

  @DeleteMapping("/admin/users/{handle}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteUser(
      @AuthenticationPrincipal AuthenticatedUser user, @PathVariable String handle) {
    adminUserService.deleteUser(user.userId(), handle);
  }
}
