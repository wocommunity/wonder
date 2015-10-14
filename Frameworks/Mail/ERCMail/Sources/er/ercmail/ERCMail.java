package er.ercmail;

import er.extensions.ERXFrameworkPrincipal;

public class ERCMail extends ERXFrameworkPrincipal {
	protected static ERCMail sharedInstance;
	@SuppressWarnings("unchecked")
	public final static Class<? extends ERXFrameworkPrincipal> REQUIRES[] = new Class[] {};

	static {
		setUpFrameworkPrincipalClass(ERCMail.class);
	}

	public static ERCMail sharedInstance() {
		if (sharedInstance == null) {
			sharedInstance = sharedInstance(ERCMail.class);
		}
		return sharedInstance;
	}

	@Override
	public void finishInitialization() {
		log.debug("ERCMail loaded");
	}
}
