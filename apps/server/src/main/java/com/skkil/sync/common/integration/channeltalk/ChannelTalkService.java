package com.skkil.sync.common.integration.channeltalk;

import com.skkil.sync.common.integration.channeltalk.exception.ChannelTalkMemberHashFailedException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ChannelTalkService {

  private static final String HMAC_ALGORITHM = "HmacSHA256";

  private final String secretKey;

  public ChannelTalkService(@Value("${app.channel-talk.secret-key}") String secretKey) {
    this.secretKey = secretKey;
  }

  /**
   * 채널톡 SDK가 특정 멤버로 부팅하는 것을 검증하기 위한 member hash를 생성한다. 시크릿 키는 hex 문자열로 발급되므로 HMAC 키로 쓰기 전에 반드시 원시
   * 바이트로 디코딩해야 한다. 이 단계를 빠뜨리면 검증이 조용히 실패하고 클라이언트는 unauthenticated BootStatus를 받는다.
   *
   * <p>키가 설정되지 않은 환경(로컬 등)에서는 null을 반환해 클라이언트가 익명으로 부팅하도록 둔다.
   */
  public @Nullable String generateMemberHash(String memberId) {
    if (secretKey == null || secretKey.isBlank()) {
      log.warn("Channel Talk secret key is not configured. Skipping member hash generation.");
      return null;
    }

    try {
      Mac mac = Mac.getInstance(HMAC_ALGORITHM);
      mac.init(new SecretKeySpec(HexFormat.of().parseHex(secretKey), HMAC_ALGORITHM));

      return HexFormat.of().formatHex(mac.doFinal(memberId.getBytes(StandardCharsets.UTF_8)));
    } catch (GeneralSecurityException | IllegalArgumentException e) {
      throw new ChannelTalkMemberHashFailedException(e);
    }
  }
}
