package er.quartzscheduler.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import er.quartzscheduler.foundation.ERQSAbstractJobTest;
import er.quartzscheduler.foundation.ERQSJobListenerTest;
import er.quartzscheduler.foundation.ERQSJobSupervisorTest;
import er.quartzscheduler.foundation.ERQSJobTest;
import er.quartzscheduler.util.ERQSSchedulerServiceFrameworkPrincipalTest;
import er.quartzscheduler.util.ERQSUtilitiesTest;

@RunWith(Suite.class)
@Suite.SuiteClasses( {
	ERQSUtilitiesTest.class,
	ERQSSchedulerServiceFrameworkPrincipalTest.class,
	ERQSJobTest.class,
	ERQSJobSupervisorTest.class,
	ERQSJobListenerTest.class,
	ERQSAbstractJobTest.class
})

public class AllTests
{

}
