package com.skkil.sync.config;

import com.skkil.sync.auth.agent.AuthorizedAgentClientResolver;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnProperty(name = "app.agent.enabled", havingValue = "true", matchIfMissing = true)
public class AgentWebMvcConfig implements WebMvcConfigurer {

  private final AuthorizedAgentClientResolver authorizedAgentClientResolver;

  public AgentWebMvcConfig(AuthorizedAgentClientResolver authorizedAgentClientResolver) {
    this.authorizedAgentClientResolver = authorizedAgentClientResolver;
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(authorizedAgentClientResolver);
  }
}
