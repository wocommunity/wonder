package er.quartzscheduler.foundation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.quartz.JobExecutionException;

import er.quartzscheduler.util.ERQSSchedulerFP4Test;

public class ERQSAbstractJobTest 
{
	@Test (expected=IllegalStateException.class)
	public void testGetSchedulerWithNoScheduler() 
	{
		ERQSExtendedAbstractJob4Test aJob = new ERQSExtendedAbstractJob4Test();
		aJob.getScheduler();
	}

	@Test (expected=IllegalStateException.class)
	public void testGetSchedulerFPInstanceWithNoFP() 
	{
		ERQSExtendedAbstractJob4Test aJob = new ERQSExtendedAbstractJob4Test();
		aJob.getSchedulerFPInstance();
	}

	@Test (expected=IllegalStateException.class)
	public void testGetJobContextWithNoContext() 
	{
		ERQSExtendedAbstractJob4Test aJob = new ERQSExtendedAbstractJob4Test();
		aJob.getJobContext();
	}

	@Test (expected=IllegalStateException.class)
	public void testGetSchedulerFPInstance() 
	{
		ERQSExtendedAbstractJob4Test aJob = new ERQSExtendedAbstractJob4Test();
		aJob.getSchedulerFPInstance();
	}

	@Test
	public void testEditingContext() throws JobExecutionException 
	{
		ERQSJobExecutionContext4Test jec = new ERQSJobExecutionContext4Test();
		ERQSSchedulerFP4Test fp = new ERQSSchedulerFP4Test();
		jec.setSchedulerFP(fp);
		ERQSExtendedAbstractJob4Test aJob = new ERQSExtendedAbstractJob4Test();
		aJob.execute(jec);
		assertNotNull(aJob.editingContext());
	}

	@Test
	public void testGetResultMessage() throws JobExecutionException 
	{
		ERQSJobExecutionContext4Test jec = new ERQSJobExecutionContext4Test();
		ERQSSchedulerFP4Test fp = new ERQSSchedulerFP4Test();
		jec.setSchedulerFP(fp);
		ERQSExtendedAbstractJob4Test aJob = new ERQSExtendedAbstractJob4Test();
		aJob.execute(jec);
		aJob.setResultMessage("message");
		assertEquals(jec.getResult(), "message");
	}
}
