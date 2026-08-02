package com.skkil.sync.common.integration.captcha;

import com.skkil.sync.common.integration.captcha.dto.CaptchaVerifyResponse;
import com.skkil.sync.common.integration.captcha.exception.CaptchaVerificationFailedException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@Slf4j
public class CaptchaService {

  private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";
  private static final double MIN_SCORE = 0.5;

  private final String secretKey;
  private final RestClient restClient;

  public CaptchaService(@Value("${app.captcha.secret-key}") String secretKey) {
    this.secretKey = secretKey;
    this.restClient = RestClient.builder().build();
  }

  public void verify(@Nullable String token, String expectedAction) {
    if (secretKey == null || secretKey.isBlank()) {
      log.warn("Captcha secret key is not configured. Skipping captcha verification.");
      return;
    }

    if (token == null || token.isBlank()) {
      throw new CaptchaVerificationFailedException();
    }

    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("secret", secretKey);
    form.add("response", token);

    CaptchaVerifyResponse response;
    try {
      response =
          restClient
              .post()
              .uri(VERIFY_URL)
              .contentType(MediaType.APPLICATION_FORM_URLENCODED)
              .body(form)
              .retrieve()
              .body(CaptchaVerifyResponse.class);
    } catch (RestClientException e) {
      log.warn("Failed to reach captcha verify endpoint: {}", e.getMessage());
      throw new CaptchaVerificationFailedException(e);
    }

    if (response == null
        || !response.success()
        || response.score() == null
        || response.score() < MIN_SCORE
        || !expectedAction.equals(response.action())) {
      log.warn("Captcha verification rejected: {}", response);
      throw new CaptchaVerificationFailedException();
    }
  }
}
