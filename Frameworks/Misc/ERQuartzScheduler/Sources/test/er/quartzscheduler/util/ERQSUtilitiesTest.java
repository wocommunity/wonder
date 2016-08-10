package er.quartzscheduler.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

import er.quartzscheduler.foundation.ERQSJob4Test;
import er.quartzscheduler.foundation.ERQSJobDescription4Test;
import er.quartzscheduler.util.ERQSUtilities.COJobInstanciationException;

public class ERQSUtilitiesTest 
{

	@Ignore
	private ERQSJobDescription4Test initialize()
	{
		ERQSJobDescription4Test jd = new ERQSJobDescription4Test();
		jd.setClassPath("er.quartzscheduler.foundation.ERQSJob4Test");
		return jd;
	}

	@Test
	public void testCreateJobInstance() throws COJobInstanciationException 
	{
		ERQSJobDescription4Test jd = initialize();
		ERQSJob4Test aJob = (ERQSJob4Test) ERQSUtilities.createJobInstance(jd);
		assertNotNull(aJob);
	}

	@Test
	public void testWillDelete() throws COJobInstanciationException 
	{
		ERQSJobDescription4Test jd = initialize();
		ERQSJob4Test aJob = (ERQSJob4Test) ERQSUtilities.willDelete(jd);
		assertTrue(aJob.isWillDeleteMethodCalled);
	}

	@Test
	public void testWillSave() throws COJobInstanciationException 
	{
		ERQSJobDescription4Test jd = initialize();
		ERQSJob4Test aJob = (ERQSJob4Test) ERQSUtilities.willSave(jd);
		assertTrue(aJob.isWillSaveMethodCalled);
	}

	@Test
	public void testValidateForDelete() throws COJobInstanciationException 
	{
		ERQSJobDescription4Test jd = initialize();
		ERQSJob4Test aJob = (ERQSJob4Test) ERQSUtilities.validateForDelete(jd);
		assertTrue(aJob.isValidateForDeleteMethodCalled);
	}

	@Test
	public void testValidateForSave() throws COJobInstanciationException 
	{
		ERQSJobDescription4Test jd = initialize();
		ERQSJob4Test aJob = (ERQSJob4Test) ERQSUtilities.validateForSave(jd);
		assertTrue(aJob.isValidateForSaveMethodCalled);
	}

	@Test (expected=COJobInstanciationException.class)
	public void testWrongClass() throws COJobInstanciationException 
	{
		ERQSJobDescription4Test jd = initialize();
		jd.setClassPath("NoClass");
		ERQSJob4Test aJob = null;
		aJob = (ERQSJob4Test) ERQSUtilities.validateForSave(jd);
		assertTrue(aJob.isValidateForSaveMethodCalled);
	}
}
