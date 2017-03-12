package com.webobjects.monitor.application.starter;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.monitor._private.MApplication;
import com.webobjects.monitor._private.MHost;
import com.webobjects.monitor._private.MInstance;

/**
 * Bounces an application by refusing new sessions, waiting a while, shutting down all instances, then starting the same instances again.
 * 
 * @author ak
 */
public class ShutdownBouncer extends ApplicationStarter {

    private long _time;
    
    public ShutdownBouncer(MApplication app, int seconds) {
        super(app);
        _time = seconds * 1000;
    }

    @Override
    protected void bounce() throws InterruptedException {

        NSArray<MInstance> instances = application().instanceArray().immutableClone();
        NSMutableArray<MInstance> runningInstances = new NSMutableArray<>();
        NSMutableSet<MHost> activeHosts = new NSMutableSet<>();
        for (MInstance instance : instances) {
            MHost host = instance.host();
            if (instance.isRunning_M()) {
                runningInstances.addObject(instance);
                activeHosts.addObject(host);
            }
        }
        handler().sendRefuseSessionToWotaskds(runningInstances, activeHosts.allObjects(), true);
        boolean waiting = true;

        long startTime = System.currentTimeMillis();
        // wait until apps have started
        while (waiting && (_time + startTime > System.currentTimeMillis())) {
            handler().startReading();
            try {
                log("Checking for started instances");
                handler().getInstanceStatusForHosts(activeHosts.allObjects());
                boolean allStopped = false;
                for (MInstance instance : runningInstances) {
                    allStopped &= !instance.isRunning_M();
                }
                if (allStopped) {
                    waiting = false;
                } else {
                    sleep(10 * 1000);
                }
            } finally {
                handler().endReading();
            }
        }
        handler().sendStopInstancesToWotaskds(runningInstances, activeHosts.allObjects());
        log("Stopped instances sucessfully");

        handler().sendRefuseSessionToWotaskds(runningInstances, activeHosts.allObjects(), false);
        handler().sendStartInstancesToWotaskds(runningInstances, activeHosts.allObjects());

        handler().startReading();
        try {
            handler().getInstanceStatusForHosts(activeHosts.allObjects());
            log("Finished");
        } finally {
            handler().endReading();
        }
    }

}
