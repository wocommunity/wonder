package er.quartzscheduler.foundation;

import java.util.Map;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSTimestamp;

/**
 * This framework doesn't embed an EOModel so it let you free to implement your own entity and enterprise objects.<p>
 * As a result, your EOs must implement this interface.<br>
 * Notice that a ERQSJobDescription object is not considered as an EO if the method <code>isEnterpriseObject</code> returns <code>false</code>.
 * 
 * @author Philippe Rabier
 *
 */
public interface ERQSJobDescription 
{
	/**
	 * The name and the group are very important because the scheduler retrieve jobs based on the name and group.<p>
	 * It can't be null.
	 * 
	 * @return job name
	 */
	String name();
	
	/**
	 * If group() return null or an empty string, Scheduler.DEFAULT_GROUP is used instead.
	 * 
	 * @return group
	 */
	String group();
	
	/**
	 * The cron expression allows you to define a period where the job is triggered.<br>
	 * If the cron expression returns null, the job runs once immediately.
	 * 
	 * See the documentation: http://www.quartz-scheduler.org/docs/tutorials/crontrigger.html
	 * 
	 * @return cron expression
	 */
	String cronExpression();
	
	/**
	 * The description is optional. It's used only when displaying the dashboard so it can give you additional
	 * informations useful for you.
	 * 
	 * @return a job description
	 */
	String jobDescription();
	
	/**
	 * Object that will be instantiated by the scheduler to make its job. Of course, very important ;-)
	 * 
	 * @return a class path
	 */
	String classPath();
	
	/**
	 * If you set up the listener to send email when the job is done, recipient() will be used to send emails.<p>
	 * Depending on the value of executionSucceeded, you can return a different list of recipients.
	 * 
	 * @param executionSucceeded <code>true</code> if the job ran successfully.
	 * @return array of recipients
	 */
	NSArray<String> recipients(boolean executionSucceeded);
	
	/**
	 * Very important: tells the supervisor and the listener if the current object is a EO. If yes, it must have 
	 * a global ID and we assume that it derives from ERXGenericRecord.
	 * 
	 * @return <code>true</code> if it's an EO
	 * @see er.quartzscheduler.foundation.ERQSJobSupervisor#buildTriggerForJob  buildTriggerForJob
	 */
	boolean isEnterpriseObject();
	
	/**
	 * A getter that returns the last execution date of the job.
	 * 
	 * @return last execution date
	 */
	NSTimestamp lastExecutionDate();
	void setLastExecutionDate(NSTimestamp lastExecutionDate);
	
	/**
	 * A getter that returns the first execution date of the job.
	 * 
	 * @return last execution date
	 */
	NSTimestamp firstExecutionDate();
	void setFirstExecutionDate(NSTimestamp firstExecutionDate);
	
	/**
	 * A setter to save the next execution date.<p>
	 * Notice that there is no getter because the framework doesn't need it to run. But it's a good idea to code it.
	 * 
	 * @param nextExecutionDate
	 */
	void setNextExecutionDate(NSTimestamp nextExecutionDate);
	
	/**
	 * jobInfos is used to pass information when the job will run.<p>
	 * All key/value pair will be given to the job.
	 */
	Map<String, Object> jobInfos();
}
