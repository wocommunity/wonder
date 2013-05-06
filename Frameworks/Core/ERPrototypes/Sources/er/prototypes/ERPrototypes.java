package er.prototypes;

import er.extensions.ERXExtensions;
import er.extensions.ERXFrameworkPrincipal;

/**
HACKALERT
simple class that ensures correct framework reference with PBX
 */

public class ERPrototypes extends ERXFrameworkPrincipal {
	
	public static Class[] REQUIRES = {ERXExtensions.class};

    static {
        setUpFrameworkPrincipalClass(ERPrototypes.class);
    }
    
	@Override
	public void finishInitialization() {
      log.debug("ERPrototypes loaded");
	}
}
