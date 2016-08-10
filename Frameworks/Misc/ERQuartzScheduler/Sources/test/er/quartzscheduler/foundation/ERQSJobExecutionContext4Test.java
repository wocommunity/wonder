package er.quartzscheduler.foundation;

import static org.quartz.JobBuilder.newJob;

import java.util.Date;

import org.quartz.Calendar;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import er.quartzscheduler.util.ERQSSchedulerServiceFrameworkPrincipal;

public class ERQSJobExecutionContext4Test implements JobExecutionContext
{
	Object result;
	JobDataMap map = new JobDataMap();
	final boolean withScheduler;

	public ERQSJobExecutionContext4Test()
	{
		withScheduler = false;
	}

	/**
	 * 
	 * @param withScheduler turns out that a quartz scheduler instance is required (see below).
	 */
	public ERQSJobExecutionContext4Test(final boolean withScheduler)
	{
		this.withScheduler = withScheduler;
	}
	
	public Object get(final Object obj) 
	{
		return null;
	}

	public Calendar getCalendar() 
	{
		return null;
	}

	public Date getFireTime() 
	{
		java.util.Calendar c = java.util.Calendar.getInstance();
		c.set(2010, 10, 01, 10, 15);
		return c.getTime();
	}

	public JobDetail getJobDetail() 
	{
		return newJob(er.quartzscheduler.foundation.ERQSExtendedAbstractJob4Test.class)
		.withIdentity("name", "group")
		.withDescription("description")
		.build();
	}

	public Job getJobInstance() 
	{
		return null;
	}

	public long getJobRunTime() 
	{
		return 0;
	}

	public JobDataMap getMergedJobDataMap() 
	{
		return map;
	}

	public Date getNextFireTime() 
	{
		java.util.Calendar c = java.util.Calendar.getInstance();
		c.set(2011, 12, 02, 10, 15);
		return c.getTime();
	}

	public Date getPreviousFireTime() 
	{
		return null;
	}

	public int getRefireCount() 
	{
		return 0;
	}

	public Object getResult() 
	{
		return result;
	}

	public Date getScheduledFireTime() 
	{
		return null;
	}

	public Scheduler getScheduler() 
	{
		if (withScheduler)
			try 
			{
				return StdSchedulerFactory.getDefaultScheduler();
			} catch (SchedulerException e) 
			{
				e.printStackTrace();
			}		
		return null;
	}

	public Trigger getTrigger() 
	{
		return null;
	}

	public boolean isRecovering() 
	{
		return false;
	}

	public void put(final Object obj, final Object obj1) 
	{
		
	}

	public void setResult(final Object obj) 
	{
		result = obj;
	}

	public void setSchedulerFP(final ERQSSchedulerServiceFrameworkPrincipal fp) 
	{
		map.put(ERQSSchedulerServiceFrameworkPrincipal.INSTANCE_KEY, fp);
	}

	public String getFireInstanceId() 
	{
		return null;
	}
}