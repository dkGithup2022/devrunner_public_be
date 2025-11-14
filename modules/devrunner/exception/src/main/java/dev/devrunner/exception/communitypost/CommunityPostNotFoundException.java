package dev.devrunner.exception.communitypost;

import dev.devrunner.exception.BadRequestException;

public class CommunityPostNotFoundException extends BadRequestException {

    public CommunityPostNotFoundException(String message) {
        super(message);
    }
}
