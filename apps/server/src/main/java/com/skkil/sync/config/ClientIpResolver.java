package com.skkil.sync.config;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

final class ClientIpResolver {

  static final String X_FORWARDED_FOR = "X-Forwarded-For";

  private static final Pattern ENTRY_SEPARATOR = Pattern.compile(",");
  private static final int MAX_IP_LENGTH = 45;

  private final int trustedProxyCount;

  ClientIpResolver(int trustedProxyCount) {
    this.trustedProxyCount = Math.max(0, trustedProxyCount);
  }

  String resolve(HttpServletRequest request) {
    if (trustedProxyCount == 0) {
      return request.getRemoteAddr();
    }

    List<String> forwardedFor = parseForwardedFor(request.getHeader(X_FORWARDED_FOR));
    if (forwardedFor.size() < trustedProxyCount) {
      return request.getRemoteAddr();
    }

    String candidate = normalize(forwardedFor.get(forwardedFor.size() - trustedProxyCount));
    if (!isIpLiteral(candidate)) {
      return request.getRemoteAddr();
    }

    return candidate;
  }

  private static List<String> parseForwardedFor(String header) {
    if (header == null || header.isBlank()) {
      return List.of();
    }

    List<String> entries = new ArrayList<>();
    for (String entry : ENTRY_SEPARATOR.split(header, -1)) {
      String trimmed = entry.trim();
      if (!trimmed.isEmpty()) {
        entries.add(trimmed);
      }
    }

    return entries;
  }

  private static String normalize(String entry) {
    if (entry.startsWith("[")) {
      int end = entry.indexOf(']');
      return end > 1 ? entry.substring(1, end) : "";
    }

    int firstColon = entry.indexOf(':');
    if (firstColon >= 0 && entry.indexOf(':', firstColon + 1) < 0) {
      return entry.substring(0, firstColon);
    }

    return entry;
  }

  private static boolean isIpLiteral(String value) {
    if (value.isEmpty() || value.length() > MAX_IP_LENGTH) {
      return false;
    }

    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      boolean allowed =
          (c >= '0' && c <= '9')
              || (c >= 'a' && c <= 'f')
              || (c >= 'A' && c <= 'F')
              || c == '.'
              || c == ':'
              || c == '%';
      if (!allowed) {
        return false;
      }
    }

    return true;
  }
}
