package er.cayenne;

import er.extensions.ERXFrameworkPrincipal;

public class CayennePrincipal extends ERXFrameworkPrincipal {

	public static final String FRAMEWORK_NAME = "ERCayenne";
	
	static {
		setUpFrameworkPrincipalClass(CayennePrincipal.class);
	}

	@Override
	public void finishInitialization() {
	}
	
}
