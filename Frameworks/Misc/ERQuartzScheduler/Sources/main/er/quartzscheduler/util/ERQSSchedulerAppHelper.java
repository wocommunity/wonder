package er.quartzscheduler.util;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ERQSSchedulerAppHelper is an helper class that helps you to shut down the scheduler when the WO application
 * is terminating.<p>
 * 
 * Use it as follow:
 * <pre>
 * public void refuseNewSessions(final boolean shouldRefuse)
 * {
 *   ERQSSchedulerAppHelper.refuseNewSessions(this, shouldRefuse);
 * }
 * 
 * public void _terminateFromMonitor()
 * {
 *   ERQSSchedulerAppHelper._terminateFromMonitor();
 *   super._terminateFromMonitor();
 * }
 * 
 * public boolean isTerminating()
 * {
 *   return ERQSSchedulerAppHelper.isTerminating(super.isTerminating());
 * }
 * </pre>
 * @author Philippe Rabier
 *
 */
public class ERQSSchedulerAppHelper 
{
	private static final Logger log = LoggerFactory.getLogger(ERQSSchedulerAppHelper.class);

	 /**
     * When refusing new sessions is activated, all running threads are told to exit.
     *
     * @param shouldRefuse <code>true</code> if the application should start shutting down, <code>false</code> is ignored
     *
     * @see com.webobjects.appserver.WOApplication#refuseNewSessions(boolean)
     */
	public static void refuseNewSessions(final boolean shouldRefuse)
    {
    	log.info("method: refuseNewSessions called with {}", shouldRefuse);
    	if (shouldRefuse && ERQSSchedulerServiceFrameworkPrincipal.schedulerMustRun())
    	{
    		ERQSSchedulerServiceFrameworkPrincipal.getSharedInstance().deleteAllJobs();
    	}
     }
	
    /**
     * When JavaMonitor tells us to terminate, all jobs must be removed.  The application won't actually terminate
     * until all jobs are done.
     *
     * @see #isTerminating(boolean)
     */
	public static void _terminateFromMonitor()
    {
    	log.info("method: _terminateFromMonitor: Told to terminate by JavaMonitor");
    	if (ERQSSchedulerServiceFrameworkPrincipal.schedulerMustRun())
    		ERQSSchedulerServiceFrameworkPrincipal.getSharedInstance().deleteAllJobs();
    }

    /**
     * Overridden to return <code>false</code> if jobs are still running.  Termination is delayed until
     * the last job exits.
     *
     * @param terminating value given by the application (ERQSSchedulerAppHelper.isTerminating(super.isTerminating()))
     * @return <code>true</code> if the application is about to shut down
     *
     * @see com.webobjects.appserver.WOApplication#isTerminating()
     */
	public static boolean isTerminating(final boolean terminating)
    {
    	if (terminating && ERQSSchedulerServiceFrameworkPrincipal.schedulerMustRun())
    	{
    		if (ERQSSchedulerServiceFrameworkPrincipal.getSharedInstance().hasRunningJobs())
    			return false;

    		if (terminating)
    		{
    			try 
    			{
    				if (ERQSSchedulerServiceFrameworkPrincipal.getSharedInstance().getScheduler().isStarted())
    					ERQSSchedulerServiceFrameworkPrincipal.getSharedInstance().stopScheduler();
    			} catch (SchedulerException e) 
    			{
    				log.error("method: isTerminating", e);
    			}
    		}
    	}
    	return terminating;
    }
}
