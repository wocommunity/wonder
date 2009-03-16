//
// ERWorkerChannel.java
// Project ERWorkerChannel
//
// Created by tatsuya on Mon Jul 29 2002
//
package er.workerchannel;


public interface ERWorkerChannel {

    public void startWorkers();
    public void shutdownWorkers();
    public void suspendWorkers();
    public void resumeWorkers();
    public void cancelScheduledWorkUnits();
    
    public ERResultUnit scheduleWorkUnit(ERWorkUnit workUnit);
    public ERWorkUnit dispatchWorkUnit();
    
    public int currentQueueSize();

}
