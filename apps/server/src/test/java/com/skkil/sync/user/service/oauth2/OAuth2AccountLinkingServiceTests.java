package com.skkil.sync.user.service.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.skkil.sync.user.constant.OAuth2Provider;
import com.skkil.sync.user.dto.request.RegisterRequest;
import com.skkil.sync.user.exception.OAuth2AccountCannotBeLinkedException;
import com.skkil.sync.user.model.User;
import com.skkil.sync.user.repository.UserRepository;
import com.skkil.sync.user.service.AuthService;
import com.skkil.sync.user.service.oauth2.userinfo.OAuth2UserDetails;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OAuth2AccountLinkingServiceTests {

  @InjectMocks private OAuth2AccountLinkingService oAuth2AccountLinkingService;

  @Mock private AuthService authService;
  @Mock private UserRepository userRepository;

  @Test
  void getOrCreateUser_userNotExists_registerUser() {
    String email = "newuser@email.com";
    String fullName = "New User";
    OAuth2UserDetails userDetails = new OAuth2UserDetails("provider-user-id-123", email, fullName);

    User newUser = User.builder().email(email).fullName(fullName).hashedPassword(null).build();
    newUser.setId(1L);

    when(userRepository.findByEmailWithOAuthAccounts(email)).thenReturn(Optional.empty());
    when(authService.registerUser(any(RegisterRequest.class))).thenReturn(newUser);
    when(userRepository.save(any(User.class))).thenReturn(newUser);

    User result = oAuth2AccountLinkingService.getOrCreateUser(OAuth2Provider.GOOGLE, userDetails);

    assertThat(result).isNotNull();
    assertThat(result.getEmail()).isEqualTo(email);
    assertThat(result.getFullName()).isEqualTo(fullName);
    assertThat(result.getId()).isEqualTo(1L);
    verify(userRepository).findByEmailWithOAuthAccounts(email);
    verify(authService).registerUser(any(RegisterRequest.class));
    verify(userRepository).save(any(User.class));
  }

  @Test
  void getOrCreateUser_userExists_returnExistingUser() {
    String email = "existinguser@email.com";
    String fullName = "Existing User";
    OAuth2UserDetails userDetails = new OAuth2UserDetails("provider-user-id-123", email, fullName);

    User existingUser =
        User.builder().email(email).fullName(fullName).hashedPassword("hashedPassword").build();
    existingUser.setId(2L);

    when(userRepository.findByEmailWithOAuthAccounts(email)).thenReturn(Optional.of(existingUser));
    when(userRepository.save(any(User.class))).thenReturn(existingUser);

    User result = oAuth2AccountLinkingService.getOrCreateUser(OAuth2Provider.NAVER, userDetails);

    assertThat(result).isNotNull();
    assertThat(result.getEmail()).isEqualTo(email);
    assertThat(result.getId()).isEqualTo(2L);
    verify(userRepository).findByEmailWithOAuthAccounts(email);
    verify(userRepository).save(any(User.class));
  }

  @Test
  void linkOAuth2Account_emailMatches_linkAccount() {
    String email = "user@email.com";
    OAuth2UserDetails userDetails =
        new OAuth2UserDetails("provider-user-id-123", email, "User Name");

    User user =
        User.builder().email(email).fullName("User Name").hashedPassword("password").build();
    user.setId(1L);

    when(userRepository.save(any(User.class))).thenReturn(user);

    oAuth2AccountLinkingService.linkOAuth2Account(user, OAuth2Provider.NAVER, userDetails);

    assertThat(user.getOAuth2Accounts()).hasSize(1);
    assertThat(user.getOAuth2Accounts().get(0).getOAuth2Provider()).isEqualTo(OAuth2Provider.NAVER);
    verify(userRepository).save(user);
  }

  @Test
  void linkOAuth2Account_emailDoesNotMatch_throwException() {
    String userEmail = "user@email.com";
    String otherEmail = "different@email.com";
    OAuth2UserDetails userDetails =
        new OAuth2UserDetails("provider-user-id-123", otherEmail, "Other");

    User user =
        User.builder().email(userEmail).fullName("User Name").hashedPassword("password").build();
    user.setId(1L);

    assertThatThrownBy(
            () ->
                oAuth2AccountLinkingService.linkOAuth2Account(
                    user, OAuth2Provider.GOOGLE, userDetails))
        .isInstanceOf(OAuth2AccountCannotBeLinkedException.class);
  }

  @Test
  void linkOAuth2Account_accountAlreadyLinked_doNothing() {
    String email = "user@email.com";
    OAuth2UserDetails userDetails =
        new OAuth2UserDetails("provider-user-id-123", email, "User Name");

    User user =
        User.builder().email(email).fullName("User Name").hashedPassword("password").build();
    user.setId(1L);

    when(userRepository.save(any(User.class))).thenReturn(user);

    oAuth2AccountLinkingService.linkOAuth2Account(user, OAuth2Provider.GOOGLE, userDetails);
    int initialSize = user.getOAuth2Accounts().size();

    oAuth2AccountLinkingService.linkOAuth2Account(user, OAuth2Provider.GOOGLE, userDetails);

    assertThat(user.getOAuth2Accounts()).hasSize(initialSize);
  }
}
