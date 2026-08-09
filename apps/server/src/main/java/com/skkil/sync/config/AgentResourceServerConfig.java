package com.skkil.sync.config;

import com.skkil.sync.auth.agent.AgentJwtAuthenticationConverter;
import com.skkil.sync.auth.agent.AgentScopes;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@ConditionalOnProperty(name = "app.agent.enabled", havingValue = "true", matchIfMissing = true)
public class AgentResourceServerConfig {

  static final String PROTECTED_RESOURCE_METADATA_PATH = "/.well-known/oauth-protected-resource";

  @Bean
  @Order(1)
  SecurityFilterChain agentApiSecurityFilterChain(
      HttpSecurity http,
      AgentJwtAuthenticationConverter jwtAuthenticationConverter,
      AgentOAuth2Properties properties)
      throws Exception {
    http.securityMatcher("/agent/**", "/mcp", "/mcp/**", PROTECTED_RESOURCE_METADATA_PATH)
        .csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            requests ->
                requests
                    .requestMatchers(PROTECTED_RESOURCE_METADATA_PATH)
                    .permitAll()
                    .anyRequest()
                    .hasAuthority("SCOPE_" + AgentScopes.POSTS_DRAFT))
        .oauth2ResourceServer(
            oauth2 ->
                oauth2
                    .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                    .protectedResourceMetadata(
                        metadata ->
                            metadata.protectedResourceMetadataCustomizer(
                                builder ->
                                    builder
                                        .resource(properties.issuerUri())
                                        .authorizationServer(properties.issuerUri())
                                        .scope(AgentScopes.POSTS_DRAFT)
                                        .tlsClientCertificateBoundAccessTokens(false)
                                        .resourceName("SYNC")))
                    .accessDeniedHandler(new BearerTokenAccessDeniedHandler()));

    return http.build();
  }
}
