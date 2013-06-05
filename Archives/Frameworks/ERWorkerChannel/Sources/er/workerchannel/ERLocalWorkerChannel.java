//
// ERLocalWorkerChannel.java
// Project ERWorkerChannel
//
// Created by tatsuya on Mon Jul 29 2002
//
package er.workerchannel;

import er.extensions.logging.ERXLogger;

public class ERLocalWorkerChannel implements ERWorkerChannel {

    public static final ERXLogger log = ERXLogger.getERXLogger(ERLocalWorkerChannel.class);

    private final int _queueSize;
    private ERWorkUnit[] _workUnitQueue;
    private int _head;
    private int _tail;
    private int _count;
    private boolean _isSuspended;

    private final ERWorkerThread[] _threadPool;

    public ERLocalWorkerChannel(int numberOfThreads, int queueSize) {
        _queueSize = queueSize; 
        _initalizeWorkUnitQueue();
        _isSuspended = false;

        _threadPool = new ERWorkerThread[numberOfThreads];
        for (int i = 0; i < _threadPool.length; i++) 
            _threadPool[i] = new ERWorkerThread("Worker-" + i, this);
    }

    public void startWorkers() {
        for (int i = 0; i < _threadPool.length; i++) 
            _threadPool[i].start();
    }

    public synchronized void shutdownWorkers() {
        log.info("shutdownWorkers() - count = " + _count);
        
        while (_count > 0) {
            notifyAll();
            try {
                wait();
            } catch (InterruptedException e) {
                ;
            }
        }
        
        for (int i = 0; i < _threadPool.length; i++) 
            _threadPool[i].shutdown();

        for (int i = 0; i < _threadPool.length; i++) {
            notifyAll();
            try {
                _threadPool[i].join();
            } catch (InterruptedException ex) {
                ;
            }
        }
    }

    public void suspendWorkers() {
        _isSuspended = true;
    }
    
    public void resumeWorkers() {
        _isSuspended = false;
        notifyAll();
    }

    public synchronized ERResultUnit scheduleWorkUnit(ERWorkUnit workUnit) {
        while (_count >= _workUnitQueue.length) {
            try {
                wait();
            } catch (InterruptedException e) {
                ;
            }
        }
        ERFutureResult futureResult = new ERFutureResult();
        workUnit.setFutureResult(futureResult);
        _workUnitQueue[_tail] = workUnit;
        _tail = (_tail + 1) % _workUnitQueue.length;
        _count++;
        if (_count >= _queueSize * 0.15d) 
            notifyAll();
        return futureResult;
    }

    public synchronized ERWorkUnit dispatchWorkUnit() {
        while (_count <= 0  ||  _isSuspended) {
            try {
                wait();
            } catch (InterruptedException e) {
                ;
            }
        }
        ERWorkUnit workUnit = _workUnitQueue[_head];
        _head = (_head + 1) % _workUnitQueue.length;
        _count--;
        if (_count <= _queueSize * (1.0d - 0.15d)) 
            notifyAll();
        return workUnit;
    }

    public synchronized void cancelScheduledWorkUnits() {
        _initalizeWorkUnitQueue();
        _isSuspended = false;
    }
    
    public int currentQueueSize() {
        return _count;
    }

    private void _initalizeWorkUnitQueue() {
        _workUnitQueue = new ERWorkUnit[_queueSize];
    	_head = 0;
    	_tail = 0;
    	_count = 0;
    }
    
}
