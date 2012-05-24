package com.webobjects.monitor.application.starter;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.monitor._private.MApplication;
import com.webobjects.monitor._private.MHost;
import com.webobjects.monitor._private.MInstance;

/**
 * Bounces an application using a rolling shutdown. 
 * 
 * It does so by starting at least one inactive instance per active host 
 * (or 10 % of the total active instance count), waiting until they have started, 
 * then forcefully restarting each instance one at a time until they have all 
 * been restarted.
 * 
 * You must have at least one inactive instance in order to perform this bounce.
 * 
 * @author johnthuss
 */
public class RollingShutdownBouncer extends ApplicationStarter {

    public RollingShutdownBouncer(MApplication app) {
        super(app);
    }

    @Override
    protected void bounce() throws InterruptedException {

        NSArray<MInstance> instances = application().instanceArray().immutableClone();
        NSArray<MInstance> runningInstances = application().runningInstances_M();
        NSArray<MHost> activeHosts = (NSArray<MHost>) runningInstances.valueForKeyPath("host.@unique");

        NSMutableArray<MInstance> inactiveInstances = instances.mutableClone();
        inactiveInstances.removeObjectsInArray(runningInstances);

        if (inactiveInstances.isEmpty()) {
        	addObjectsFromArrayIfAbsentToErrorMessageArray(
        			new NSArray<String>("You must have at least one inactive instance to perform a rolling shutdown bounce."));
        	return;
        }
        
        int numInstancesToStartPerHost = numInstancesToStartPerHost(runningInstances, activeHosts);
        NSArray<MInstance> startingInstances = instancesToStart(inactiveInstances, activeHosts, numInstancesToStartPerHost);
        
        boolean useScheduling = doAllRunningInstancesUseScheduling(runningInstances);
        log("Starting inactive instances");
        startInstances(startingInstances, activeHosts, useScheduling);
        
        waitForInactiveInstancesToStart(startingInstances, activeHosts);
        
        
        NSMutableArray<MInstance> restartingInstances = runningInstances.mutableClone();
        refuseNewSessions(restartingInstances, activeHosts);
        
        NSMutableArray<MInstance> stoppingInstances = new NSMutableArray<MInstance>();
        for (int i = numInstancesToStartPerHost; i > 0; i--) {
        	if (restartingInstances.isEmpty()) {
        		break;
        	}
        	stoppingInstances.addObject(restartingInstances.removeLastObject());
		}
        
        restartInstances(restartingInstances, activeHosts, useScheduling);
        stopInstances(stoppingInstances, activeHosts);
        
        
        handler().startReading();
        try {
            handler().getInstanceStatusForHosts(activeHosts);
            log("Finished");
        } finally {
            handler().endReading();
        }
    }

	protected int numInstancesToStartPerHost(NSArray<MInstance> runningInstances, NSArray<MHost> activeHosts) {
		int numToStartPerHost = 1;
        if (activeHosts.count() > 0) {
            numToStartPerHost = (int) (runningInstances.count() / activeHosts.count() * .1);
        }
        if (numToStartPerHost < 1) {
            numToStartPerHost = 1;
        }
        return numToStartPerHost;
	}

	protected NSArray<MInstance> instancesToStart(NSArray<MInstance> inactiveInstances, NSArray<MHost> activeHosts, 
			int numInstancesToStartPerHost) {
		NSMutableArray<MInstance> startingInstances = new NSMutableArray<MInstance>();
        for (int i = 0; i < numInstancesToStartPerHost; i++) {
            for (MHost host : activeHosts) {
                NSArray<MInstance> inactiveInstancesForHost = MInstance.HOST.eq(host).filtered(inactiveInstances);
                if (inactiveInstancesForHost != null && inactiveInstancesForHost.count() >= i) {
                    MInstance instance = inactiveInstancesForHost.objectAtIndex(i);
                    log("Starting inactive instance " + instance.displayName() + " on host " + host.addressAsString());
                    startingInstances.addObject(instance);
                } else {
                    log("Not enough inactive instances on host: " + host.addressAsString());
                }
            }
        }
		return startingInstances.immutableClone();
	}
	
	protected boolean doAllRunningInstancesUseScheduling(NSArray<MInstance> runningInstances) {
		boolean useScheduling = true;
        for (MInstance instance : runningInstances) {
            useScheduling &= instance.schedulingEnabled() != null && instance.schedulingEnabled().booleanValue();
        }
        return useScheduling;
	}

	protected void startInstances(NSArray<MInstance> startingInstances, NSArray<MHost> activeHosts, boolean useScheduling) {
		for (MInstance instance : startingInstances) {
            if (useScheduling) {
                instance.setSchedulingEnabled(Boolean.TRUE);
            }
            instance.setAutoRecover(Boolean.TRUE);
        }
        handler().sendUpdateInstancesToWotaskds(startingInstances, activeHosts);
        handler().sendStartInstancesToWotaskds(startingInstances, activeHosts);
	}

	protected void waitForInactiveInstancesToStart(NSArray<MInstance> startingInstances, NSArray<MHost> activeHosts)
			throws InterruptedException {
		boolean waiting = true;

        // wait until apps have started
        while (waiting) {
            handler().startReading();
            try {
                log("Checking to see if inactive instances have started");
                handler().getInstanceStatusForHosts(activeHosts);
                boolean allStarted = true;
                for (MInstance instance : startingInstances) {
                    allStarted &= instance.isRunning_M();
                }
                if (allStarted) {
                    waiting = false;
                } else {
                    sleep(10 * 1000);
                }
            } finally {
                handler().endReading();
            }
        }
        log("Started inactive instances sucessfully");
	}

	protected void refuseNewSessions(NSArray<MInstance> restartingInstances, NSArray<MHost> activeHosts) {
		for (MInstance instance : restartingInstances) {
            instance.setRefusingNewSessions(true);
        }
        handler().sendRefuseSessionToWotaskds(restartingInstances, activeHosts, true);
	}
	
	protected void restartInstances(NSArray<MInstance> runningInstances, NSArray<MHost> activeHosts, boolean useScheduling)
			throws InterruptedException {
		for (MInstance instance : runningInstances) {
        	NSArray<MInstance> instanceInArray = new NSArray<MInstance>(instance);
            handler().sendStopInstancesToWotaskds(instanceInArray, activeHosts);
            
            sleep(10 * 1000);
            
            handler().sendUpdateInstancesToWotaskds(instanceInArray, activeHosts);

            startInstances(instanceInArray, activeHosts, useScheduling);
            waitForInactiveInstancesToStart(instanceInArray, activeHosts);
            log("Restarted instance " + instance.displayName() + " sucessfully");
		}
	}
	
    protected void stopInstances(NSMutableArray<MInstance> stoppingInstances, NSArray<MHost> activeHosts) {
        for (MInstance instance : stoppingInstances) {
            instance.setSchedulingEnabled(Boolean.FALSE);
            instance.setAutoRecover(Boolean.FALSE);
        }	
        handler().sendUpdateInstancesToWotaskds(stoppingInstances, activeHosts);
        handler().sendStopInstancesToWotaskds(stoppingInstances, activeHosts);
        log("Stopped instances " + stoppingInstances.toString() + " sucessfully");
	}
	
}
