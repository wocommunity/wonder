//
// ERWorkerThread.java
// Project ERWorkerChannel
//
// Created by tatsuya on Mon Jul 29 2002
//
package er.workerchannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



class ERWorkerThread extends Thread {

    public static final Logger log = LoggerFactory.getLogger(ERWorkerThread.class);

    private final ERWorkerChannel _channel;
    private volatile boolean _wasShutdownRequested = false;

    public ERWorkerThread(String name, ERWorkerChannel channel) {
        super(name);
        _channel = channel;
    }

    public void shutdown() {
        _wasShutdownRequested = true;
        interrupt();
    }
    
    public boolean wasShutdownRequested() {
        return _wasShutdownRequested;
    }
    
    public void run() {
        log.info(this.toString() + " started.");
        try {
            while (! _wasShutdownRequested) {
                ERWorkUnit workUnit = _channel.dispatchWorkUnit();
                log.info(toString() + " dispatched a work unit: " + workUnit);
                workUnit.setWasDispatched(true);
                workUnit.execute();
            }
        } finally {
            log.info(this.toString() + " shutdown.");
        }
    }
    
    public String toString() {
        return "<" + Thread.currentThread().getName() + ">";
    }
}
