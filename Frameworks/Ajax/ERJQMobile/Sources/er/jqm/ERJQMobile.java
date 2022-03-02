package er.jqm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import er.extensions.ERXFrameworkPrincipal;

/**
 * Just log on start up
 * @author stefanklein
 *
 */
public class ERJQMobile extends ERXFrameworkPrincipal
{
	public static Class[] REQUIRES = new Class[0];

	public static final Logger log = LoggerFactory.getLogger(ERJQMobile.class);

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
