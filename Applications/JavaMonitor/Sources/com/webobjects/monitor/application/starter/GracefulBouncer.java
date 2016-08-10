package com.webobjects.monitor.application.starter;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.monitor._private.MApplication;
import com.webobjects.monitor._private.MHost;
import com.webobjects.monitor._private.MInstance;

/**
 * Bounces an application gracefully. It does so by starting at least one inactive instance
 * per active host (or 10 % of the total active instance count), waiting
 * until they have started, then refusing sessions for all old instances and
 * turning scheduling on for all but the number of instances we started
 * originally. The next effect should be that the new users get the new app,
 * old instances die in due time and then restart when the sessions stop.
 * 
 * You must have at least one inactive instance in order to perform a graceful bounce.
 * 
 * You may or may not need to set ERKillTimer to prevent totally
 * long-running sessions to keep the app from dying.
 *
 * @author ak
 */
public class GracefulBouncer extends ApplicationStarter {

    public GracefulBouncer(MApplication app) {
        super(app);
    }

    @Override
    protected void bounce() throws InterruptedException {

        NSArray<MInstance> instances = application().instanceArray().immutableClone();
        NSMutableArray<MInstance> runningInstances = new NSMutableArray<MInstance>();
        NSMutableSet<MHost> activeHosts = new NSMutableSet<MHost>();
        NSMutableDictionary<MHost, NSMutableArray<MInstance>> inactiveInstancesByHost = new NSMutableDictionary<MHost, NSMutableArray<MInstance>>();
        NSMutableDictionary<MHost, NSMutableArray<MInstance>> activeInstancesByHost = new NSMutableDictionary<MHost, NSMutableArray<MInstance>>();
        for (MInstance instance : instances) {
            MHost host = instance.host();
            if (instance.isRunning_M()) {
                runningInstances.addObject(instance);
                activeHosts.addObject(host);
                NSMutableArray<MInstance> currentInstances = activeInstancesByHost.objectForKey(host);
                if (currentInstances == null) {
                    currentInstances = new NSMutableArray<MInstance>();
                    activeInstancesByHost.setObjectForKey(currentInstances, host);
                }
                currentInstances.addObject(instance);
            } else {
                NSMutableArray<MInstance> currentInstances = inactiveInstancesByHost.objectForKey(host);
                if (currentInstances == null) {
                    currentInstances = new NSMutableArray<MInstance>();
                    inactiveInstancesByHost.setObjectForKey(currentInstances, host);
                }
                currentInstances.addObject(instance);
            }
        }
        
        if (inactiveInstancesByHost.isEmpty()) {
        	addObjectsFromArrayIfAbsentToErrorMessageArray(
        			new NSArray<String>("You must have at least one inactive instance to perform a graceful bounce."));
        	return;
        }
        
        int numToStartPerHost = 1;
        if (activeHosts.count() > 0) {
            numToStartPerHost = (int) (runningInstances.count() / activeHosts.count() * .1);
        }
        if (numToStartPerHost < 1) {
            numToStartPerHost = 1;
        }
        boolean useScheduling = true;

        for (MInstance instance : runningInstances) {
            useScheduling &= instance.schedulingEnabled() != null && instance.schedulingEnabled().booleanValue();
        }

        NSMutableArray<MInstance> startingInstances = new NSMutableArray<MInstance>();
        for (int i = 0; i < numToStartPerHost; i++) {
            for (MHost host : activeHosts) {
                NSArray<MInstance> inactiveInstances = inactiveInstancesByHost.objectForKey(host);
                if (inactiveInstances != null && inactiveInstances.count() >= i) {
                    MInstance instance = inactiveInstances.objectAtIndex(i);
                    log("Starting inactive instance " + instance.displayName() + " on host " + host.addressAsString());
                    startingInstances.addObject(instance);
                } else {
                    log("Not enough inactive instances on host: " + host.addressAsString());
                }
            }
        }
        for (MInstance instance : startingInstances) {
            if (useScheduling) {
                instance.setSchedulingEnabled(Boolean.TRUE);
            }
            instance.setAutoRecover(Boolean.TRUE);
        }
        handler().sendUpdateInstancesToWotaskds(startingInstances, activeHosts.allObjects());
        handler().sendStartInstancesToWotaskds(startingInstances, activeHosts.allObjects());
        boolean waiting = true;

        // wait until apps have started
        while (waiting) {
            handler().startReading();
            try {
                log("Checking for started instances");
                handler().getInstanceStatusForHosts(activeHosts.allObjects());
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
        log("Started instances sucessfully");

        // turn scheduling off
        for (MHost host : activeHosts) {
            NSArray<MInstance> currentInstances = activeInstancesByHost.objectForKey(host);
            for (MInstance instance : currentInstances) {
                if (useScheduling) {
                    instance.setSchedulingEnabled(Boolean.FALSE);
                }
                instance.setAutoRecover(Boolean.FALSE);
            }
        }

        handler().sendUpdateInstancesToWotaskds(runningInstances, activeHosts.allObjects());

        // then start to refuse new sessions
        for (MHost host : activeHosts) {
            NSArray<MInstance> currentInstances = activeInstancesByHost.objectForKey(host);
            for (MInstance instance : currentInstances) {
                instance.setRefusingNewSessions(true);
            }
        }
        handler().sendRefuseSessionToWotaskds(runningInstances, activeHosts.allObjects(), true);
        log("Refused new sessions: " + runningInstances);

        // turn scheduling on again, but only
        NSMutableArray<MInstance> restarting = new NSMutableArray<MInstance>();
        for (MHost host : activeHosts) {
            NSArray<MInstance> currentInstances = activeInstancesByHost.objectForKey(host);
            for (int i = 0; i < currentInstances.count() - numToStartPerHost; i++) {
                MInstance instance = currentInstances.objectAtIndex(i);
                if (useScheduling) {
                    instance.setSchedulingEnabled(Boolean.TRUE);
                }
                instance.setAutoRecover(Boolean.TRUE);
                restarting.addObject(instance);
            }
        }
        handler().sendUpdateInstancesToWotaskds(restarting, activeHosts.allObjects());
        log("Started scheduling again: " + restarting);

        handler().startReading();
        try {
            handler().getInstanceStatusForHosts(activeHosts.allObjects());
            log("Finished");
        } finally {
            handler().endReading();
        }
    }
}