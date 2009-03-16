//
// ERRealResult.java
// Project ERWorkerChannel
//
// Created by tatsuya on Mon Jul 29 2002
//
package er.workerchannel;

import er.extensions.logging.ERXLogger;

public class ERRealResult extends ERResultUnit {

    public static final ERXLogger log = ERXLogger.getERXLogger(ERRealResult.class);

    public static final ERRealResult EmptyResult = new ERRealResult("EmptyResult", null);

    private final Object _resultValue;
    private final RuntimeException _exception;

    public ERRealResult(Object resultValue, RuntimeException exception) {
        _resultValue = resultValue;
        _exception = exception;
    }

    public Object resultValue() {
        if (_exception != null)  throw _exception; 
        return _resultValue;
    }

}
