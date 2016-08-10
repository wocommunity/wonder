package er.quartzscheduler.foundation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

import er.extensions.foundation.ERXProperties;

public class ERQSJobListenerTest 
{
	boolean notificationReceived = false;
	
	@Test
	public void testGetName() 
	{
		ERQSJobListener jl = new ERQSJobListener(null);
		assertEquals("er.quartzscheduler.foundation.ERQSJobListener", jl.getName());		
	}

	@Ignore // Because this test need a ERXLocalizer object and we didn't succeed to make it work.
	public void testGetMailSubject() 
	{
		ERQSJobListener jl = new ERQSJobListener(null);
		ERQSJobExecutionContext4Test jec = new ERQSJobExecutionContext4Test();
		String subject = jl.getMailSubject(jec);
		assertTrue(subject.contains("Job info"));
	}

	@Ignore // Because this test need a ERXLocalizer object and we didn't succeed to make it work.
	public void testGetMailContent() 
	{
		ERQSJobListener jl = new ERQSJobListener(null);
		ERQSJobExecutionContext4Test jec = new ERQSJobExecutionContext4Test();
		String message = jl.getMailContent(jec, null);
		assertTrue(message.startsWith(" It took")); // doesn't contain more informations
		
		jec.setResult("My test message");
		message = jl.getMailContent(jec, null);
		assertTrue(message.contains("More informations"));
		
		message = jl.getMailContent(jec, "error");
		assertTrue(message.startsWith("Error message"));
	}

	@Test
	public void testJobToBeExecuted() 
	{
		ERQSJobListener jl = new ERQSJobListener(null);
		NSSelector sel = new NSSelector("register4Listener",new Class[] {NSNotification.class});
		NSNotificationCenter.defaultCenter().addObserver(this, sel, ERQSJobListener.JOB_WILL_RUN, null);
		ERQSJobExecutionContext4Test jec = new ERQSJobExecutionContext4Test();
		jec.getMergedJobDataMap().put(ERQSJob.NOT_PERSISTENT_OBJECT_KEY, new ERQSJobDescription4Test());
		
		jl.jobToBeExecuted(jec);
		assertTrue(notificationReceived);
	}

	public void register4Listener(final NSNotification notification)
	{
		NSDictionary dic = notification.userInfo();
		notificationReceived = dic.objectForKey(ERQSJob.NOT_PERSISTENT_OBJECT_KEY) != null;
	}
	
	@Test
	public void testNotificationJobWasExecuted() 
	{
		ERQSJobListener jl = new ERQSJobListener(null);
		NSSelector sel = new NSSelector("register4Listener",new Class[] {NSNotification.class});
		NSNotificationCenter.defaultCenter().addObserver(this, sel, ERQSJobListener.JOB_RAN, null);
		ERQSJobExecutionContext4Test jec = new ERQSJobExecutionContext4Test();
		jec.getMergedJobDataMap().put(ERQSJob.NOT_PERSISTENT_OBJECT_KEY, new ERQSJobDescription4Test());
		
		jl.jobWasExecuted(jec, null);
		assertTrue(notificationReceived);
	}
	
	@Test
	public void testRecipientsWhenJobWasExecutedWithSuccess() 
	{
		ERXProperties.setStringForKey("globalSuccessEmail@wocommunity.org", "er.quartzscheduler.ERQSJobListener.executionWithSuccess.to");
		ERXProperties.setStringForKey("globalFailEmail@wocommunity.org", "er.quartzscheduler.ERQSJobListener.executionWithError.to");
		ERQSJobListener jl = new ERQSJobListener(null);
		ERQSJobExecutionContext4Test jec = new ERQSJobExecutionContext4Test();
		jec.getMergedJobDataMap().put(ERQSJob.NOT_PERSISTENT_OBJECT_KEY, new ERQSJobDescription4Test());
		
		jl.jobWasExecuted(jec, null);
		NSArray<String> recipients = jl.recipients(jec, true);
		assertTrue(recipients.contains(ERQSJobDescription4Test.EMAIL_WHEN_SUCCEDED));
		assertTrue(recipients.contains("globalSuccessEmail@wocommunity.org"));
		assertFalse(recipients.contains("globalFailEmail@wocommunity.org"));
	}
	
	@Test
	public void testRecipientsWhenJobWasExecutedAndFailed() 
	{
		ERXProperties.setStringForKey("globalSuccessEmail@wocommunity.org", "er.quartzscheduler.ERQSJobListener.executionWithSuccess.to");
		ERXProperties.setStringForKey("globalFailEmail@wocommunity.org", "er.quartzscheduler.ERQSJobListener.executionWithError.to");
		ERQSJobListener jl = new ERQSJobListener(null);
		ERQSJobExecutionContext4Test jec = new ERQSJobExecutionContext4Test();
		jec.getMergedJobDataMap().put(ERQSJob.NOT_PERSISTENT_OBJECT_KEY, new ERQSJobDescription4Test());
		
		jl.jobWasExecuted(jec, null);
		NSArray<String> recipients = jl.recipients(jec, false);
		assertTrue(recipients.contains(ERQSJobDescription4Test.EMAIL_WHEN_FAILED));
		assertFalse(recipients.contains("globalSuccessEmail@wocommunity.org"));
		assertTrue(recipients.contains("globalFailEmail@wocommunity.org"));
	}

	@Test
	public void testUpdateJobDescription() 
	{
		ERQSJobListener jl = new ERQSJobListener(null);
		ERQSJobExecutionContext4Test jec = new ERQSJobExecutionContext4Test();
		ERQSJobDescription4Test jd = new ERQSJobDescription4Test();
		assertNull(jd.lastExecutionDate());
		assertNull(jd.firstExecutionDate());
		assertNull(jd.nextExecutionDate());
		jl.updateJobDescription(jec, jd);
		assertNotNull(jd.lastExecutionDate());
		assertNotNull(jd.firstExecutionDate());
		assertNotNull(jd.nextExecutionDate());
	}
}
