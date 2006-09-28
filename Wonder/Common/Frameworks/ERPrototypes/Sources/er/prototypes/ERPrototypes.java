package er.prototypes;

import er.extensions.ERXExtensions;
import er.extensions.ERXFrameworkPrincipal;

/**
HACKALERT
simple class that ensures correct framework reference with PBX
 */

public class ERPrototypes extends ERXFrameworkPrincipal {
	
	public static Class[] REQUIRES = {ERXExtensions.class};
	
	public void finishInitialization() {
      log.info("ERPrototypes loaded");
	}
}
