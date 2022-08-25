package er.quartzscheduler.foundation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.JobExecutionContext;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOGlobalID;

import er.extensions.eof.ERXEC;
import er.quartzscheduler.util.ERQSSchedulerServiceFrameworkPrincipal;

/**
 * The abstract listener provides reusable methods like editingContext and getJobDescription to any listener like ERQSJobListener.<p>
 * This class can be used if you need to create your own job listener or trigger listener.
 * 
 * @author Philippe Rabier
 *
 */
public abstract class ERQSAbstractListener 
{
	protected static final Logger log = LoggerFactory.getLogger(ERQSAbstractListener.class);
	private final ERQSSchedulerServiceFrameworkPrincipal schedulerFPInstance;
	private EOEditingContext editingContext;

	public ERQSAbstractListener(final ERQSSchedulerServiceFrameworkPrincipal schedulerFPInstance) 
	{
		this.schedulerFPInstance = schedulerFPInstance;
	}
	
	/**
	 * Send back the ERQSJobDescription object attached to the job.
	 *
	 * @param context the JobExecutionContext
	 * @param ec
	 * @return the ERQSJobDescription object.
	 */
	protected ERQSJobDescription getJobDescription(final JobExecutionContext context, final EOEditingContext ec)
	{
		ERQSJobDescription aJobDescription = null;
		if (context.getMergedJobDataMap() != null)
		{
			EOGlobalID id = (EOGlobalID) context.getMergedJobDataMap().get(ERQSJob.ENTERPRISE_OBJECT_KEY);

			if (id != null)
				aJobDescription = (ERQSJobDescription) ec.faultForGlobalID(id, ec);
			else
				aJobDescription = (ERQSJobDescription) context.getMergedJobDataMap().get(ERQSJob.NOT_PERSISTENT_OBJECT_KEY);
		}
		return aJobDescription;
	}
	
	/**
	 * Return a "regular" editing context with the current objectStore using directly the ERXEC factory.
	 * 
	 * @return an ec
	 */
	protected EOEditingContext editingContext()
	{
		if (editingContext == null)
			editingContext = ERXEC.newEditingContext();
		return editingContext;
	}

	/**
	 * Return the instance of the scheduler framework principal instance
	 */
	public ERQSSchedulerServiceFrameworkPrincipal getSchedulerFPInstance()
	{
		return schedulerFPInstance;
	}
}