package er.quartzscheduler.foundation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.quartz.JobExecutionException;

import com.webobjects.foundation.NSTimestamp;

import er.quartzscheduler.util.ERQSSchedulerFP4Test;

public class ERQSJobTest 
{
	@Test
	public void testExecute() throws JobExecutionException 
	{
		ERQSJobExecutionContext4Test jec = new ERQSJobExecutionContext4Test(true);
		ERQSSchedulerFP4Test fp = new ERQSSchedulerFP4Test();
		jec.setSchedulerFP(fp);

		ERQSJobDescription4Test jdo = new ERQSJobDescription4Test();
		jdo.setIsEnterpriseObject(false);
		jdo.setLastExecutionDate(new NSTimestamp());
		jec.getMergedJobDataMap().put(ERQSJob.NOT_PERSISTENT_OBJECT_KEY, jdo);

		ERQSJob4Test aJob = new ERQSJob4Test();
		aJob.execute(jec);
		assertTrue(aJob.isExecuteMethodCalled);
		
		assertSame(aJob.getJobDescription(), jdo);
		assertNotNull(aJob.getLastExecutionDate());
	}
	
	@Test (expected=IllegalStateException.class)
	public void testGetJobDescriptionWithError()
	{
		ERQSJob4Test aJob = new ERQSJob4Test();
		aJob.getJobDescription();
	}
}
