package ar.edu.itba.sds.objects;

import java.util.EmptyStackException;
import java.util.Stack;

public class CustomStack<E> extends Stack<E> {

    public CustomStack() {
        super();
    }

    public synchronized E peekPeek() {
        int len = size();

        if (len == 0 || len == 1)
            throw new EmptyStackException();
        return elementAt(len - 2);
    }
}
