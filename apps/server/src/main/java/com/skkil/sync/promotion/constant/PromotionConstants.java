package com.skkil.sync.promotion.constant;

public final class PromotionConstants {

  public static final int MAX_ATTACHMENT_VALUE_LENGTH = 200;

  // Defense in depth, independent of how many fields a promotion declares — a promotion
  // with several fields whose individually-valid lengths could still sum to an oversized
  // payload is still bounded by this.
  public static final int MAX_ATTACHMENT_SERIALIZED_SIZE_BYTES = 10000;

  private PromotionConstants() {}
}
