package dev.devrunner.api.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Signup request DTO
 *
 * Nickname validation is performed in the service layer for detailed error messages.
 * Rule: 4-15 characters, English letters and numbers only
 */
@Getter
@NoArgsConstructor
public class SignupRequest {

    @NotBlank(message = "Access token is required")
    private String accessToken;

    @NotBlank(message = "Nickname is required")
    private String nickname;
}
