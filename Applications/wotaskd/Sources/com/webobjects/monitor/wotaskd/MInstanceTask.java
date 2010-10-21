package com.webobjects.monitor.wotaskd;

import java.util.TimerTask;

import com.webobjects.appserver.WOApplication;
import com.webobjects.foundation.NSLog;
import com.webobjects.monitor._private.MInstance;
import com.webobjects.monitor._private.MonitorException;

public abstract class MInstanceTask extends TimerTask {
	
	MInstance _instance;
	
	public MInstanceTask(MInstance instance) {
		super();
		_instance = instance;
	}
	
    public static class ForceQuit extends MInstanceTask {
    	
    	public ForceQuit(MInstance instance) {
    		super(instance);
    	}

		@Override
		public void run() {
			Application app = (Application) WOApplication.application();
			app._lock.startReading();
			try {
				_instance.setShouldDie(true);
				_instance.setForceQuitTask(null);
				cancel();
			}
			finally {
				app._lock.endReading();
			}
		}
    	
    }
    
    public static class Refuse extends MInstanceTask {
    	
    	private int _numberOfRetriesBeforeForceQuit;
    	private int retries = 0;
    	
    	public Refuse(MInstance instance, int numberOfRetriesBeforeForceQuit) {
    		super(instance);
    		_numberOfRetriesBeforeForceQuit = numberOfRetriesBeforeForceQuit;
    	}
    	
		@Override
		public void run() {
			
			Application app = (Application) WOApplication.application();
			app._lock.startReading();
			LocalMonitor localMonitor = app.localMonitor();
			try {
				
				if (retries >= _numberOfRetriesBeforeForceQuit) {
					//we only send a force quit if the instance is still running 
					if (_instance.isRunning_W())
						_instance.setShouldDie(true);
					
					_instance.setForceQuitTask(null);
					//stop this task from starting again
					cancel();
				}
				else if (_instance.isRefusingNewSessions() == false) {
					//resend the REFUSE command
					if (localMonitor.stopInstance(_instance) != null) {
						//we got a response, let's reset the retry
						//if retries reaches the max (WOTaskd.refuseNumRetries), force quit the instance
						retries = 0;
					}
				}
				
			}
			catch (MonitorException e) {
				NSLog.err.appendln("Exception while scheduling forceQuit: " + e.getMessage());
			}
			finally {
				++retries;
				app._lock.endReading();
			}
			
		}
    	
    }
    
}
