package com.skkil.sync.collection.dto.response;

import com.skkil.sync.collection.dto.summary.CollectionSummary;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record GetCollectionsResponse(
    List<CollectionSummary> collections, @Nullable List<Membership> memberships) {

  public record Membership(String collectionExternalId, Long collectionPostId) {}
}
