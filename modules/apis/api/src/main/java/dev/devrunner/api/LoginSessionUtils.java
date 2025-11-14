package dev.devrunner.api;

import dev.devrunner.auth.model.SessionUser;
import dev.devrunner.exception.auth.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoginSessionUtils {

    public static Long readUserIdOrThrow(SessionUser sessionUser) {
        var userId = sessionUser != null ? sessionUser.getUserId() : null;
        if (userId == null) {
            log.error("user not found in current session");
            throw new UnauthorizedException("You need to logged in before  request");
        }
        return userId;
    }

}