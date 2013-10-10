//
// ERWorkerChannelException.java
// Project ERWorkerChannel
//
// Created by tatsuya on Sun Dec 29 2002
//
package er.workerchannel;

public class ERWorkerChannelException extends RuntimeException {

    private final Exception _exception;

    /**
     * Constructs an EJBException with no detail message.
     */
    public ERWorkerChannelException() {
        super();
        _exception = null;
    }

    /** 
     * Constructs an EJBException with the specified detailed message.
     */ 
    public ERWorkerChannelException(String message) {
        super(message);
        _exception = null;
    }
    
    /** 
     * Constructs an EJBException that embeds the originally 
     * thrown exception.
     */ 
    public ERWorkerChannelException(Exception exception) {
        super();
        _exception = exception;
    }

    /**
     * Constructs an ERWorkerChannelException that embeds the 
     * originally thrown exception with the specified detail message.
     */
    public ERWorkerChannelException(Exception exception, String message) {
        super(message);
        _exception = exception;
    }

    /**
     * Obtain the exception that caused the ERWorkerChannelException 
     * being thrown.
     */
    public Exception getCausedByException() {
        return _exception;
    }

    /** 
     * Returns the detail message, including the message from the 
     * nested exception if there is one.
     */
    public String getMessage() {
        if (_exception == null) 
            return getMessage();
        else 
            return getMessage() + ", nested exception: " + _exception.getMessage();
    }

}
