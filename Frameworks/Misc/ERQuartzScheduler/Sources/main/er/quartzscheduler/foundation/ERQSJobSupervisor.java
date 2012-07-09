package er.quartzscheduler.foundation;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.List;
import java.util.Set;

import org.quartz.CronTrigger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.ScheduleBuilder;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSSet;

import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXGenericRecord;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;
import er.quartzscheduler.util.ERQSSchedulerServiceFrameworkPrincipal;

/**
 * The supervisor has in charge to add, remove or update the list of job handled by the quartz scheduler.<p>
 * Every job handled by the supervisor has a group starting by GROUP_NAME_PREFIX. The goal is to let developers to add any
 * job directly, aka not linked to a job description. For that reason, by convention, the jobs not handled by the
 * supervisor must have a group not starting with GROUP_NAME_PREFIX
 * 
 * @author Philippe Rabier
 *
 */
@DisallowConcurrentExecution
public class ERQSJobSupervisor extends ERQSAbstractJob
{
	public static final String TRIGGER_SUFFIX = ERXProperties.stringForKeyWithDefault("er.quartzscheduler.foundation.ERQSJobSupervisor.suffix", ".CO");
	public static final int DEFAULT_SLEEP_DURATION = 10; //10 mn
	public static final String GROUP_NAME_PREFIX = ERXProperties.stringForKeyWithDefault("er.quartzscheduler.foundation.ERQSJobSupervisor.prefix", "CO.");

	@Override
	public void execute(final JobExecutionContext jobexecutioncontext) throws JobExecutionException 
	{
		super.execute(jobexecutioncontext);
		
		EOEditingContext ec = editingContext();
		ec.lock();
		try
		{
			NSArray<? extends ERQSJobDescription> jobs2Check = getSchedulerFPInstance().getListOfJobDescription(ec);
			setResultMessage("# of jobs to check: " + jobs2Check.size());
			if (log.isDebugEnabled())
				log.debug("method: execute: jobs2Check.size: " + jobs2Check.size());
			removeObsoleteJobs(jobs2Check);
			if (jobs2Check.size() != 0)
				addOrModifyJobs(jobs2Check);
		} catch (Exception e)
		{
			log.error("method: execute: fetching jobs.", e);
		}
		finally
		{
			ec.unlock();
			ec.dispose();
		}
	}

	/**
	 * Return a a set of jobs handled currently by Quartz. Actually, it's a set of JobKey rather than Job.
	 * 
	 * @return set of JobKeys, never return null but an empty set instead.
	 */
	protected Set<JobKey> getScheduledJobKeys()
	{
		Set<JobKey> scheduledJobKeys = null;
		try 
		{
			GroupMatcher<JobKey> matcher = GroupMatcher.groupStartsWith(GROUP_NAME_PREFIX);
			scheduledJobKeys = getScheduler().getJobKeys(matcher);
		} catch (SchedulerException e) 
		{
			log.error("method: getScheduledJobKeys: unable to get the list.", e);
		}
		return scheduledJobKeys == null ? new java.util.HashSet<JobKey>(0) : scheduledJobKeys;
	}

	/**
	 * From jobs2Check (a fresh list of ERQSJobDescription objects), removeJobs checks if jobs must be removed.<p>
	 * 
	 * @param jobs2Check list of ERQSJobDescription objects
	 */
	protected void removeObsoleteJobs(final NSArray<? extends ERQSJobDescription> jobs2Check)
	{
		NSSet<JobKey> jobKeys2remove;
		NSSet<JobKey> scheduledJobKeysSet = new NSSet<JobKey>(getScheduledJobKeys());

		// If the list of existing jobs is empty, nothing to remove
		if (scheduledJobKeysSet.size() != 0)
		{
			// If there is no new job, we must remove all existing jobs
			if (jobs2Check.size() == 0)
				jobKeys2remove = scheduledJobKeysSet;
			else
			{
				//NSSet<JobKey> scheduledJobKeysSet = new NSSet<JobKey>(scheduledJobKeys);
				//JobKey temp = scheduledJobKeysSet.anyObject();
				NSMutableSet<JobKey> jobKeys2Check = new NSMutableSet<JobKey>(jobs2Check.count());
				for (ERQSJobDescription aJob2Check : jobs2Check) 
				{
					JobKey aJobKey = getJobKeyForJobDescription(aJob2Check);
					jobKeys2Check.add(aJobKey);
				}
				jobKeys2remove = scheduledJobKeysSet.setBySubtractingSet(jobKeys2Check);
			}
			
			if (log.isDebugEnabled())
				log.debug("method: removeJobs: jobKeys2remove.size: " + jobKeys2remove.size());
			if (jobKeys2remove.size() != 0)
			{
				setResultMessage("# of jobs to remove: " + jobKeys2remove.size());
				try 
				{
					getScheduler().deleteJobs(jobKeys2remove.allObjects());
				} catch (SchedulerException e) 
				{
					log.error("method: removeJobs: unable to remove the jobs.", e);
				}
			}
		}
	}

	/**
	 * From jobs2Check (a fresh list of ERQSJobDescription objects), addOrModifyJobs checks if jobs must be added or modified.<p>
	 * 
	 * @param jobs2Check list of ERQSJobDescription objects
	 */
	protected void addOrModifyJobs(final NSArray<? extends ERQSJobDescription> jobs2Check)
	{
		setResultMessage("# of jobs to add or modify: " + jobs2Check.size());
		for (ERQSJobDescription aJob2Check : jobs2Check) 
		{
			JobKey aJobKey = getJobKeyForJobDescription(aJob2Check);
			try 
			{
				JobDetail aJobDetail = getScheduler().getJobDetail(aJobKey);
				if (log.isDebugEnabled())
					log.debug("method: jobs2AddOrModify: aJobKey: " + aJobKey + " /aJobDetail in scheduler: " + aJobDetail);
				
				if (aJobDetail == null)
					addJob2Scheduler(aJob2Check);
				else
					modifyJob(aJob2Check, aJobDetail);
				
			} catch (SchedulerException e) 
			{
				log.error("method: addOrModifyJobs: error when retrieving a jobDetail with this jobKey: " + aJobKey, e);
			}
		}
	}

	public JobKey getJobKeyForJobDescription(final ERQSJobDescription aJobDescription)
	{
		return new JobKey(aJobDescription.name(), buildGroup(aJobDescription.group()));
	}
	
	/**
	 * Add a job to the scheduler described the job description job2Add.<p>
	 * 
	 * @param job2Add job to add
	 */
	protected void addJob2Scheduler(final ERQSJobDescription job2Add) 
	{
		if (!isJobDescriptionValid(job2Add))
			throw new IllegalArgumentException("method: addJob2Scheduler: some fields of job2Add are null or empty: job2Check: " + job2Add);

		else
		{
			JobDetail job = buildJobDetail(job2Add);
			if (log.isDebugEnabled())
				log.debug("method: addJob2Scheduler: job: " + job);
			if (job != null)
			{
				Trigger trigger;
				try 
				{
					trigger = buildTriggerForJob(job2Add, job);
					getScheduler().scheduleJob(job, trigger);
				}
				catch (SchedulerException se) 
				{
					log.error("method: addJob2Scheduler: unable to schedule the job: " + job2Add.group() + "." + job2Add.name(), se);
				}
			}
		}
	}

	protected void modifyJob(final ERQSJobDescription job2Check, final JobDetail job) 
	{
		if (log.isDebugEnabled())
			log.debug("method: modifyJob: ENTER: job2Check: " + job2Check + " /job: " +job);
		if (!isJobDescriptionValid(job2Check))
			throw new IllegalArgumentException("method: applyModification2Scheduler: some fields of job2Check are null or empty: job2Check: " + job2Check);
		// We compare the job description with the scheduled job
		// We don't compare to the name and group because the job would have been removed and added just before.
		Scheduler scheduler = getScheduler();
		String jobClass = job.getJobClass().getName();
		String jobDescription = job.getDescription();
		String jobCronExpression;

		boolean isJobModified =  (!ERXStringUtilities.stringEqualsString(job2Check.jobDescription(), jobDescription) || !job2Check.classPath().equals(jobClass));
		try 
		{
			List<? extends Trigger> triggers = scheduler.getTriggersOfJob(job.getKey());
			if (triggers.size() != 0 && triggers.get(0) instanceof CronTrigger)
			{
				CronTrigger aTrigger = (CronTrigger) triggers.get(0);
				jobCronExpression = aTrigger.getCronExpression();
				
				if (!ERXStringUtilities.stringEqualsString(job2Check.cronExpression(), jobCronExpression) && !isJobModified)
				{
					//We just need to reschedule the job
					Trigger newTrigger = buildTriggerForJob(job2Check, job);
					TriggerKey aTriggerKey = new TriggerKey(buildTriggerName(job2Check.name()), buildGroup(job2Check.group()));
					scheduler.rescheduleJob(aTriggerKey, newTrigger);
					if (log.isDebugEnabled())
						log.debug("method: modifyJob: job2Check: " + job2Check + " has been rescheduled.");
				}
				if (isJobModified)
				{
					if (log.isDebugEnabled())
						log.debug("method: modifyJob: job2Check: " + job2Check + " has been removed then added.");
					// We remove the job and we create a new one
					getScheduler().deleteJob(job.getKey());
					addJob2Scheduler(job2Check);
				}
			}
		} catch (SchedulerException e) 
		{
			log.error("method: modifyJob: unable to get triggers of job: " + job2Check.group() + "." + job2Check.name(), e);
		}
		if (log.isDebugEnabled())
			log.debug("method: modifyJob: DONE: job2Check: " + job2Check + " /job: " +job + " /isJobModified: " + isJobModified);
	}

	/**
	 * Return a job detail built from a ERQSJobDescription object
	 * 
	 * @param jobDescription
	 * @return a JobDetail object
	 */
	protected JobDetail buildJobDetail(final ERQSJobDescription jobDescription)
	{
		JobDataMap map = new JobDataMap();
		map.put(ERQSSchedulerServiceFrameworkPrincipal.INSTANCE_KEY, getSchedulerFPInstance());
		if (jobDescription.isEnterpriseObject())
		{
			EOKeyGlobalID globalID = ((ERXGenericRecord)jobDescription).permanentGlobalID();
			map.put(ERQSJob.ENTERPRISE_OBJECT_KEY, globalID);
		}
		else
			map.put(ERQSJob.NOT_PERSISTENT_OBJECT_KEY, jobDescription);

		String name = jobDescription.name();
		String group = jobDescription.group();
		String classPath = jobDescription.classPath();
		String description = jobDescription.jobDescription();
		JobDetail job = null;
		Class<? extends Job> jobClass = getClass(classPath);
		if (jobClass != null)
		{
			job = newJob(jobClass)
			.withIdentity(name, buildGroup(group))
			.withDescription(description)
			.usingJobData(map)
			.build();
		}
		if (jobDescription.jobInfos() != null)
			job.getJobDataMap().putAll(jobDescription.jobInfos());
		return job;
	}

	/**
	 * Return a trigger built from a ERQSJobDescription object and a JobDetail object
	 * 
	 * @param jobDescription (we suppose that jobDescription is a subclass of ERXGenericRecord or a non persistent object)
	 * @param job
	 * @return a Trigger object
	 */
	protected Trigger buildTriggerForJob(final ERQSJobDescription jobDescription, final JobDetail job) 
	{
		String name = jobDescription.name();
		String group = jobDescription.group();
		String cronExpression = jobDescription.cronExpression();
		
		return buildTrigger(name, group, cronExpression, null, job);
	}

	protected Trigger buildTrigger(final String name, final String group, final String cronExpression, final JobDataMap map, final JobDetail job)
	{
		Trigger trigger = null;		
		ScheduleBuilder<? extends Trigger> scheduleBuilder = null;
		if (cronExpression != null)
		{
			try 
			{
				scheduleBuilder = cronSchedule(cronExpression);
			} catch (RuntimeException e) 
			{
				log.error("method: buildTrigger: cronExpression: " + cronExpression + " for name: " + name + " /group: " + group, e);
			}
		}
		else
			scheduleBuilder = simpleSchedule();
		
		trigger = newTrigger()
		.withIdentity(buildTriggerName(name), buildGroup(group))
		.withPriority(Trigger.DEFAULT_PRIORITY)
		.forJob(job)
		.usingJobData(map == null ? new JobDataMap() : map)
		.withSchedule(scheduleBuilder)
		.build();
		return trigger;
	}
	
	protected String buildTriggerName(final String name) 
	{
		return name + TRIGGER_SUFFIX;
	}

	protected String buildGroup(final String group) 
	{
		if (ERXStringUtilities.stringIsNullOrEmpty(group))
			return GROUP_NAME_PREFIX + Scheduler.DEFAULT_GROUP;
		return GROUP_NAME_PREFIX + group;
	}

	protected boolean isJobDescriptionValid(final ERQSJobDescription aJobDescription)
	{
		return (aJobDescription.classPath() != null && aJobDescription.classPath().length() != 0
				&& aJobDescription.name() != null  && aJobDescription.name().length() != 0
				);
	}

	protected Class<? extends Job> getClass(final String path) {
		Class<? extends Job> jobClass = null;
		try
		{
			jobClass = (Class<? extends Job>) Class.forName(path, false, this.getClass().getClassLoader());
		}
		catch (ClassNotFoundException ce)
		{
			log.error("method: getClass: path: " + path + " /exception: " + ce.getMessage(), ce);
		}
		catch (ExceptionInInitializerError ie)
		{
			log.error("method: getClass: path: " + path + " /exception: " + ie.getMessage(), ie);
		}
		catch (LinkageError le)
		{
			log.error("method: getClass: path: " + path + " /exception: " + le.getMessage(), le);
		}
		return jobClass;
	}
	
	@Override
	public EOEditingContext newEditingContext()
	{
		return ERXEC.newEditingContext();
	}
}
