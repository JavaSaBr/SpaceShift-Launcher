package com.ss.launcher.exception;

/**
 * @author Ronn
 */
public class NotFoundClientException extends RuntimeException {

    private static final long serialVersionUID = 2950932604404159164L;

    public NotFoundClientException(String message) {
        super(message);
    }
}
