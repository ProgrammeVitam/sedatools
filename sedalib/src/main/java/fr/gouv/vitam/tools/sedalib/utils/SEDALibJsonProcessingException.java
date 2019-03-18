/*
 *
 */
package fr.gouv.vitam.tools.sedalib.utils;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * The Class SEDALibJsonProcessingException.
 * <p>
 * These exceptions are used to cast exceptions in specific json serializing/deserializing process.
 */
public class SEDALibJsonProcessingException extends JsonProcessingException {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 961528185663683074L;

    /**
     * Instantiates a new seda lib json processing exception.
     *
     * @param msg the msg
     */
    public SEDALibJsonProcessingException(String msg) {
        super(msg);
    }
}
