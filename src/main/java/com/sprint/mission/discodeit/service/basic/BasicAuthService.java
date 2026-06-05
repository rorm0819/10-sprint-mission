package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.auth.jwt.JwtInformation;
import com.sprint.mission.discodeit.auth.jwt.JwtRegistry;
import com.sprint.mission.discodeit.auth.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.dto.data.JwtDto;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.LoginRequest;
import com.sprint.mission.discodeit.dto.request.RoleUpdateRequest;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.details.DiscodeitUserDetails;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.exception.user.InvalidCredentialsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import com.sprint.mission.discodeit.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
//  private final SessionRegistry sessionRegistry;
  private final JwtRegistry jwtRegistry;
  private final UserService userService;
  private final JwtTokenProvider jwtTokenProvider;
  private final UserDetailsService userDetailsService;
  private final ApplicationEventPublisher eventPublisher;

  /*
  @Transactional(readOnly = true)
  @Override
  public UserDto login(LoginRequest loginRequest) {
    log.debug("로그인 시도: username={}", loginRequest.username());

    String username = loginRequest.username();
    String password = loginRequest.password();

    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> UserNotFoundException.withUsername(username));

    if (!user.getPassword().equals(password)) {
      throw InvalidCredentialsException.wrongPassword();
    }

    log.info("로그인 성공: userId={}, username={}", user.getId(), username);
    return userMapper.toDto(user);
  }
   */

  // Role 변경
  @Transactional
  @PreAuthorize( "hasRole('ADMIN')")
  public UserDto updateUserRole(RoleUpdateRequest request) {
    log.debug("사용자 Role 변경 시작");

    User user = userRepository.findById(request.userId())
            .orElseThrow(() -> UserNotFoundException.withId(request.userId()));

    Role oldRole = user.getRole();

    user.updateRole(request.newRole());
    userRepository.save(user);

    jwtRegistry.invalidateJwtInformationByUserId(user.getId());

    eventPublisher.publishEvent(new RoleUpdatedEvent(
            user.getId(),
            oldRole,
            user.getRole()
    ));

    log.debug("사용자 Role 변경 완료");

    return userMapper.toDto(user, userService.isLoggedIn(user.getId()));
  }

  public JwtDto refresh(String refreshToken, HttpServletResponse response) {
    if (refreshToken == null || refreshToken.isBlank()) {
      throw new RuntimeException("Refresh token is null or blank");
    }

    Map<String, Object> refreshClaims = jwtTokenProvider.getClaims(refreshToken);
    UUID userId = UUID.fromString(String.valueOf(refreshClaims.get("sub")));

    if (!jwtRegistry.hasActiveJwtInformationByRefreshToken(refreshToken)) {
      throw new RuntimeException("활성되지 않은 refresh token 입니다.");
    }

    User user = userRepository.findById(userId)
            .orElseThrow(() -> UserNotFoundException.withId(userId));

    UserDto userDto = userMapper.toDto(
            user,
            jwtRegistry.hasActiveJwtInformationByUserId(userId)
    );

    String newAccessToken = jwtTokenProvider.generateAccessToken(userDto);
    String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDto);

    JwtInformation newJwtInformation = new JwtInformation(userDto, newAccessToken, newRefreshToken);
    jwtRegistry.rotateJwtInformation(refreshToken, newJwtInformation);

    Cookie refreshTokenCookie = new Cookie("REFRESH_TOKEN", newRefreshToken);

    refreshTokenCookie.setHttpOnly(true);
    refreshTokenCookie.setSecure(true);
    refreshTokenCookie.setPath("/");
    refreshTokenCookie.setMaxAge(60 * 60 * 24 * 7);

    response.addCookie(refreshTokenCookie);
    return new JwtDto(userDto, newAccessToken);
  }

//  private void expiredUserSession(UUID userId) {
//    sessionRegistry.getAllPrincipals().stream()
//            .filter(principal -> principal instanceof DiscodeitUserDetails)
//            .map(principal -> (DiscodeitUserDetails) principal)
//            .filter(userDetails -> userDetails.getUserDto().id().equals(userId))
//            .forEach(userDetails -> sessionRegistry.getAllSessions(userDetails, false)
//                    .forEach(SessionInformation::expireNow)
//            );
//  }
}
