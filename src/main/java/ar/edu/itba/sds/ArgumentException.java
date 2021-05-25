package ar.edu.itba.sds;

import java.util.function.Supplier;

public class ArgumentException extends Exception implements Supplier<ArgumentException> {
    private static final long serialVersionUID = 4247958468405264522L;

    public ArgumentException(String message) {
        super(message);
    }

    @Override
    public ArgumentException get() {
        return this;
    }
}