package dev.devrunner.api.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CheckUserRequest {

    @NotBlank(message = "Access token is required")
    private String accessToken;
}
