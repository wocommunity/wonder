package er.quartzscheduler.util;

import static org.quartz.DateBuilder.futureDate;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.quartz.DateBuilder.IntervalUnit;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.simpl.SimpleClassLoadHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOObjectStore;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.ERXFrameworkPrincipal;
import er.extensions.foundation.ERXProperties;
import er.quartzscheduler.foundation.ERQSJobDescription;
import er.quartzscheduler.foundation.ERQSJobListener;
import er.quartzscheduler.foundation.ERQSJobSupervisor;
import er.quartzscheduler.foundation.ERQSMyJobListener;
import er.quartzscheduler.foundation.ERQSMySupervisor;

/**
 * This framework principal is abstract so you must create you own class, put your code in the abstract method <code>getListOfJobDescription</code>,
 * implement the methods newEditingContext(), newEditingContext(osc) and that's it!<p>
 * Don't forget to include the following static code in your class:
 * <pre>
 * static 
 * {
 *    log.debug("MyClassThatExtendsCOSchedulerServiceFrameworkPrincipal: static: ENTERED");
 *    setUpFrameworkPrincipalClass(MyClassThatExtendsCOSchedulerServiceFrameworkPrincipal.class);
 *    log.debug("MyClassThatExtendsCOSchedulerServiceFrameworkPrincipal: static: DONE");
 * }<br>
 * </pre>
 * 
 * @author Philippe Rabier
 *
 */
public abstract class ERQSSchedulerServiceFrameworkPrincipal extends ERXFrameworkPrincipal 
{
	public static final String INSTANCE_KEY = "COInstanceKey";
	private static final Logger log = LoggerFactory.getLogger(ERQSSchedulerServiceFrameworkPrincipal.class);
	private static ERQSSchedulerServiceFrameworkPrincipal sharedInstance;
	private volatile Scheduler quartzSheduler;

	/** 
	 * 
	 * @return shared instance of framework principal
	 */
	public static ERQSSchedulerServiceFrameworkPrincipal getSharedInstance()
	{
		if (sharedInstance == null)
			throw new IllegalStateException("method: getSharedInstance: sharedInstance is null.");
		return sharedInstance;
	}
	
	public static void setSharedInstance(final ERQSSchedulerServiceFrameworkPrincipal aSharedInstance)
	{
		sharedInstance = aSharedInstance;
	}
	
	/**
	 *  Expects that this method never returns null but an empty array if there is no job
	 * 
	 * @return array of job description to check.
	 * 
	 */
	public abstract NSArray<? extends ERQSJobDescription> getListOfJobDescription(EOEditingContext editingContext);

	/** 
	 * This method is used by a job that subclasses ERQSAbstractJob. It must return an editing context and it's highly recommended that useAutolock() returns false.<br>
	 * It's also highly recommended to use a new object store coordinator if you work heavily with EOF.
	 * 
	 * We recommend that you create your own factory as follow:
	 * <pre>
	 * 	private static ERXEC.Factory manualLockingEditingContextFactory = new ERXEC.DefaultFactory() {

		protected EOEditingContext _createEditingContext(final EOObjectStore parent) 
		{
			return new MyEditingContext(parent == null ? EOEditingContext.defaultParentObjectStore() : parent) 
			{
				public boolean useAutoLock() {return false;}

				public boolean coalesceAutoLocks() {return false;}
			};
		}

	 * </pre>
	 * 
	 * Then implement newEditingContext() as follow:
	 * <pre>
	 * public EOEditingContext newEditingContext()
	 * {
	 *    EOObjectStoreCoordinator osc = ERXTaskObjectStoreCoordinatorPool.objectStoreCoordinator();
	 *    return COEditingContextFactory.newManualLockingEditingContext(osc);
	 * }
	 * </pre>
	 * @return new editingContext
	 */
	public abstract EOEditingContext newEditingContext();

	/** 
	 * This method is used by a job that subclasses ERQSAbstractJob. The first time a job asks for a new editing context, the 
	 * method newEditingContext() is called. Then the following requests call this method by passing the object store coordinator
	 * used the first time.
	*/
	public abstract EOEditingContext newEditingContext(EOObjectStore parent);

	/**
     * This method initializes the scheduler service.<p>
     * The following services must be set:
     * <ul>
     * <li> er.quartzscheduler.schedulerServiceToLaunch=true or false to launch or not the service
     * <li> er.quartzscheduler.triggersAutomaticallyPaused=true or false. If <code>true</code> any new job/trigger will be
     * in pause mode when added to the scheduler. Very useful when you are developing and debugging your code.
 	 * </ul>
     */
	@Override
	public void finishInitialization() 
	{
		if (log.isInfoEnabled())
			log.info("method: finishInitialization: ENTER: isSchedulerMustRun: {}", schedulerMustRun());
		setSharedInstance(this);
		if (schedulerMustRun())
		{
			try 
			{
				Scheduler scheduler = getScheduler();
				if (scheduler != null)
				{
					getScheduler().start();
					addJobListener(getDefaultJobListener());
					instantiateJobSupervisor();
					boolean shouldJobsBePausedAtLaunch = ERXProperties.booleanForKeyWithDefault("er.quartzscheduler.triggersAutomaticallyPaused", false);
					if (shouldJobsBePausedAtLaunch)
						getScheduler().pauseAll();
				}
				log.info("method: finishInitialization: DONE. {}", scheduler == null ? "The scheduler is not running." : "The scheduler has been successfully launched.");
			} catch (SchedulerException e) 
			{
				log.error("method: finishInitialization: error message: {}", e.getMessage(), e);
			}
		}
		else
			log.info("method: finishInitialization: DONE. The scheduler is not running.");
	}

	/**
	 * This method reads the the property er.quartzscheduler.schedulerServiceToLaunch<p>
	 * It's a static method because the sharedInstance could be null if it's not running.
	 * 
	 * @return <code>true</code> if the scheduler should run, <code>false</code> by default.
	 */
	public static boolean schedulerMustRun()
	{
		return ERXProperties.booleanForKeyWithDefault("er.quartzscheduler.schedulerServiceToLaunch", false);
	}
	
	/**
	 * Return the quartz scheduler which schedules the job described by ERQSJobDescription objects.<p>
	 * As the quartz scheduler is uses a ram job stores, everything is gone when the scheduler stops.<br>
	 * The persistence must be handled by your own EOs which must implement ERQSJobDescription interface.<p>
	 * You have several options to set quartz properties:
	 * <ul>
	 * <li>do nothing. Quartz will use the default property file quartz.properties in quartz.jar
	 * <li>define the property "org.quartz.properties" with the full path of your property file
	 * <li>define the properties "quartz.properties.fileName" and "quartz.properties.framework". If "quartz.properties.framework" is missing 
	 * the mainBundle is used to find the filePath of your property file.
	 * </ul>
	 * 
	 * @return the scheduler
	 * @see ERQSJobDescription
	 */
	public Scheduler getScheduler()
	{
		if (quartzSheduler == null)
		{
			try 
			{
				String propFileName = ERXProperties.stringForKey("quartz.properties.fileName");
				// Grab the Scheduler instance from the Factory
				// There is no synchronized mechanism because the ivar quartzSheduler is initialized when finishInitialization is called.
				if (propFileName != null)
				{
					String propFramework = ERXProperties.stringForKey("quartz.properties.framework");
					NSBundle bundle;
					String filePath = null;
					URL propFileURL = null;
					
					if (propFramework == null)
						bundle = NSBundle.mainBundle();
					else
						bundle = NSBundle.bundleForName(propFramework);
					if (bundle != null)
						propFileURL = bundle.pathURLForResourcePath(propFileName);
					if (propFileURL == null)
						log.error("method: getScheduler: unable to get the path to the properties file: {} in the framework: {}.\nThe Quartz scheduler is not launched.", propFileName, propFramework);
					else
					{
						filePath = propFileURL.getFile();
						StdSchedulerFactory sf = new StdSchedulerFactory();
						sf.initialize(filePath);
						quartzSheduler = sf.getScheduler();
					}
				}
				else
					quartzSheduler = StdSchedulerFactory.getDefaultScheduler();	

			} catch (SchedulerException e) 
			{
				log.error("method: getScheduler: exception", e);
			}
		}
		return quartzSheduler;
	}

	/**
	 * Return a list of all jobs handled by the scheduler, even those that are not managed by the framework, aka
	 * jobs that have been added manually.
	 * 
	 * @return immutable list of all job detail or an empty list
	 */
    public List<JobDetail> getAllJobs()
    {
    	try 
    	{
    		NSMutableArray<JobDetail> jobDetailList = new NSMutableArray<JobDetail>();
    		List<String> groups = getScheduler().getJobGroupNames();

    		for (int i = 0; i < groups.size(); i++)
    		{
    			String name = groups.get(i);
    			GroupMatcher<JobKey> matcher = GroupMatcher.groupEquals(name);
    			Set<JobKey> keys = getScheduler().getJobKeys(matcher);

    			for (JobKey jk : keys) 
    			{
    				JobDetail jd = getScheduler().getJobDetail(jk);
    				jobDetailList.add(jd);
    			}
    		}
    		return jobDetailList.immutableClone();
    	} catch (SchedulerException e) 
    	{
    		log.error("method: getAllJobs: execution error.", e);
    	}
    	return NSArray.emptyArray();
    }

	public boolean hasRunningJobs()
	{
		List<JobExecutionContext> executingJobs = null;
		try 
		{
			executingJobs = getScheduler().getCurrentlyExecutingJobs();
		} catch (SchedulerException e) 
		{
			log.error("method: hasRunningJobs: execution error", e);
		}
		return executingJobs != null && executingJobs.size() > 0;
	}

	public Trigger.TriggerState getTriggerState(final JobKey aJobKey)
	{
		Trigger aTrigger = getTriggerOfJob(aJobKey);
		if (aTrigger == null)
			return Trigger.TriggerState.NONE;
		try 
		{
			return getScheduler().getTriggerState(aTrigger.getKey());
		} catch (SchedulerException e) 
		{
			log.error("method: getTriggerState: error for JobKey: {}", aJobKey, e);
		}
		return Trigger.TriggerState.NONE;
	}
	
	public Trigger getTriggerOfJob(final JobKey aJobKey)
	{
    	try 
    	{
			if (getScheduler().getTriggersOfJob(aJobKey).size() > 0)
	    		return getScheduler().getTriggersOfJob(aJobKey).get(0);
		} catch (SchedulerException e) 
		{
			log.error("method: getTriggerOfJob: error for JobKey: {}", aJobKey, e);
		}
		return null;
	}
	
	public void triggerNow(final JobDetail aJob) throws SchedulerException
	{
		getScheduler().triggerJob(aJob.getKey(), aJob.getJobDataMap());
	}
	
	protected void instantiateJobSupervisor() 
	{
		Class<? extends Job> supervisorClass = ERQSJobSupervisor.class;
		if (this.getClass().isAnnotationPresent(ERQSMySupervisor.class))
		{
			String supervisorClassPath = this.getClass().getAnnotation(ERQSMySupervisor.class).value();

			SimpleClassLoadHelper loader = new SimpleClassLoadHelper();
			try 
			{
				supervisorClass = (Class<? extends Job>) loader.loadClass(supervisorClassPath);
			} catch (ClassNotFoundException e) 
			{
				log.error("method: instantiateJobSupervisor: load class error for supervisorClass: {}", supervisorClassPath, e);
			}
		}

		JobDataMap map = new JobDataMap();
		map.put(INSTANCE_KEY, getSharedInstance());
		JobDetail job = newJob(supervisorClass).withIdentity("JobSupervisor", Scheduler.DEFAULT_GROUP).usingJobData(map).build();
		
		Trigger trigger = newTrigger()
		.withIdentity("JobSupervisorTrigger")
		.startAt(futureDate(1, IntervalUnit.MINUTE))
		.withPriority(Trigger.DEFAULT_PRIORITY)
		.withSchedule(simpleSchedule()
				.withIntervalInMinutes(supervisorSleepDuration())
				.repeatForever())
				.build();
		// Attache data to the job
		try 
		{
			getScheduler().scheduleJob(job, trigger);
		} catch (SchedulerException e) 
		{
			log.error("method: instantiateJobSupervisor: unable to launch supervisor.", e);
		}
	}
	
	/**
	 * Use this method if you need to add other listeners jobs handled by COScheduler, aka job with group
	 * beginning with ERQSJobSupervisor.GROUP_NAME_PREFIX
	 * 
	 * @param newJobListener
	 */
	protected void addJobListener(final JobListener newJobListener) 
	{
		try 
		{
			GroupMatcher<JobKey> matcher = GroupMatcher.groupStartsWith(ERQSJobSupervisor.GROUP_NAME_PREFIX);
			getScheduler().getListenerManager().addJobListener(newJobListener, matcher);
		} catch (SchedulerException e) 
		{
			log.error("method: addJobListener: unable to add a job listener", e);
		}
	}

	/**
	 * This method returns a joblistener object. If you used annotation to change the default job listener, getDefaultJobListener()
	 * will return an instance of your own class. Otherwise, it will return a ERQSJobListener object
	 * 
	 * @return a JobListener object
	 */
	protected JobListener getDefaultJobListener()
	{
		JobListener aJobListener = null;
		Class<? extends JobListener> jobListenerClass = ERQSJobListener.class;
		if (this.getClass().isAnnotationPresent(ERQSMyJobListener.class))
		{
			String jobListenerClassPath = this.getClass().getAnnotation(ERQSMyJobListener.class).value();

			SimpleClassLoadHelper loader = new SimpleClassLoadHelper();
			loader.initialize();
			try 
			{
				jobListenerClass = (Class<? extends JobListener>) loader.loadClass(jobListenerClassPath);
			} catch (ClassNotFoundException e) 
			{
				log.error("method: getDefaultJobListener: load class error for jobListenerClass: {}", jobListenerClassPath, e);
			}
		}
		
	   	if (jobListenerClass != null) 
	   	{
	   		Constructor<? extends JobListener> constructor = null;
	   		try 
	   		{
	   			constructor = jobListenerClass.getConstructor(ERQSSchedulerServiceFrameworkPrincipal.class);
	   			aJobListener = constructor.newInstance(ERQSSchedulerServiceFrameworkPrincipal.getSharedInstance());
	   		} catch (SecurityException se) 
	   		{
	   			log.error("method: createJobInstance: getConstructor error", se);
	   		} catch (NoSuchMethodException nme) 
	   		{
	   			log.error("method: createJobInstance: getConstructor error: ", nme);
	   		} catch (IllegalArgumentException e) 
	   		{
	   			log.error("method: createJobInstance: newInstance error", e);
			} catch (InstantiationException e) 
			{
	   			log.error("method: createJobInstance: getConstructor error: ", e);
			} catch (IllegalAccessException e) 
			{
	   			log.error("method: createJobInstance: newInstance error", e);
			} catch (InvocationTargetException e) 
			{
	   			log.error("method: createJobInstance: newInstance error", e);
			}
	   	}
   		return aJobListener;
	}
	
	protected int supervisorSleepDuration()
	{
		return ERXProperties.intForKeyWithDefault("er.quartzscheduler.COJobSupervisor.sleepduration", ERQSJobSupervisor.DEFAULT_SLEEP_DURATION);
	}
	
	public synchronized void deleteAllJobs()
	{
		List<JobDetail> allJobs = getAllJobs();
		if (allJobs.size() > 0)
		{
			NSMutableArray<JobKey> jobKeys = new NSMutableArray<JobKey>(allJobs.size());
			for (JobDetail jobDetail : allJobs) 
			{
				jobKeys.add(jobDetail.getKey());
			}
			try 
			{
				getScheduler().deleteJobs(jobKeys);
			} catch (SchedulerException e) 
			{
	   			log.error("method: deleteAllJobs", e);
			}
		}
	}
	
	public synchronized void stopScheduler()
	{
		try 
		{
			getScheduler().shutdown();
		} catch (SchedulerException e) 
		{
			log.error("method: stopScheduler: exception: {}", e.getMessage(), e);
		}
	}
}
