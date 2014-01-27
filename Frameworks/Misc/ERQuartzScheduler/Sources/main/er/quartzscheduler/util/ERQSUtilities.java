package er.quartzscheduler.util;

import java.lang.reflect.Constructor;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.simpl.SimpleClassLoadHelper;

import er.quartzscheduler.foundation.ERQSJob;
import er.quartzscheduler.foundation.ERQSJobDescription;
import er.quartzscheduler.util.ERQSUtilities.COJobInstanciationException.ErrorType;

/**
 * ERQSUtilities helps you to call the methods:
 * <ul>
 * <li>willDelete</li>
 * <li>willSave</li>
 * <li>validateForDelete</li>
 * <li>validateForSave</li>
 * </ul>
 * 
 * by instantiating an object based on the job description class path.<p>
 * 
 * You can call directly ERQSUtilities.willSave(myJobDescriptionEO) for example but an instance of the job will be create each time.
 * You can also call createJobInstance(myJobDescriptionEO) and call the above methods yourself.<p>
 * Because the job class is not necessarily a subclass of ERQSJob (it can be a sub class of ERQSAbstractJob or just implement
 * the interface Job), the methods willDelete, willSave, validateForDelete, validateForSave checks if the instantiated object
 * is a ERQSJob object. If not the object is just returned.
 * 
 * @author Philippe Rabier
 *
 */
public class ERQSUtilities 
{
	/**
	 * This exception is thrown if the class to be instantiate doesn't exist or if it can't be instantiate like a wrong constructor
	 * for example.<p>
	 * Rather than create a hierarchy of classes corresponding to each different errors, we preferred to add an error type
	 * that gives more information about the error.
	 * 
	 * @author Philippe Rabier
	 * @see ErrorType
	 */
	public static class COJobInstanciationException extends Exception
	{
		public enum ErrorType
		{
			CLASS_NOT_FOUND,
			CONSTRUCTOR_ERROR,
			INSTANCE_ERROR;
		}
		private static final long serialVersionUID = 1L;
		private final ErrorType errorType;

		public COJobInstanciationException(final String message, final ErrorType type) 
		{
			super(message);
			errorType = type;
		}

		public COJobInstanciationException(final String msg, final ErrorType type, final Throwable cause)
		{
			super(msg, cause);
			errorType = type;
		}

		public Throwable getUnderlyingException()
		{
			return super.getCause();
		}

		public ErrorType getErrorType()
		{
			return errorType;
		}

		@Override
		public String toString()
		{
			Throwable cause = getUnderlyingException();
			if (cause == null || cause == this)
				return super.toString();
			else
				return new StringBuilder().append(super.toString()).append(" [See nested exception: ").append(cause).append(']').toString();
		}
	}

	protected static final Logger log = Logger.getLogger(ERQSUtilities.class);

	public static Job createJobInstance(final ERQSJobDescription jobDescription) throws COJobInstanciationException
	{
		if (jobDescription == null)
			throw new IllegalArgumentException("jobDescription can't be null");

		SimpleClassLoadHelper loader = new SimpleClassLoadHelper();
		Class<? extends Job> aJobClass = null;
		try 
		{
			aJobClass = (Class<? extends Job>) loader.loadClass(jobDescription.classPath());
		} catch (ClassNotFoundException e) 
		{
			throw new COJobInstanciationException("Class " + jobDescription.classPath() + " not found.", ErrorType.CLASS_NOT_FOUND);
		}

		Constructor<? extends Job> constructor = null;
		try 
		{
			constructor = aJobClass.getConstructor();
		} catch (Exception e) 
		{
			throw new COJobInstanciationException("Class " + jobDescription.classPath() + " not found.", ErrorType.CONSTRUCTOR_ERROR, e);
		}

		Job aJob = null;
		try 
		{
			aJob = constructor.newInstance();
		} catch (Exception e) 
		{
			throw new COJobInstanciationException("Class " + jobDescription.classPath() + " not found.", ErrorType.INSTANCE_ERROR, e);
		}
		return aJob;
	}

	public static Job willDelete(final ERQSJobDescription jobDescription) throws COJobInstanciationException
	{
		Job aJob = createJobInstance(jobDescription);
		if (aJob instanceof ERQSJob)
			((ERQSJob)aJob).willDelete(jobDescription);
		return aJob;
	}

	public static Job willSave(final ERQSJobDescription jobDescription) throws COJobInstanciationException
	{
		Job aJob = createJobInstance(jobDescription);
		if (aJob instanceof ERQSJob)
			((ERQSJob)aJob).willSave(jobDescription);
		return aJob;
	}

	public static Job validateForDelete(final ERQSJobDescription jobDescription) throws COJobInstanciationException
	{
		Job aJob = createJobInstance(jobDescription);
		if (aJob instanceof ERQSJob)
			((ERQSJob)aJob).validateForDelete(jobDescription);
		return aJob;
	}

	public static Job validateForSave(final ERQSJobDescription jobDescription) throws COJobInstanciationException
	{
		Job aJob = createJobInstance(jobDescription);
		if (aJob instanceof ERQSJob)
			((ERQSJob)aJob).validateForSave(jobDescription);
		return aJob;
	}
}
