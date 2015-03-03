package org.simplericity.jettyconsole.creator;

/**
 * Created by IntelliJ IDEA.
 * User: bjorsnos
 * Date: Dec 28, 2008
 * Time: 1:00:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class CreatorExecutionException extends Exception {
    public CreatorExecutionException(String s) {
        super(s);
    }

    public CreatorExecutionException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
