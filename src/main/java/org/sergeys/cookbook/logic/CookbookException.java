package org.sergeys.cookbook.logic;

public class CookbookException extends Exception {

    private static final long serialVersionUID = 7373188450197946128L;

    public CookbookException(String string) {
        super(string);
    }

    public CookbookException(String string, Throwable ex) {
        super(string, ex);
    }
}
