/**
 * 
 */
package er.quartzscheduler.foundation;

import java.util.Map;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSTimestamp;

public class ERQSJobDescription4Test implements ERQSJobDescription
{
	public static final String DEF_JOB_NAME = "jobName";
	public static final String DEF_GROUP_NAME = "groupName";
	public static final String EMAIL_WHEN_SUCCEDED = "success@wocommunity.org";
	public static final String EMAIL_WHEN_FAILED = "failed@wocommunity.org";
	String name = DEF_JOB_NAME;
	String group = DEF_GROUP_NAME;
	private String classPath;
	private String cronExpression;
	private boolean persistent = true;
	private NSTimestamp firstExecutionDate, lastExecutionDate, nextExecutionDate;
	private Map<String, Object> map;

	public ERQSJobDescription4Test()
	{
		// Nop
	}
	
	public String classPath() 
	{
		return classPath;
	}
	public void setClassPath(final String classPath)
	{
		this.classPath = classPath;
	}
	
	public String cronExpression() 
	{
		return cronExpression;
	}
	public void setCronExpression(final String cronExpression)
	{
		this.cronExpression = cronExpression;
	}

	public String group() 
	{
		return group;
	}
	public void setGroup(final String group)
	{
		this.group = group;
	}

	public boolean isEnterpriseObject() 
	{
		return persistent;
	}
	public void setIsEnterpriseObject(final boolean b) 
	{
		persistent  = b;	
	}

	public String jobDescription() 
	{
		return "test description";
	}

	public String name() 
	{
		return name;
	}
	public void setName(final String name)
	{
		this.name = name;
	}
	
	public NSArray<String> recipients(final boolean executionSucceeded) 
	{
		if (executionSucceeded)
			return new NSArray<String>(EMAIL_WHEN_SUCCEDED);
		return new NSArray<String>(EMAIL_WHEN_FAILED);
	}

	public void setFirstExecutionDate(final NSTimestamp firstExecutionDate) 
	{
		this.firstExecutionDate = firstExecutionDate;
	}
	
	public NSTimestamp firstExecutionDate() 
	{
		return firstExecutionDate;
	}

	public void setLastExecutionDate(final NSTimestamp lastExecutionDate) 
	{
		this.lastExecutionDate = lastExecutionDate;
	}

	public NSTimestamp lastExecutionDate() 
	{
		return lastExecutionDate;
	}

	public void setNextExecutionDate(final NSTimestamp nextExecutionDate) 
	{
		this.nextExecutionDate = nextExecutionDate;
	}

	public NSTimestamp nextExecutionDate() 
	{
		return nextExecutionDate;
	}

	public void setJobInfos(final Map<String, Object> aMap) 
	{
		map = aMap;
	}

	public Map<String, Object> jobInfos() 
	{
		return map;
	}
}