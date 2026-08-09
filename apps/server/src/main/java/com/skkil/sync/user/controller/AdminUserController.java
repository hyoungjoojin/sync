package com.skkil.sync.user.controller;

import com.skkil.sync.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminUserController {

  private final UserService userService;

  public AdminUserController(UserService userService) {
    this.userService = userService;
  }

  @PatchMapping("/admin/users/{handle}/promote")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void promoteToAdmin(@PathVariable String handle) {
    userService.promoteToAdmin(handle);
  }
}
