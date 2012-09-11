package er.quartzscheduler.components;

import java.util.Collection;
import java.util.List;

import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.UnableToInterruptJobException;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.quartzscheduler.util.ERQSSchedulerServiceFrameworkPrincipal;

public class ERQSJobInformations extends WOComponent 
{
	private static final long serialVersionUID = 1L;
	public JobExecutionContext aJobContext;
	public JobDetail aJob;
	public String aKey, errorMessage = null;
	public Trigger aTrigger;
	public boolean dispDashboard = true, dispRunningJobs = false, dispScheduledJobs = false;

	public ERQSJobInformations(final WOContext context) 
	{
		super(context);
	}
    
    public Scheduler getScheduler()
    {
    	return ERQSSchedulerServiceFrameworkPrincipal.getSharedInstance().getScheduler();
    }
    
    public boolean hasErrorToDisplay()
    {
    	return errorMessage != null;
    }
    
    public Collection<JobExecutionContext> getJobsRunning()
    {
    	if (getScheduler() != null) {
			try {
				return getScheduler().getCurrentlyExecutingJobs();
			} catch (SchedulerException e) 
			{
				e.printStackTrace();
			}
		}
    	return NSArray.emptyArray();
    }
    
    public boolean isJobsRunningListEmpty()
    {
    	return !ERQSSchedulerServiceFrameworkPrincipal.getSharedInstance().hasRunningJobs();
    }
    
    public List<JobDetail> getAllJobs()
    {
    	return ERQSSchedulerServiceFrameworkPrincipal.getSharedInstance().getAllJobs();
    }
    
    public NSArray<Trigger> getTriggersOfJob() 
    {
    	try 
    	{
			return new NSArray<Trigger>(getScheduler().getTriggersOfJob(aJob.getKey()));
		} catch (SchedulerException e) 
		{
			e.printStackTrace();
		}
		return null;
    }
    
    public Trigger getFirstTrigger()
    {
    	return ERQSSchedulerServiceFrameworkPrincipal.getSharedInstance().getTriggerOfJob(aJob.getKey());
    }

	public WOActionResults stopJobAction() 
	{
		try 
		{
			ERQSSchedulerServiceFrameworkPrincipal.getSharedInstance().getScheduler().interrupt(aJob.getKey());
		} catch (UnableToInterruptJobException e) 
		{
			errorMessage = "Unable to stop the job: " + aJob.toString();
		}
		return null;
	}

	public String actionName() 
	{
		if (aJob == null)
			return "";
		Trigger.TriggerState state = ERQSSchedulerServiceFrameworkPrincipal.getSharedInstance().getTriggerState(aJob.getKey());
		if (state == Trigger.TriggerState.NORMAL || state == Trigger.TriggerState.BLOCKED)
			return "pause";
		if (state == Trigger.TriggerState.PAUSED)
			return "resume";
		return "error?";
	}

    public WOActionResults jobAction() throws SchedulerException 
	{
		Trigger.TriggerState state = ERQSSchedulerServiceFrameworkPrincipal.getSharedInstance().getTriggerState(aJob.getKey());
		if (state == Trigger.TriggerState.NORMAL || state == Trigger.TriggerState.BLOCKED)
			getScheduler().pauseJob(aJob.getKey());
		if (state == Trigger.TriggerState.PAUSED)
			getScheduler().resumeJob(aJob.getKey());
		
		return null;
	}

    public WOActionResults runNowAction() throws SchedulerException
    {
		ERQSSchedulerServiceFrameworkPrincipal.getSharedInstance().triggerNow(aJob);
		return null;
    }
    
	public String[] jobDataMapKeys()
    {
    	return aJobContext.getJobDetail().getJobDataMap().getKeys();
    }
    
    public Object jobDataMapInfo()
    {
    	return aJobContext.getJobDetail().getJobDataMap().get(aKey);
    }
    
    public WOComponent displayDashboard()
    {
    	errorMessage = null;
    	dispDashboard = true;
    	dispRunningJobs = false;
    	dispScheduledJobs = false;
    	return null;
    }
    
    public WOComponent displayRunningJobs()
    {
    	errorMessage = null;
    	dispDashboard = false;
    	dispRunningJobs = true;
    	dispScheduledJobs = false;
    	return null;
    }
    
    public WOComponent displayScheduledJobs()
    {
    	errorMessage = null;
    	dispDashboard = false;
    	dispRunningJobs = false;
    	dispScheduledJobs = true;
    	return null;
    }
  
    public WOComponent refresh()
    {
    	errorMessage = null;
    	return null;
    }

	public boolean isSchedulerRunning() 
	{
		return ERQSSchedulerServiceFrameworkPrincipal.schedulerMustRun();
	}
}