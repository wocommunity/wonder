//
// ERWorkerChannel.java
// Project ERWorkerChannel
//
// Created by tatsuya on Mon Jul 29 2002
//
package er.workerchannel;

import er.extensions.ERXLogger;

public abstract class ERWorkerChannel {

    public abstract void startWorkers();
    public abstract void shutdownWorkers();
    public abstract void suspendWorkers();
    public abstract void resumeWorkers();
    public abstract void cancelScheduledWorkUnits();
    
    public abstract ERResultUnit scheduleWorkUnit(ERWorkUnit workUnit);
    public abstract ERWorkUnit dispatchWorkUnit();
    
    public abstract int currentQueueSize();

}
