package com.skkil.sync.user.controller;

import com.skkil.sync.user.dto.request.ConfirmPasswordResetRequest;
import com.skkil.sync.user.dto.request.RequestPasswordResetRequest;
import com.skkil.sync.user.service.PasswordResetService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
class PasswordResetController {

  private final PasswordResetService passwordResetService;

  public PasswordResetController(PasswordResetService passwordResetService) {
    this.passwordResetService = passwordResetService;
  }

  @PostMapping("/auth/password-reset/request")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void requestPasswordReset(@RequestBody @Validated RequestPasswordResetRequest request) {
    passwordResetService.requestPasswordReset(request);
  }

  @PostMapping("/auth/password-reset/confirm")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void confirmPasswordReset(@RequestBody @Validated ConfirmPasswordResetRequest request) {
    passwordResetService.confirmPasswordReset(request);
  }
}
