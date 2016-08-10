//
// ERFutureResult.java
// Project ERWorkerChannel
//
// Created by tatsuya on Mon Jul 29 2002
//
package er.workerchannel;

import er.extensions.logging.ERXLogger;

public class ERFutureResult extends ERResultUnit {

    public static final ERXLogger log = ERXLogger.getERXLogger(ERFutureResult.class);

    private ERResultUnit _result;

    private boolean _isReady = false;

    public synchronized void setResult(ERResultUnit result) {
        _result = result;
        _isReady = true;
        notifyAll();
    }

    public synchronized Object resultValue() {
        while (! _isReady) {
            try {
                wait();
            } catch (InterruptedException e) {
                ;
            }
        }
        return _result.resultValue();
    }
}
