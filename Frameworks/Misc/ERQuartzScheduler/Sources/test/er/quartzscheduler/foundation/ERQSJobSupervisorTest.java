package er.quartzscheduler.foundation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.quartzscheduler.util.ERQSSchedulerFP4Test;

public class ERQSJobSupervisorTest {

	@Test
	public void testBuildTriggerName() throws SchedulerException 
	{
		ERQSJobSupervisor js = new ERQSJobSupervisor();
		String name = js.buildTriggerName("name");
		assertEquals(name, "name" + ERQSJobSupervisor.TRIGGER_SUFFIX);
	}

	@Test
	public void testBuildGroup() throws SchedulerException 
	{
		ERQSJobSupervisor js = new ERQSJobSupervisor();
		String group = js.buildGroup("group");
		assertEquals(group, ERQSJobSupervisor.GROUP_NAME_PREFIX + "group");
		group = js.buildGroup(null);
		assertEquals(group, ERQSJobSupervisor.GROUP_NAME_PREFIX + Scheduler.DEFAULT_GROUP);
	}

	@Test
	public void testIsJobDescriptionValid() throws SchedulerException 
	{
		ERQSJobSupervisor js = new ERQSJobSupervisor();
		ERQSJobDescription4Test jd = new ERQSJobDescription4Test();
		jd.setClassPath("er.quartzscheduler.foundation.ERQSJobSupervisor");
		jd.setName(null);
		js.isJobDescriptionValid(jd);
		assertFalse(js.isJobDescriptionValid(jd));
		jd = new ERQSJobDescription4Test();
		jd.setClassPath("er.quartzscheduler.foundation.ERQSJobSupervisor");
		jd.setGroup(null);
		assertTrue(js.isJobDescriptionValid(jd));
		jd = new ERQSJobDescription4Test();
		jd.setClassPath("er.quartzscheduler.foundation.ERQSJobSupervisor");
		jd.setCronExpression(null); // A null cron expression is valid
		assertTrue(js.isJobDescriptionValid(jd));
		jd = new ERQSJobDescription4Test();
		jd.setClassPath(null);
		assertFalse(js.isJobDescriptionValid(jd));
	}

	@Test
	public void testGetClassString() throws SchedulerException 
	{
		ERQSJobSupervisor js = new ERQSJobSupervisor();
		Class<? extends ERQSJob> aClass = (Class<? extends ERQSJob>) js.getClass("er.quartzscheduler.foundation.ERQSJobSupervisor");
		assertEquals(ERQSJobSupervisor.class, aClass);
	}

	@Test
	public void testBuildJobDetail() throws SchedulerException 
	{
		ERQSJobExecutionContext4Test jec = new ERQSJobExecutionContext4Test(true);
		ERQSSchedulerFP4Test fp = new ERQSSchedulerFP4Test();
		jec.setSchedulerFP(fp);

		ERQSJobSupervisor js = new ERQSJobSupervisor();
		js.execute(jec);
		
		ERQSJobDescription4Test jd = new ERQSJobDescription4Test();
		jd.setIsEnterpriseObject(false);
		jd.setClassPath("er.quartzscheduler.foundation.ERQSJobSupervisor");
		jd.setCronExpression("0 0 12 * * ?");
		JobDetail job = js.buildJobDetail(jd);
		assertNotNull(job);
	}

	@Test
	public void testBuildTrigger() throws SchedulerException 
	{
		ERQSJobExecutionContext4Test jec = new ERQSJobExecutionContext4Test(true);
		ERQSSchedulerFP4Test fp = new ERQSSchedulerFP4Test();
		jec.setSchedulerFP(fp);

		ERQSJobSupervisor js = new ERQSJobSupervisor();
		js.execute(jec);
		
		ERQSJobDescription4Test jd = new ERQSJobDescription4Test();
		jd.setIsEnterpriseObject(false);
		jd.setClassPath("er.quartzscheduler.foundation.ERQSJobSupervisor");
		jd.setCronExpression("0 0 12 * * ?");
		JobDetail job = js.buildJobDetail(jd);
		Trigger t = js.buildTrigger(jd.name, jd.group, jd.cronExpression(), new JobDataMap(), job);
		assertNotNull(t);
	}

	@Test
	public void testExecute() throws SchedulerException 
	{
		ERQSJobExecutionContext4Test jec = new ERQSJobExecutionContext4Test(true);
		ERQSSchedulerFP4Test fp = new ERQSSchedulerFP4Test();
		jec.setSchedulerFP(fp);
	
		ERQSJobSupervisor js = new ERQSJobSupervisor();
		js.execute(jec);
		assertNotNull(js.getSchedulerFPInstance());
		assertNotNull(js.getJobContext());
	}

	@Test
	public void testBuildTriggerForJob() throws SchedulerException 
	{
		ERQSJobExecutionContext4Test jec = new ERQSJobExecutionContext4Test(true);
		ERQSSchedulerFP4Test fp = new ERQSSchedulerFP4Test();
		jec.setSchedulerFP(fp);

		ERQSJobSupervisor js = new ERQSJobSupervisor();
		js.execute(jec);
		
		ERQSJobDescription4Test jd = new ERQSJobDescription4Test();
		// We use a NSDictionary because there is a constructor easy to use
		jd.setJobInfos(new NSDictionary<>("Value", "key")); 
		jd.setIsEnterpriseObject(false);
		jd.setClassPath("er.quartzscheduler.foundation.ERQSJobSupervisor");
		jd.setCronExpression("0 0 12 * * ?");
		JobDetail job = js.buildJobDetail(jd);
		assertEquals(job.getJobDataMap().getString("key"), "Value");
		
		Trigger t = js.buildTriggerForJob(jd, job);
		assertNotNull(t);
	}

	/**
	 * We need to test add and remove in the same test because when the scheduler is created, it's a singleton that remains in 
	 * memory until the last test is executed.
	 * 
	 * @throws SchedulerException
	 */
	@Test
	public void testAddAndRemoveJob2Scheduler() throws SchedulerException 
	{
		ERQSJobExecutionContext4Test jec = new ERQSJobExecutionContext4Test(true);
		ERQSSchedulerFP4Test fp = new ERQSSchedulerFP4Test();
		jec.setSchedulerFP(fp);
	
		ERQSJobSupervisor js = new ERQSJobSupervisor();
		js.execute(jec);
		ERQSJobDescription4Test jd = new ERQSJobDescription4Test();
		jd.setClassPath("er.quartzscheduler.foundation.ERQSExtendedAbstractJob4Test");
		jd.setCronExpression("0 0 12 * * ?");
		jd.setIsEnterpriseObject(false);
		js.addJob2Scheduler(jd);
		JobKey aJobKey = new JobKey(ERQSJobDescription4Test.DEF_JOB_NAME,  ERQSJobSupervisor.GROUP_NAME_PREFIX + ERQSJobDescription4Test.DEF_GROUP_NAME);
		JobDetail job = fp.getScheduler().getJobDetail(aJobKey);
		assertNotNull(job);
		
		Set<JobKey> aSet = js.getScheduledJobKeys();
		assertTrue(aSet.size() == 1);
		
		ERQSJobDescription4Test newJd = new ERQSJobDescription4Test();
		newJd.setName("newName");
		NSArray<ERQSJobDescription> newJobsList = new NSArray<>(newJd);
		// As the the new list of jobs doesn't contain the previous one, it must be removed.
		js.removeObsoleteJobs(newJobsList);
		aSet = js.getScheduledJobKeys();
		assertTrue(aSet.size() == 0);
	}

	/**
	 * We need to test add and remove in the same test because when the scheduler is created, it's a singleton that remains in 
	 * memory until the last test is executed.<p>
	 * Not the best option.
	 * 
	 * @throws SchedulerException
	 */
	@Test
	public void testJobs2AddOrModify() throws SchedulerException 
	{
		ERQSJobExecutionContext4Test jec = new ERQSJobExecutionContext4Test(true);
		ERQSSchedulerFP4Test fp = new ERQSSchedulerFP4Test();
		jec.setSchedulerFP(fp);
	
		ERQSJobSupervisor js = new ERQSJobSupervisor();
		js.execute(jec);
		ERQSJobDescription4Test jd1 = new ERQSJobDescription4Test();
		jd1.setName("jd1");
		jd1.setClassPath("er.quartzscheduler.foundation.ERQSExtendedAbstractJob4Test");
		jd1.setCronExpression("0 0 12 * * ?");
		jd1.setIsEnterpriseObject(false);
	
		ERQSJobDescription4Test jd2 = new ERQSJobDescription4Test();
		jd2.setName("jd2");
		jd2.setClassPath("er.quartzscheduler.foundation.ERQSExtendedAbstractJob4Test");
		jd2.setCronExpression("0 0 12 * * ?");
		jd2.setIsEnterpriseObject(false);
		NSArray<ERQSJobDescription> jds = new NSArray<>(new ERQSJobDescription4Test[] {jd1, jd2});
		js.addOrModifyJobs(jds);
		Set<JobKey> aSet = js.getScheduledJobKeys();
		assertTrue(aSet.size() == 2);
		jd2.setCronExpression("0 0 6 * * ?");
		JobKey jobKey4jd2 = js.getJobKeyForJobDescription(jd2);
	
		JobDetail job4jd2 = fp.getScheduler().getJobDetail(jobKey4jd2);
		js.modifyJob(jd2, job4jd2);
		List<? extends Trigger> triggers = fp.getScheduler().getTriggersOfJob(jobKey4jd2);
		assertNotNull(triggers);
		assertTrue(triggers.size() == 1);
		CronTrigger aTrigger = (CronTrigger)triggers.get(0);
		assertEquals(aTrigger.getCronExpression(), "0 0 6 * * ?");
		jd2.setClassPath("er.quartzscheduler.foundation.ERQSJobSupervisor");
		js.modifyJob(jd2, job4jd2);
		job4jd2 = fp.getScheduler().getJobDetail(jobKey4jd2);
		assertEquals("er.quartzscheduler.foundation.ERQSJobSupervisor", job4jd2.getJobClass().getName());		
	}

}
