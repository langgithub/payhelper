package com.lang.payhelper.feature.backup.exception;

/**
 * version missed exception
 */
public class VersionMissedException extends VersionInvalidException {

    public VersionMissedException() {
    }

    public VersionMissedException(String message) {
        super(message);
    }

    public VersionMissedException(String message, Throwable cause) {
        super(message, cause);
    }

    public VersionMissedException(Throwable cause) {
        super(cause);
    }
}
