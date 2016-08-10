package er.quartzscheduler.foundation;

import org.quartz.JobExecutionException;

/**
 * This class does nothing but logs some information. It can be useful for testing purpose.<p>
 * It just logs some information when the jobdescription is saved, deleted and when the method _execute is called.
 * When _execute is called, a loop is executed and the thread is sleeping for 2s each time.
 * 
 * @author Philippe Rabier
 *
 */
public class ERQSJobDemo extends ERQSJob 
{
	static final int MAX_LOOP = 10;
	
	@Override
	protected void _execute() throws JobExecutionException 
	{
		ERQSJobDescription aJobDescription = getJobDescription();
		if (log.isDebugEnabled())
			log.debug("_execute: ENTER. name: " + aJobDescription.name() + " /group: " + aJobDescription.group());
	
		for (int i = 0; i < MAX_LOOP; i++) 
		{
			setResultMessage("_execute: i: " + i + "  name: " + aJobDescription.name() + " /group: " + aJobDescription.group());
			if (log.isDebugEnabled())
				log.debug("_execute: i: " + i + "  name: " + aJobDescription.name() + " /group: " + aJobDescription.group());
			try 
			{
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (log.isDebugEnabled())
			log.debug("_execute: DONE.");
		setResultMessage("_execute: DONE. "+ MAX_LOOP + " times");
	}

	@Override
	public void willDelete(final ERQSJobDescription jobDescription) 
	{
		// Nothing to do. Just log information
		if (log.isDebugEnabled())
			log.debug("method: willDelete has been called.");
	}

	@Override
	public void willSave(final ERQSJobDescription jobDescription) 
	{
		// Nothing to do. Just log information
		if (log.isDebugEnabled())
			log.debug("method: willSave has been called.");
	}

	@Override
	public void validateForDelete(final ERQSJobDescription jobDescription) 
	{
		// Nothing to do			
	}

	@Override
	public void validateForSave(final ERQSJobDescription jobDescription) 
	{
		// Nothing to do			
	}
}
