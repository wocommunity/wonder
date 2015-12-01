/**
 * Provides classes to manage threaded background tasks using {@link <a href="http://www.quartz-scheduler.org/">Quartz scheduler</a>} 2.1 in a WebObjects application.<p>
 * 
 * <h2>Overview</h2>
 * This framework consists of these classes / interfaces:
 * <h4>ERQSJobDescription</h4>
 * This is the interface that you need to implement for your own classes which describes a job. You can implement this in a plain Java object or in an EO.
 * @see er.quartzscheduler.foundation.ERQSJobDescription
 * 
 * <h4>ERQSJobSupervisor</h4>
 * It's itself a job scheduled by Quartz. Periodically, it checks if new jobs must be handled by the scheduler, if jobs must be removed of if jobs must be modified.<br>
 * You can sub-class it and create your own. If you do that, use the following class annotation: @ERQSMySupervisor("com.mypackage.MySupervisor")
 * If you specify nothing, like @ERQSMySupervisor(), the default supervisor will be instantiate.
 * @see ERQSMySupervisor
 * @see er.quartzscheduler.foundation.ERQSJobSupervisor
  * 
 * <h4>ERQSJobListener</h4>
 * You can sub-class it and create your own. If you do that, use the following class annotation: @ERQSMyJobListener("com.mypackage.MyJobListener")
 * If you specify nothing, like @ERQSMyJobListener(), the default listener will be instantiate.
 * @see ERQSMyJobListener
 * @see er.quartzscheduler.foundation.ERQSJobListener
* 
 * <h4>ERQSAbstractJob</h4>
 * This abstract class is used by ERQSJobSupervisor.java and ERQSJob.java. Normally you shouldn't have to sub-class it directly but use ERQSJob.java.
 * @see er.quartzscheduler.foundation.ERQSAbstractJob
 * 
 * <h4>ERQSJob</h4>
 * ERQSJob is an abstract class you must use to develop your own job. It provides your code with methods like newEditingContex or the job description object linked to your job.
 * @see er.quartzscheduler.foundation.ERQSJob
 * 
 * <h2>Integration with WOApplication</h2>
 * 
 * <h4>First step: create an EOEntity for the job description</h4>
 * A job has 2 faces actually:
 * <ul>
 * <li>a description, aka a name, a trigger, …
 * <li>a job that runs and execute your code
 * </ul>
 * So you you have to design EOs that will handle persistence of your job description. Your EO class must implement ERQSJobDescription.<p>
 * However, it's not absolutely necessary to create enterprise objects. If your objects are pure java objects, don't forget to return false in the method isEnterpriseObject 
 * 
 * <h4>Second step: sub-class ERQSSchedulerServiceFrameworkPrincipal</h4>
 * Create your own framework principal and implement the methods:
 * <ul>
 * <li> getListOfJobDescription that is called by the job supervisor to know the list of jobs that must be handled by the Quartz scheduler.
 * <li> newEditingContext() called when a job needs a new ec
 * <li> newEditingContext(final EOObjectStore parent) called when a job needs a new ec
 * </ul>
 * Read more {@link er.quartzscheduler.util.ERQSSchedulerServiceFrameworkPrincipal#newEditingContext()}
 * 
 * <h4>Third step: develop you own jobs</h4>
 * Create a class which derives from ERQSJob and code the method _execute. That's it.<br>
 * Notice that you can use the method setResultMessage to send information when the job is running and when the job ends up.

 * <h4>The last step: set the quartz properties</h4>
 * There are several options to set the quartz properties.<br>
 * Read this javadoc {@link er.quartzscheduler.util.ERQSSchedulerServiceFrameworkPrincipal#getScheduler()}

 * <h2>Java Monitor co-operation</h2>
 * 
 * <pre>
 *  @Override
 *	public void refuseNewSessions(final boolean shouldRefuse)
 *  {
 *  	log.fatal("refuseNewSessions called with " + shouldRefuse);
 *  	if (shouldRefuse)
 *  	{
 *  		ERQSSchedulerServiceFrameworkPrincipal.getSharedInstance().deleteAllJobs();
 *  	}
 *  	super.refuseNewSessions(shouldRefuse);
 *  }
 *
 *  @Override
 *	public void _terminateFromMonitor()
 *  {
 *  	log.fatal("Told to terminate by JavaMonitor");
 *  	ERQSSchedulerServiceFrameworkPrincipal.getSharedInstance().deleteAllJobs();
 *  	super._terminateFromMonitor();
 *  }
 *
 *  @Override
 *	public boolean isTerminating()
 *  {
 *  	if (super.isTerminating())
 *  	{
 *  		try 
 *  		{
 *  			if (ERQSSchedulerServiceFrameworkPrincipal.getSharedInstance().hasRunningJobs())
 *  				return false;
 *
 *  			if (ERQSSchedulerServiceFrameworkPrincipal.getSharedInstance().getScheduler().isStarted())
 *  				ERQSSchedulerServiceFrameworkPrincipal.getSharedInstance().stopScheduler();
 *  		} catch (SchedulerException e) 
 *  		{
 *  			log.error("method: isTerminating", e);
 *  		}
 *  	}
 *  	return super.isTerminating();
 *  }
 * </pre>
 * 
 * <h2>Quartz documentation</h2>
 * To get more information about Quartz, you can read the documentation {@link <a href="http://www.quartz-scheduler.org/documentation">http://www.quartz-scheduler.org/documentation</a>} and the javadoc {@link <a href="http://quartz-scheduler.org/api/">http://quartz-scheduler.org/api/</a>}
 */
package er.quartzscheduler.foundation;

