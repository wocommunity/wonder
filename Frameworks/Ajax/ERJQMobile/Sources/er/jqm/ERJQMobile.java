package er.jqm;

import org.apache.log4j.Logger;

import er.extensions.ERXFrameworkPrincipal;

/**
 * Just log on start up
 * @author stefanklein
 *
 */
public class ERJQMobile extends ERXFrameworkPrincipal
{
	public static Class[] REQUIRES = new Class[0];

	@SuppressWarnings("hiding")
	public static final Logger log = Logger.getLogger(ERJQMobile.class);

	public static String frameworkName()
	{
		return "ERJQMobile";
	}

	@Override
	public void finishInitialization()
	{
		log.debug(frameworkName() + " loaded");
	}

}
