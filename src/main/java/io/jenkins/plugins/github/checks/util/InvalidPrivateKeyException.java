package io.jenkins.plugins.github.checks.util;

public class InvalidPrivateKeyException extends RuntimeException {

    public InvalidPrivateKeyException(String message) {
        super(message);
    }
}
