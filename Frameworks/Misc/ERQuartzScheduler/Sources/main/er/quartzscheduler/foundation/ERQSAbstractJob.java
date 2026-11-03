package er.quartzscheduler.foundation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOObjectStore;

import er.quartzscheduler.util.ERQSSchedulerServiceFrameworkPrincipal;

public class ERQSAbstractJob implements Job
{
	protected static final Logger log = LoggerFactory.getLogger(ERQSJobSupervisor.class);
	private EOEditingContext editingContext = null;
	private ERQSSchedulerServiceFrameworkPrincipal schedulerFPInstance;
	private JobExecutionContext jobContext;
	private EOObjectStore currentObjectStore;

	public ERQSAbstractJob() 
	{
		super();
		if (log.isDebugEnabled())
			log.debug("Constructor: " + this);
	}

	public void execute(final JobExecutionContext jobexecutioncontext) throws JobExecutionException 
	{
		schedulerFPInstance = (ERQSSchedulerServiceFrameworkPrincipal) jobexecutioncontext.getMergedJobDataMap().get(ERQSSchedulerServiceFrameworkPrincipal.INSTANCE_KEY);
		jobContext = jobexecutioncontext;

		if (log.isDebugEnabled())
		{
			String jobName = jobexecutioncontext.getJobDetail().getKey().getName();
			String jobGroup = jobexecutioncontext.getJobDetail().getKey().getGroup();
			log.debug("method: execute: jobName: " + jobGroup + "." +jobName);
			log.debug("method: excecute: scheduler: " + getScheduler() + " /schedulerFPInstance: " + schedulerFPInstance);
		}
	}

	/**
	 * Return the editing context associated to the job. The editing context is created automatically
	 * if no one exists.
	 * 
	 * @return editingContext
	 * @see #newEditingContext()
	 */
	protected EOEditingContext editingContext() 
	{
		if (editingContext == null)
			editingContext = newEditingContext();
		return editingContext;
	}

	/**
	 * Called by <code>editingContext()</code>. Should not be called directly except if you need several
	 * editingContext.
	 * 
	 * @return new editingContext
	 */
	protected EOEditingContext newEditingContext()
	{
		EOEditingContext newEc;
		if (currentObjectStore == null)
		{
			newEc = getSchedulerFPInstance().newEditingContext();
			currentObjectStore = newEc.parentObjectStore();
		}
		else
			newEc = getSchedulerFPInstance().newEditingContext(currentObjectStore);
		return newEc;
	}
	
	protected Scheduler getScheduler() 
	{
		return getJobContext().getScheduler();
	}

	protected ERQSSchedulerServiceFrameworkPrincipal getSchedulerFPInstance() 
	{
		if (schedulerFPInstance == null)
			throw new IllegalStateException("method: getSchedulerFPInstance: there is no schedulerFPInstance !!");
		return schedulerFPInstance;	
	}
	
	/**
	 * Returns the jobContext associated with the job. It can't be null otherwise an IllegalStateException is raised
	 * 
	 * @return the job context
	 * @throws IllegalStateException if the job context is null
	 */
	public JobExecutionContext getJobContext() {
		if (jobContext == null)
			throw new IllegalStateException("method: getJobContext: the job context is not yet initialized.");
		return jobContext;
	}

	/**
	 * Helper method to set a log message displayed through the web UI when the job is running. <p>
	 * It's used also when the job ends up to display and send a log message by email.
	 * 
	 * @param message
	 */
	public void setResultMessage(final String message) 
	{
		getJobContext().setResult(message);
	}
}