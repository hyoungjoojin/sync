package com.skkil.sync.collection.dto.response;

import com.skkil.sync.collection.dto.summary.CollectionSummary;
import java.util.List;

public record GetCollectionsResponse(List<CollectionSummary> collections) {}
