/**
 * 
 */
package er.quartzscheduler.util;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOObjectStore;
import com.webobjects.foundation.NSArray;

import er.quartzscheduler.foundation.ERQSJobDescription;
import er.quartzscheduler.foundation.ERQSMyJobListener;
import er.quartzscheduler.foundation.ERQSMySupervisor;

@ERQSMyJobListener("er.quartzscheduler.foundation.ERQSJobListener")
@ERQSMySupervisor("er.quartzscheduler.foundation.ERQSJobSupervisor")
public class ERQSSchedulerFP4Test extends ERQSSchedulerServiceFrameworkPrincipal 
{
	
	@Override
	public NSArray<ERQSJobDescription> getListOfJobDescription(final EOEditingContext editingContext) 
	{
		return NSArray.emptyArray();
	}

	@Override
	public EOEditingContext newEditingContext() 
	{
		return new MockEditingContext();
	}

	@Override
	public EOEditingContext newEditingContext(final EOObjectStore parent) 
	{
		return newEditingContext();
	}
}