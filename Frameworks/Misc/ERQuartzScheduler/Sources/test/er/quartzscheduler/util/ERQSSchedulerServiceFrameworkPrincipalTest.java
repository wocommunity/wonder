package er.quartzscheduler.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Test;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import er.quartzscheduler.foundation.ERQSJobListener;
import er.quartzscheduler.foundation.ERQSJobSupervisor;

public class ERQSSchedulerServiceFrameworkPrincipalTest 
{
	public class MySupervisor implements Job
	{
		public void execute(final JobExecutionContext arg0) throws JobExecutionException 
		{
			// Nothing to do			
		}		
	}
	public class MyJobListener implements JobListener
	{

		public String getName() 
		{
			return MyJobListener.class.getName();
		}

		public void jobExecutionVetoed(final JobExecutionContext arg0) 
		{
			// Nothing to do			
			
		}

		public void jobToBeExecuted(final JobExecutionContext arg0) 
		{
			// Nothing to do			
			
		}

		public void jobWasExecuted(final JobExecutionContext arg0, final JobExecutionException arg1) 
		{
			// TODO Auto-generated method stub
			
		}
	}

	@Test (expected=IllegalStateException.class)
	public void testSharedInstanceNotInitialized()
	{
		ERQSSchedulerServiceFrameworkPrincipal.getSharedInstance();
	}

	@Test
	public void testSetSharedInstance() 
	{
		ERQSSchedulerServiceFrameworkPrincipal fp = new ERQSSchedulerFP4Test();
		ERQSSchedulerServiceFrameworkPrincipal.setSharedInstance(fp);
		assertEquals(ERQSSchedulerServiceFrameworkPrincipal.getSharedInstance(), fp);
	}

	@Test
	public void testInstantiateJobSupervisor() throws SchedulerException 
	{
		ERQSSchedulerFP4Test fp = new ERQSSchedulerFP4Test();
		fp.instantiateJobSupervisor();
		JobDetail supervisor = fp.getScheduler().getJobDetail(new JobKey("JobSupervisor", Scheduler.DEFAULT_GROUP));
		assertNotNull(supervisor);
		assertEquals(supervisor.getJobClass(), ERQSJobSupervisor.class);
		fp.stopScheduler();
	}

	@Test
	public void testSetJobListener() 
	{
		ERQSSchedulerServiceFrameworkPrincipal fp = new ERQSSchedulerFP4Test();
		assertEquals(fp.getDefaultJobListener().getClass(), ERQSJobListener.class);
	}

	@Test
	public void testGetDefaultJobListener() 
	{
		ERQSSchedulerServiceFrameworkPrincipal fp = new ERQSSchedulerFP4Test();
		ERQSSchedulerServiceFrameworkPrincipal.setSharedInstance(fp);
		assertTrue(fp.getDefaultJobListener() instanceof ERQSJobListener);
	}

	@Test
	public void testAddJobListener() throws SchedulerException 
	{
		Properties p = new Properties(System.getProperties());
		p.setProperty("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
		p.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
		System.setProperties(p);
		ERQSSchedulerFP4Test fp = new ERQSSchedulerFP4Test();
		ERQSSchedulerServiceFrameworkPrincipal.setSharedInstance(fp);
		fp.addJobListener(fp.getDefaultJobListener());
		JobListener aJobListener = fp.getScheduler().getListenerManager().getJobListener(ERQSJobListener.class.getName());
		assertNotNull(aJobListener);
	}

	@Test
	public void testDefaultSupervisorSleepDuration() 
	{
		ERQSSchedulerServiceFrameworkPrincipal fp = new ERQSSchedulerFP4Test();
		assertTrue(fp.supervisorSleepDuration() == ERQSJobSupervisor.DEFAULT_SLEEP_DURATION);
	}

	@Test
	public void testGetListOfJobDescription() 
	{
		ERQSSchedulerServiceFrameworkPrincipal fp = new ERQSSchedulerFP4Test();
		assertTrue(fp.getListOfJobDescription(null).size() == 0);
	}

	@Test
	public void testNewEditingContext() 
	{
		ERQSSchedulerServiceFrameworkPrincipal fp = new ERQSSchedulerFP4Test();
		assertNotNull(fp.newEditingContext());
	}

	@Test
	public void testStopScheduler() throws SchedulerException 
	{
		ERQSSchedulerServiceFrameworkPrincipal fp = new ERQSSchedulerFP4Test();
		Scheduler s = fp.getScheduler();
		fp.stopScheduler();
		boolean result = s.isShutdown();
		assertTrue(result);
	}
}
