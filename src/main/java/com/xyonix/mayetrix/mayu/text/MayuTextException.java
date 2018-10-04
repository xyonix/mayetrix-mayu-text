package com.xyonix.mayetrix.mayu.text;

public class MayuTextException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MayuTextException() { }

    public MayuTextException(String message) {
        super(message);
    }

    public MayuTextException(Throwable t) {
        super(t);
    }

    public MayuTextException(String message, Throwable t) {
        super(message, t);
    }
}
