package com.skkil.sync.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class ClientIpResolverTests {

  private static final String NGINX_ADDRESS = "172.18.0.5";

  private HttpServletRequest request(String forwardedFor) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr(NGINX_ADDRESS);
    if (forwardedFor != null) {
      request.addHeader(ClientIpResolver.X_FORWARDED_FOR, forwardedFor);
    }

    return request;
  }

  @Test
  @DisplayName("프록시 홉이 없으면 X-Forwarded-For 를 무시하고 TCP peer 주소를 쓴다")
  void ignoresHeaderWhenNoTrustedProxy() {
    assertThat(new ClientIpResolver(0).resolve(request("1.2.3.4"))).isEqualTo(NGINX_ADDRESS);
  }

  @Test
  @DisplayName("프록시 한 단계면 X-Forwarded-For 의 마지막 항목이 클라이언트 IP다")
  void usesLastEntryBehindSingleProxy() {
    assertThat(new ClientIpResolver(1).resolve(request("203.0.113.7"))).isEqualTo("203.0.113.7");
  }

  @Test
  @DisplayName("클라이언트가 X-Forwarded-For 를 위조해도 nginx 가 덧붙인 실제 IP를 쓴다")
  void ignoresSpoofedLeftmostEntry() {
    assertThat(new ClientIpResolver(1).resolve(request("9.9.9.9, 203.0.113.7")))
        .isEqualTo("203.0.113.7");
  }

  @Test
  @DisplayName("프록시 두 단계면 뒤에서 두 번째 항목이 클라이언트 IP다")
  void usesSecondToLastEntryBehindTwoProxies() {
    assertThat(new ClientIpResolver(2).resolve(request("9.9.9.9, 203.0.113.7, 198.51.100.1")))
        .isEqualTo("203.0.113.7");
  }

  @Test
  @DisplayName("헤더가 없으면 TCP peer 주소로 폴백한다")
  void fallsBackWhenHeaderMissing() {
    assertThat(new ClientIpResolver(1).resolve(request(null))).isEqualTo(NGINX_ADDRESS);
    assertThat(new ClientIpResolver(1).resolve(request("   "))).isEqualTo(NGINX_ADDRESS);
  }

  @Test
  @DisplayName("헤더 항목 수가 신뢰 홉 수보다 적으면 TCP peer 주소로 폴백한다")
  void fallsBackWhenHeaderShorterThanTrustedHops() {
    assertThat(new ClientIpResolver(2).resolve(request("203.0.113.7"))).isEqualTo(NGINX_ADDRESS);
  }

  @Test
  @DisplayName("IP 형식이 아닌 항목은 버킷 키로 쓰지 않는다")
  void fallsBackWhenEntryIsNotIpLiteral() {
    assertThat(new ClientIpResolver(1).resolve(request("unknown"))).isEqualTo(NGINX_ADDRESS);
    assertThat(new ClientIpResolver(1).resolve(request("evil.example.com")))
        .isEqualTo(NGINX_ADDRESS);
  }

  @Test
  @DisplayName("포트가 붙은 항목에서 주소만 뽑아낸다")
  void stripsPortFromEntry() {
    assertThat(new ClientIpResolver(1).resolve(request("203.0.113.7:41234")))
        .isEqualTo("203.0.113.7");
    assertThat(new ClientIpResolver(1).resolve(request("[2001:db8::1]:41234")))
        .isEqualTo("2001:db8::1");
  }

  @Test
  @DisplayName("대괄호 없는 IPv6 주소를 그대로 유지한다")
  void keepsBareIpv6Address() {
    assertThat(new ClientIpResolver(1).resolve(request("2001:db8::1"))).isEqualTo("2001:db8::1");
  }
}
