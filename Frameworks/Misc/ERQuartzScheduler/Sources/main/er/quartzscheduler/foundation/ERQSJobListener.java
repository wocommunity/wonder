package er.quartzscheduler.foundation;

import java.text.MessageFormat;
import java.util.Date;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSValidation;

import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.localization.ERXLocalizer;
import er.javamail.ERMailDeliveryPlainText;
import er.quartzscheduler.util.ERQSSchedulerServiceFrameworkPrincipal;

/**
 * The job listener is called automatically before a job is executed and after it has been executed.<p>
 * When a job is candidate to be executed, the job listener posts a notification JOB_WILL_RUN through the NSNotificationCenter.
 * If you want to be notified, subscribe to the JOB_WILL_RUN notification name and read the notification userInfo to know which job
 * will be executed.<p>
 * When a job has been executed, the job listener posts a notification JOB_RAN through the NSNotificationCenter.
 * Again, if you want to be notified, subscribe to the JOB_WILL_RUN notification name and read the notification userInfo to know which job.
 * If the job fails, we can also get the exception from the userInfo with the key EXCEPTION_KEY.<p>
 * Depending on the nature of the job description, you have to check the following keys when you access to the userInfo:
 * <ul>
 * <li> ERQSJob.ENTERPRISE_OBJECT_KEY if the isEnterpriseObject() method of the job description returns true
 * <li> ERQSJob.NOT_PERSISTENT_OBJECT_KEY if the job description is not an enterprise object
 * </ul>
 * 
 * When the job has been executed, the listener logs information and can send an email. The content of the log and 
 * the email are identical.
 * 
 * @see #jobToBeExecuted
 * @see #jobWasExecuted
 * @see #sendMail
 * @see #logResult
 */
public class ERQSJobListener extends ERQSAbstractListener implements JobListener
{
	
	public static String JOB_WILL_RUN = "jobWillRun";
	public static String JOB_RAN = "jobRan";
	public static String EXCEPTION_KEY = "exceptionKey";
	public static final String DEFAULT_MAIL_SUBJECT_TEMPLATE = "Job info: {0} is done.";
	public static final String DEFAULT_MAIL_ERROR_MESSAGE_TEMPLATE = "Error message: {0}. It took {1}";
	public static final String DEFAULT_MAIL_SHORT_MESSAGE_TEMPLATE = "It took {0}.";
	public static final String DEFAULT_MAIL_MESSAGE_WITH_MORE_INFOS_TEMPLATE = "More informations: {0}. It took {1}.";

	public ERQSJobListener(final ERQSSchedulerServiceFrameworkPrincipal schedulerFPInstance) 
	{
		super(schedulerFPInstance);
	}

	/**
	 * This method is due to JobListener interface.
	 * Get the name of the JobListener.
	 */
	public String getName() 
	{
		return this.getClass().getName();
	}
	
	/**
	 * This method is due to JobListener interface.<p>
	 * Called by the Scheduler when a JobDetail  was about to be executed (an associated Trigger has occured),
	 * but a TriggerListener vetoed it's execution.<br>
	 * The method is empty.
	 */
	public void jobExecutionVetoed(final JobExecutionContext jobexecutioncontext) 
	{

	}

	/**
	 * This method is due to JobListener interface.<p>
	 * Called by the Scheduler when a JobDetail  is about to be executed (an associated Trigger has occurred).<p>
	 * Posts the notification JOB_WILL_RUN and a userInfo with a global ID if the key is ERQSJob.ENTERPRISE_OBJECT_KEY
	 * or directly the ERQSJobDescription object with the key ERQSJob.NOT_PERSISTENT_OBJECT_KEY
	 */
	public void jobToBeExecuted(final JobExecutionContext jobexecutioncontext) 
	{
		EOGlobalID id = null;
		ERQSJobDescription aJobDescription = null;
		try 
		{
			NSDictionary<String, Object> userInfo = null;

			id = (EOGlobalID) jobexecutioncontext.getMergedJobDataMap().get(ERQSJob.ENTERPRISE_OBJECT_KEY);

			if (id != null)
				userInfo = new NSDictionary<String, Object>(id, ERQSJob.ENTERPRISE_OBJECT_KEY);
			else
			{
				aJobDescription = (ERQSJobDescription) jobexecutioncontext.getMergedJobDataMap().get(ERQSJob.NOT_PERSISTENT_OBJECT_KEY);
				if (aJobDescription != null)
					userInfo = new NSDictionary<String, Object>(aJobDescription, ERQSJob.NOT_PERSISTENT_OBJECT_KEY);
			}
			if (userInfo != null && userInfo.size() > 0)
				NSNotificationCenter.defaultCenter().postNotification(JOB_WILL_RUN, null, userInfo);

			if(log.isInfoEnabled())
			{
				log.info("************** Job '" + jobexecutioncontext.getJobDetail().getKey().getGroup() + "." + jobexecutioncontext.getJobDetail().getKey().getName() + "' is starting. FireTime: " + jobexecutioncontext.getFireTime() + " /previousFireTime: " + jobexecutioncontext.getPreviousFireTime() + " /nextFireTime: " + jobexecutioncontext.getNextFireTime() + " **************");
			}
		} catch (Exception e) 
		{
			log.error("method: jobToBeExecuted: an error occured: EOGlobalID: " + id + " /jobDescription: " + aJobDescription, e);
		}	
	}

	/**
	 * This method is due to JobListener interface.
	 * Called by the Scheduler after a JobDetail  has been executed <p>
	 * It retrieve the ERQSJobDescription object from the datamap and updates the object.<br>
	 * It also send an email if <code>er.quartzscheduler.ERQSJobListener.sendingmail=true</code><p>
	 * @see #recipients(JobExecutionContext, boolean)
	 */
	public void jobWasExecuted(final JobExecutionContext jobexecutioncontext, final JobExecutionException jobexecutionexception) 
	{
		NSMutableDictionary<String, Object> userInfo = new NSMutableDictionary<String, Object>();
		String errorMsg = null;

		if (log.isDebugEnabled())
			log.debug("method: jobWasExecuted: job: " + jobexecutioncontext.getJobDetail() + " /exception: " + jobexecutionexception);

		if (jobexecutionexception != null)
		{
			errorMsg = jobexecutionexception.getMessage();
			userInfo.setObjectForKey(jobexecutionexception, EXCEPTION_KEY);
			log.error("method: jobWasExecuted: jobexecutionexception: ", jobexecutionexception);
		}

		// Even if there is an exception, we continue to put the jobDescription object in the userInfo
		if (jobexecutioncontext.getMergedJobDataMap() != null)
		{
			ERQSJobDescription aJobDescription = null;
			aJobDescription = (ERQSJobDescription) jobexecutioncontext.getMergedJobDataMap().get(ERQSJob.NOT_PERSISTENT_OBJECT_KEY);

			if (aJobDescription != null)
			{
				userInfo.setObjectForKey(aJobDescription, ERQSJob.NOT_PERSISTENT_OBJECT_KEY);
				updateJobDescription(jobexecutioncontext, aJobDescription);
			}

			if (aJobDescription == null)
			{
				EOGlobalID id = (EOGlobalID) jobexecutioncontext.getMergedJobDataMap().get(ERQSJob.ENTERPRISE_OBJECT_KEY);

				// We save in database if there is no exception.
				if (id != null && jobexecutionexception == null)
				{
					userInfo.setObjectForKey(id, ERQSJob.ENTERPRISE_OBJECT_KEY);
					EOEditingContext ec = editingContext();
					ec.lock();
					try
					{
						// aJobDescription eo is refreshed because it can have been modified by the job.
						// The job can use a different EOF stack so this ec doesn't know that it has changed.
						// If we don't refresh it, we could get a updateValuesInRowDescribedByQualifier exception.
						// Trust me, we can get this exception easily.
						ec.setFetchTimestamp(System.currentTimeMillis());
						aJobDescription = (ERQSJobDescription) ec.faultForGlobalID(id, ec);
						ec.refreshObject((EOEnterpriseObject) aJobDescription);
						
						if (log.isDebugEnabled())
							log.debug("method: jobWasExecuted: aJobDescription: " + aJobDescription);

						if (aJobDescription != null && aJobDescription.isEnterpriseObject())
						{
							updateJobDescription(jobexecutioncontext, aJobDescription);
							ec.saveChanges();
						}
					}  catch (NSValidation.ValidationException eValidation)
					{
						errorMsg = eValidation.getMessage();
						userInfo.setObjectForKey(eValidation, EXCEPTION_KEY);
						log.error("method: jobWasExecuted: validationException: ", eValidation);
					} catch (Exception e)
					{
						errorMsg = e.getMessage();
						userInfo.setObjectForKey(e, EXCEPTION_KEY);
						log.error("method: jobWasExecuted: exception when saving job description: ", e);
					} finally
					{
						ec.unlock();
					}
				}
			}

			logResult(jobexecutioncontext, errorMsg);
			// We read the value each time because this value can be changed dynamically in development.
			boolean isSendingMail = ERXProperties.booleanForKeyWithDefault("er.quartzscheduler.ERQSJobListener.sendingmail", false);
			if (isSendingMail)
				sendMail(getMailSubject(jobexecutioncontext), getMailContent(jobexecutioncontext, errorMsg), recipients(jobexecutioncontext, jobexecutionexception == null));
		}
		if (userInfo != null && userInfo.size() > 0)
			NSNotificationCenter.defaultCenter().postNotification(JOB_RAN, null, userInfo.immutableClone());
	}

	/**
	 * Return a list of recipients depending on the good or bad execution of the job.
	 * If the job ran successfully, the recipients are:
	 * <ul>
	 * <li>the recipients returned by the method recipients() of ERQSJobDescription
	 * <li>the email set by the property <code>er.quartzscheduler.ERQSJobListener.executionWithSuccess.to</code> if any
	 * </ul>
	 * If the job didn't run successfully, the recipients are:
	 * <ul>
	 * <li>the recipients returned by the method recipients() of ERQSJobDescription
	 * <li>the email set by the property <code>er.quartzscheduler.ERQSJobListener.executionWithError.to</code> if any
	 * </ul>
	 * @see ERQSJobDescription#recipients(boolean)
	 * 
	 * @param jobexecutioncontext
	 * @param jobRanSuccessfully
	 * @return a list of recipients
	 */
	protected NSArray<String> recipients(final JobExecutionContext jobexecutioncontext, final boolean jobRanSuccessfully)
	{
		ERQSJobDescription aJobDescription = getJobDescription(jobexecutioncontext, editingContext());
		NSArray<String> recipients = aJobDescription != null ? aJobDescription.recipients(jobRanSuccessfully) : null;
		String toEmail;
		if (jobRanSuccessfully)
			toEmail = ERXProperties.stringForKeyWithDefault("er.quartzscheduler.ERQSJobListener.executionWithSuccess.to","");				
		else
			toEmail = ERXProperties.stringForKeyWithDefault("er.quartzscheduler.ERQSJobListener.executionWithError.to","");
		
		if (toEmail.length() > 0)
		{
			if (recipients == null)
				recipients = new NSArray<String>(toEmail);
			else
				recipients = recipients.mutableClone().arrayByAddingObject(toEmail);
		}
		return recipients;
	}
	
	/**
	 * Update the first, last and next execution date attributes of jobDescription
	 * 
	 * @param jobexecutioncontext
	 * @param jobDescription
	 */
	protected void updateJobDescription(final JobExecutionContext jobexecutioncontext, final ERQSJobDescription jobDescription)
	{
		if (jobDescription.firstExecutionDate() == null && jobexecutioncontext.getFireTime() != null)
			jobDescription.setFirstExecutionDate(dateToNSTimestamp(jobexecutioncontext.getFireTime()));

		jobDescription.setLastExecutionDate(dateToNSTimestamp(jobexecutioncontext.getFireTime()));
		// The next fire time can be null, mainly if it's a simple trigger when launched manually for example.
		if (jobexecutioncontext.getNextFireTime() != null)
			jobDescription.setNextExecutionDate(dateToNSTimestamp(jobexecutioncontext.getNextFireTime()));
	}
	
	/**
	 * If log info is enabled, logResult logs informations about the job execution like the job duration. It can 
	 * also logs specific information if the job called the method setResult(message) before ending its duty.<p>
	 * But if something wrong happened, the log displays the message <code>errorMsg</code>.
	 * 
	 * @param jobexecutioncontext
	 * @param errorMsg
	 */
	protected void logResult(final JobExecutionContext jobexecutioncontext, final String errorMsg)
	{
		if(log.isInfoEnabled())
		{
			String jobFullName = jobexecutioncontext.getJobDetail().getKey().getGroup() + "." + jobexecutioncontext.getJobDetail().getKey().getName();
			String msg = (String) jobexecutioncontext.getResult();
			String duration = formattedDuration(jobexecutioncontext.getJobRunTime()); 
			if ((msg != null) && (msg.length() != 0))
				log.info("************** More informations about the job: '" + jobFullName + "' /Message: "+ msg	+ " **************");
			if (errorMsg != null)
				log.info("************** Execution error about the job: '" + jobFullName + "' /Error message: "+ errorMsg	+ " **************");
			else
				log.info("************** Job '" + jobFullName + "' is done and it took: " + duration + " **************");
		}
	}


	/**
	 * Return the mail subject.<p>
	 * An interesting improvement will be to use a localized template. Currently, the default message is:<br>
	 * <i>Job info: JobGroup.MyBeautifullJob is done.</i>
	 * 
	 * @param jobexecutioncontext (used to build the job full name)
	 * @return subject
	 */
	protected String getMailSubject(final JobExecutionContext jobexecutioncontext)
	{
		String subjectTemplate = (String) localizer().valueForKey("COScheduler.MailSubject");
		if (log.isDebugEnabled())
			log.debug("method: getMailSubject: subjectTemplate: " + subjectTemplate);
		if (subjectTemplate == null)
		{
			log.warn("method: getMailSubject: subjectTemplate is null but shouldn't be!!! / localizer: " + localizer());
			subjectTemplate = DEFAULT_MAIL_SUBJECT_TEMPLATE;
		}
		String jobFullName = jobexecutioncontext.getJobDetail().getKey().getGroup() + "." + jobexecutioncontext.getJobDetail().getKey().getName();
		return MessageFormat.format(subjectTemplate, jobFullName);
	}
	
	/**
	 * Return the mail content.<p>
	 * An interesting improvement will be to use a localized template. Currently, the default content is:<br>
	 * <i>More informations:blabla. It took 90s.</i> if the job returns additional informations or just 
	 * <i>It took 90s.</i>
	 * 
	 * @param jobexecutioncontext (used to get the job duration)
	 * @param errorMsg 
	 * @return subject
	 */
	protected String getMailContent(final JobExecutionContext jobexecutioncontext, final String errorMsg)
	{
		String duration = formattedDuration(jobexecutioncontext.getJobRunTime()); 
		if (errorMsg != null)
		{
			String mailErrorTemplate = (String) localizer().valueForKey("COScheduler.DefaultMailErrorMessage");
			if (log.isDebugEnabled())
				log.debug("method: getMailContent: mailErrorTemplate: " + mailErrorTemplate);
			if (mailErrorTemplate == null)
			{
				log.warn("method: getMailContent: mailErrorTemplate is null but shouldn't be!!! / localizer: " + localizer());
				mailErrorTemplate = DEFAULT_MAIL_ERROR_MESSAGE_TEMPLATE;
			}
			return MessageFormat.format(mailErrorTemplate, errorMsg, duration);
		}
		String message = (String) jobexecutioncontext.getResult();

		if (ERXStringUtilities.stringIsNullOrEmpty(message))
		{
			String mailTemplate = (String) localizer().valueForKey("COScheduler.DefaultMailShortMessage");
			if (log.isDebugEnabled())
				log.debug("method: getMailContent: DefaultMailShortMessage: mailTemplate: " + mailTemplate);
			if (mailTemplate == null)
			{
				log.warn("method: getMailContent: DefaultMailShortMessage is null but shouldn't be!!! / localizer: " + localizer());
				mailTemplate = DEFAULT_MAIL_SHORT_MESSAGE_TEMPLATE;
			}
			message = MessageFormat.format(mailTemplate, duration);
		}
		else
		{
			String mailTemplate = (String) localizer().valueForKey("COScheduler.DefaultMailMessageWithMoreInfos");
			if (log.isDebugEnabled())
				log.debug("method: getMailContent: DefaultMailMessageWithMoreInfos: mailTemplate: " + mailTemplate);
			if (mailTemplate == null)
			{
				log.warn("method: getMailContent: DefaultMailMessageWithMoreInfos is null but shouldn't be!!! / localizer: " + localizer());
				mailTemplate = DEFAULT_MAIL_MESSAGE_WITH_MORE_INFOS_TEMPLATE;
			}
			message = MessageFormat.format(mailTemplate, message, duration);
		}
		return message;
	}
	
	/**
	 * Sends an plain text email to:
	 * <ul>
	 * <li> the recipients passed as parameters.
	 * <li> the email stored in the properties file (er.quartzscheduler.ERQSJobListener.to=myEmail@domain.com)
	 * </ul>
	 * 
	 * The author is read from properties file (er.quartzscheduler.ERQSJobListener.from=myOtherEmail@domain.com)<p>
	 * 
	 * @throws IllegalStateException if from email is empty and the is no recipient at all.
	 * @param subject
	 * @param textContent
	 * @param recipients 
	 */
	protected void sendMail(final String subject, final String textContent, final NSArray<String> recipients)
	{
		try
		{
			String fromEmail = ERXProperties.stringForKeyWithDefault("er.quartzscheduler.ERQSJobListener.from","");
			if (fromEmail.length() == 0 || recipients == null || recipients.size() == 0)
				throw new IllegalStateException("method: sendMail: fromEmail or toEmail are empty: fromEmail: " + fromEmail + " /recipients: " + recipients);

			ERMailDeliveryPlainText plainText = new ERMailDeliveryPlainText(); 
			plainText.newMail(); 
			plainText.setFromAddress(fromEmail);
			plainText.setToAddresses(recipients);
			plainText.setSubject(subject); 
			plainText.setTextContent(textContent);
			plainText.sendMail(false);
		} 
		catch (AddressException e) 
		{ 
			log.error("Method: sendMail: ", e);
		} 
		catch (MessagingException e) 
		{ 
			log.error("Method: sendMail: ", e);
		}
	}

	/**
	 * Return a string used by the logger and the mail sending method.<p>
	 * If the duration is less than 180s, the duration is expressed in seconds otherwise there is a conversion in mn.
	 * 
	 * @param duration
	 * @return the formatted duration
	 */
	protected String formattedDuration(final long duration) {
		long durationInMinute = 0;
		long durationInSecond = (duration)/1000; //in seconds

		if (durationInSecond > 180)
		{
			durationInMinute = durationInSecond / 60;
			durationInSecond = durationInSecond % 60;
		}
		return durationInMinute == 0 ? durationInSecond + "s" : (durationInMinute + "mn " + durationInSecond+"s");
	}

	/**
	 * Utility method.
	 * 
	 * @param date
	 * @return the date in NSTimestamp format
	 */
	protected NSTimestamp dateToNSTimestamp(final Date date)
	{
		if (date !=null )
			return new NSTimestamp(date);
		return null;
	}
	
	protected ERXLocalizer localizer()
	{
		String language = ERXProperties.stringForKey("er.quartzscheduler.ERQSJobListener.defaultLanguage");
		if (log.isDebugEnabled())
			log.debug("method: localizer: language: " + language);
		ERXLocalizer localizer;
		if (ERXStringUtilities.stringIsNullOrEmpty(language))
			localizer =  ERXLocalizer.defaultLocalizer();
		else
			localizer = ERXLocalizer.localizerForLanguage(language);
		if (log.isDebugEnabled())
			log.debug("method: localizer: localizer: " + localizer + " /localizer.language: " + localizer.language());
		return localizer;
	}
}
