package com.skkil.sync.common.integration.channeltalk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.skkil.sync.common.integration.channeltalk.exception.ChannelTalkMemberHashFailedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ChannelTalkServiceTests {

  private static final String SECRET_KEY =
      "4629de5def93d6a2abea6afa9bd5476d9c6cbc04223f9a2f7e517b535dde3e25";

  @Test
  @DisplayName("[generateMemberHash] 채널톡 공식 문서의 검증 벡터와 동일한 해시를 생성한다")
  void generateMemberHash_documentedVector_matches() {
    ChannelTalkService channelTalkService = new ChannelTalkService(SECRET_KEY);

    assertThat(channelTalkService.generateMemberHash("lucas"))
        .isEqualTo("99427c7bba36a6902c5fd6383f2fb0214d19b81023296b4bd6b9e024836afea2");
  }

  @Test
  @DisplayName("[generateMemberHash] 시크릿 키가 설정되지 않은 경우 null 반환")
  void generateMemberHash_secretKeyNotConfigured_returnsNull() {
    ChannelTalkService channelTalkService = new ChannelTalkService("");

    assertThat(channelTalkService.generateMemberHash("1")).isNull();
  }

  @Test
  @DisplayName("[generateMemberHash] 시크릿 키가 hex 문자열이 아닌 경우 예외 발생")
  void generateMemberHash_secretKeyIsNotHex_throws() {
    ChannelTalkService channelTalkService = new ChannelTalkService("not-a-hex-key");

    assertThatThrownBy(() -> channelTalkService.generateMemberHash("1"))
        .isInstanceOf(ChannelTalkMemberHashFailedException.class);
  }
}
