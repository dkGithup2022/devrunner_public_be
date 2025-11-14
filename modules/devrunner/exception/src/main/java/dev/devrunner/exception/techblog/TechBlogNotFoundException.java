package dev.devrunner.exception.techblog;

import dev.devrunner.exception.BadRequestException;

public class TechBlogNotFoundException extends BadRequestException {
    public TechBlogNotFoundException(String message) {
        super(message);
    }
}
