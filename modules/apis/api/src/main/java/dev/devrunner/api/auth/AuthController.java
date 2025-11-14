package dev.devrunner.api.auth;

import dev.devrunner.api.auth.dto.request.CheckUserRequest;
import dev.devrunner.api.auth.dto.request.LoginRequest;
import dev.devrunner.api.auth.dto.request.SignupRequest;
import dev.devrunner.api.auth.dto.response.CheckNicknameResponse;
import dev.devrunner.api.auth.dto.response.CheckUserResponse;
import dev.devrunner.api.auth.dto.response.UserInfoResponse;
import dev.devrunner.service.auth.SessionService;
import dev.devrunner.service.auth.UserOnboardingService;
import dev.devrunner.auth.model.SessionUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 API")
public class AuthController {

    private final SessionService sessionService;
    private final UserOnboardingService userOnboardingService;

    private static final String SESSION_COOKIE_NAME = "SESSION_ID";
    private static final int SESSION_MAX_AGE = 3 * 24 * 60 * 60;  // 3일 (초 단위)

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "Google Access Token으로 로그인하고 세션을 생성합니다.")
    public ResponseEntity<Void> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        String sessionId = sessionService.login(request.getAccessToken());

        // HttpOnly, Secure, SameSite 쿠키 설정
        addSessionCookie(response, sessionId, SESSION_MAX_AGE);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "세션을 삭제하고 로그아웃합니다.")
    public ResponseEntity<Void> logout(
            @CookieValue(value = SESSION_COOKIE_NAME, required = false) String sessionId,
            HttpServletResponse response
    ) {
        sessionService.logout(sessionId);

        // 쿠키 삭제 (Max-Age=0)
        //addSessionCookie(response, "", 0);


        // 쿠키 삭제 (Max-Age=0 설정)
        Cookie cookie = new Cookie("SESSION_ID", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // HTTPS 환경에서만
        // cookie.setSecure(false); // 개발 환경 (HTTP)에서는 false
        response.addCookie(cookie);

        log.info("SESSION_ID cookie deleted for sessionId={}", sessionId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    @Operation(summary = "현재 사용자 정보 조회", description = "현재 로그인된 사용자의 정보를 반환합니다.")
    public ResponseEntity<UserInfoResponse> getMe(
            @AuthenticationPrincipal SessionUser sessionUser
    ) {
        log.info("getMe :{}", sessionUser);
        var userInfo = sessionService.getCurrentUser(sessionUser);

        // Service DTO → API Response 변환
        UserInfoResponse response = UserInfoResponse.of(
                userInfo.getUserId(),
                userInfo.getEmail(),
                userInfo.getNickname(),
                userInfo.getPicture(),
                userInfo.getGoogleId()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "Google Access Token과 닉네임으로 회원가입합니다. (자동 로그인 안함)")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest request) {
        log.info("Signup request: {}", request);
        userOnboardingService.signup(request.getAccessToken(), request.getNickname());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/check")
    @Operation(summary = "사용자 존재 확인", description = "Google Access Token으로 사용자 가입 여부를 확인합니다.")
    public ResponseEntity<CheckUserResponse> checkUser(@Valid @RequestBody CheckUserRequest request) {
        log.info("Check user request: {}", request);
        var result = userOnboardingService.checkUser(request.getAccessToken());

        // Service DTO → API Response 변환
        CheckUserResponse response = CheckUserResponse.of(result.isExists(), result.getEmail());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/nicknames/{nickname}/check")
    @Operation(summary = "닉네임 중복 확인", description = "닉네임 사용 가능 여부를 확인합니다.")
    public ResponseEntity<CheckNicknameResponse> checkNickname(@PathVariable String nickname) {
        var result = userOnboardingService.checkNickname(nickname);

        // Service DTO → API Response 변환
        CheckNicknameResponse response = result.isAvailable()
                ? CheckNicknameResponse.available(nickname)
                : CheckNicknameResponse.taken(nickname);

        return ResponseEntity.ok(response);
    }

    /**
     * 세션 쿠키를 응답에 추가
     *
     * @param response HttpServletResponse
     * @param value    쿠키 값
     * @param maxAge   만료 시간 (초)
     */
    private void addSessionCookie(HttpServletResponse response, String value, int maxAge) {
        ResponseCookie cookie = ResponseCookie.from(SESSION_COOKIE_NAME, value)
                .httpOnly(true)           // XSS 방지
                .secure(true)            // TODO: 프로덕션에서는 true (HTTPS 필수)
                .path("/")
                .maxAge(maxAge)
                .sameSite("Lax")          // CSRF 방지
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }
}
