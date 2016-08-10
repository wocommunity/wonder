package er.distribution;

import er.extensions.ERXFrameworkPrincipal;

public class ERDistributionPrincipal extends ERXFrameworkPrincipal {

	public static final String FRAMEWORK_NAME = "ERDistribution";
	
	static {
		setUpFrameworkPrincipalClass(ERDistributionPrincipal.class);
	}

	@Override
	public void finishInitialization() {
	}
	
}
