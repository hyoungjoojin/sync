package com.skkil.sync.post.service;

import com.skkil.sync.post.event.PostContentChangedEvent;
import com.skkil.sync.post.model.Post;
import com.skkil.sync.post.model.PostEmbedding;
import com.skkil.sync.post.repository.PostEmbeddingRepository;
import com.skkil.sync.post.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@Slf4j
public class PostEmbeddingService {

  private final ObjectProvider<EmbeddingModel> embeddingModelProvider;
  private final PostRepository postRepository;
  private final PostEmbeddingRepository embeddingRepository;

  @Value("${app.ai.enabled:true}")
  private boolean aiEnabled;

  public PostEmbeddingService(
      ObjectProvider<EmbeddingModel> embeddingModelProvider,
      PostRepository postRepository,
      PostEmbeddingRepository embeddingRepository) {
    this.embeddingModelProvider = embeddingModelProvider;
    this.postRepository = postRepository;
    this.embeddingRepository = embeddingRepository;
  }

  private EmbeddingModel requireEmbeddingModel() {
    EmbeddingModel embeddingModel = embeddingModelProvider.getIfAvailable();
    if (embeddingModel == null) {
      throw new IllegalStateException(
          "AI features are enabled but no EmbeddingModel bean is available"
              + " (check AI_PROVIDER configuration)");
    }
    return embeddingModel;
  }

  @Async
  @TransactionalEventListener
  public void refreshPostEmbeddings(PostContentChangedEvent event) {
    if (!aiEnabled) {
      log.debug("AI features disabled, skipping embedding refresh for post {}", event.getPostId());
      return;
    }

    Post post = postRepository.getReferenceById(event.getPostId());

    Document document = Document.builder().text(event.getContent()).build();
    float[] embedding = requireEmbeddingModel().embed(document);

    PostEmbedding postEmbedding =
        embeddingRepository
            .findByPostId(post.getId())
            .orElseGet(() -> PostEmbedding.builder().post(post).embedding(embedding).build());
    postEmbedding.updateEmbedding(embedding);
    embeddingRepository.save(postEmbedding);
  }

  public float[] computeEmbedding(String content) {
    if (!aiEnabled) {
      throw new IllegalStateException("AI features are disabled");
    }

    Document document = Document.builder().text(content).build();
    return requireEmbeddingModel().embed(document);
  }
}
