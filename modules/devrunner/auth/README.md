# Auth Module

**⚠️ OAuth login & user authentication features are on hold until the first version of the UI is completed.**

Session-based authentication module.

## TODO

1. **Replace with Redis**
   - Current: InMemorySessionStore (for single instance)
   - Goal: Implement RedisSessionStore (for multi-instance production environment)
   - Location: Add new implementation of `SessionStore` interface

2. **Setup local test environment**
   - Build an environment for testing without OAuth locally
   - Add mock authentication or simple dev login API

3. **Review module separation (optional)**
   - Current: Single `modules/auth` module
   - Review: Check if separation into `auth:model`, `auth:filter`, `auth:infra` is needed
   - Criteria: Project scale and reusability

---

## Structure

```
modules/auth/
├── model/
│   └── SessionUser.java              # Session user model
├── store/
│   ├── SessionStore.java             # Session store interface (replaceable)
│   └── InMemorySessionStore.java     # In-memory implementation
├── filter/
│   └── SessionAuthenticationFilter.java  # Session validation filter
├── config/
│   ├── SecurityConfig.java           # Spring Security configuration
│   └── SecurityProperties.java       # Configuration properties
└── exception/
    ├── InvalidCredentialsException.java
    └── SessionExpiredException.java

modules/service/src/.../auth/
├── AuthService.java                   # Auth business logic interface
└── impl/
    └── DefaultAuthService.java        # Implementation
```

---

## Usage

### 1. application.yml Configuration

```yaml
# Auth module configuration
devrunner:
  security:
    # Path pattern to apply authentication (default: /api/**)
    secured-path-pattern: /api/**

    # Paths accessible without authentication
    permit-all-paths:
      - /api/auth/login
      - /api/auth/signup
      - /api/auth/oauth/callback
      - /health
      - /actuator/**

    # Whether to apply authentication to all paths (default: false)
    # false: Apply authentication only to secured-path-pattern
    # true: Apply authentication to all paths (except permit-all-paths)
    secure-all-paths: false
```

### 2. Using Authentication Info in Controller

```java
import dev.devrunner.auth.model.SessionUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public CommentResponse write(
        @AuthenticationPrincipal SessionUser sessionUser,  // Authenticated user info
        @RequestBody CommentRequest request
    ) {
        // Extract userId, email from sessionUser
        Long userId = sessionUser.getUserId();
        String email = sessionUser.getEmail();

        // Call service
        CommentWriteCommand command = CommentWriteCommand.root(
            userId,
            request.content(),
            request.targetType(),
            request.targetId()
        );

        return commentService.write(command);
    }
}
```

### 3. OAuth Callback Handling Example

```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * OAuth callback handler
     * Called after authentication is completed by OAuth Provider
     */
    @PostMapping("/oauth/callback")
    public ResponseEntity<LoginResponse> oauthCallback(
        @RequestBody OAuthCallbackRequest request,  // Contains OAuth token
        HttpServletResponse response
    ) {
        // 1. Validate OAuth token (call OAuth Provider API)
        String email = validateOAuthTokenAndGetEmail(request.getToken());

        // 2. Create session
        String sessionId = authService.login(email, null);

        // 3. Set cookie
        Cookie cookie = new Cookie("SESSION_ID", sessionId);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);  // HTTPS only
        cookie.setPath("/");
        cookie.setMaxAge(43200);  // 12 hours
        response.addCookie(cookie);

        return ResponseEntity.ok(new LoginResponse(email));
    }

    /**
     * Logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
        @CookieValue(value = "SESSION_ID", required = false) String sessionId,
        HttpServletResponse response
    ) {
        if (sessionId != null) {
            authService.logout(sessionId);
        }

        // Delete cookie
        Cookie cookie = new Cookie("SESSION_ID", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }
}
```

### 4. Configuration Examples

#### Example 1: Apply authentication only under /api/** (default)
```yaml
devrunner:
  security:
    secured-path-pattern: /api/**
    permit-all-paths:
      - /api/auth/login
      - /api/auth/oauth/callback
    secure-all-paths: false
```

**Result:**
- `/api/comments` → Authentication required ✅
- `/api/auth/login` → No authentication (permitAll)
- `/public/info` → No authentication (outside /api/**)

#### Example 2: Apply authentication to all paths
```yaml
devrunner:
  security:
    permit-all-paths:
      - /api/auth/login
      - /health
    secure-all-paths: true
```

**Result:**
- `/api/comments` → Authentication required ✅
- `/api/auth/login` → No authentication (permitAll)
- `/public/info` → Authentication required ✅ (all paths)

#### Example 3: Apply authentication only to specific paths
```yaml
devrunner:
  security:
    secured-path-pattern: /api/admin/**
    permit-all-paths:
      - /api/admin/login
    secure-all-paths: false
```

**Result:**
- `/api/admin/users` → Authentication required ✅
- `/api/admin/login` → No authentication (permitAll)
- `/api/comments` → No authentication (outside /api/admin/**)

---

## Authentication Flow

### Login
```
1. Client → OAuth Provider (Google, Kakao, etc.)
2. OAuth Provider → Backend callback (/api/auth/oauth/callback)
3. Backend: Validate OAuth token → Extract email
4. Call AuthService.login(email, null)
5. Store session in SessionStore (generate UUID session ID)
6. Set cookie (SESSION_ID=uuid, HttpOnly, Secure)
7. Respond to client
```

### Authenticated Request
```
1. Client request (includes Cookie: SESSION_ID=uuid)
2. SessionAuthenticationFilter executes
   - Extract SESSION_ID from cookie
   - Query SessionStore.getSession(sessionId)
   - If valid, store SessionUser in SecurityContext
3. Controller executes
   - Inject SessionUser via @AuthenticationPrincipal
4. Response
```

### Logout
```
1. Client → POST /api/auth/logout (includes Cookie)
2. Execute SessionStore.deleteSession(sessionId)
3. Delete cookie (Max-Age=0)
4. Response
```

---

## Dependencies

```kotlin
// modules/auth/build.gradle.kts
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation(project(":modules:model"))
    implementation(project(":modules:infrastructure"))
}
```

---

## Notes

1. **HTTPS Required**: Must use HTTPS in production (cookie Secure option)
2. **Session Expiration**: Default 12 hours, modify `DefaultAuthService.SESSION_TTL` if needed
3. **Multi-Instance**: Currently uses InMemorySessionStore, needs to be replaced with Redis
4. **CSRF**: Currently disabled, enable Spring Security CSRF token if needed
