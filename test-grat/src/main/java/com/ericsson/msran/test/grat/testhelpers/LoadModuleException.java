package com.ericsson.msran.test.grat.testhelpers;

/**
 * @name LoadModuleException
 * 
 * @author Markus Hedvall (xmahedv)
 * 
 * @created 2013-11-21
 * 
 * @description This exception is thrown if something unexpected happens during
 *              a restart of a load module.
 * 
 * @revision xmahedv 2013-11-21 First version.
 */
public class LoadModuleException extends Exception {

    private static final long serialVersionUID = -5086190916053046078L;

    /**
     * Constructs a new load module exception with null as its detail message.
     */
    public LoadModuleException() {
    }

    /**
     * Constructs a new load module exception with the specified detail message.
     * 
     * @param message the detail message.
     */
    public LoadModuleException(String message) {
        super(message);
    }

    /**
     * Constructs a new load module exception with the specified detail message
     * and cause.
     * 
     * @param cause the cause.
     */
    public LoadModuleException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new load module exception with the specified cause and a
     * detail message.
     * 
     * @param message the detail message.
     * @param cause the cause.
     */
    public LoadModuleException(String message, Throwable cause) {
        super(message, cause);
    }

}
