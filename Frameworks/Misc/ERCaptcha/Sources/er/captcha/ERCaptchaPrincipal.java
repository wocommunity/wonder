package er.captcha;

import er.extensions.ERXExtensions;
import er.extensions.ERXFrameworkPrincipal;

public class ERCaptchaPrincipal extends ERXFrameworkPrincipal {
	public final static Class<?>[] REQUIRES = new Class[] { ERXExtensions.class };

	static {
		setUpFrameworkPrincipalClass(ERCaptchaPrincipal.class);
	}

	@Override
	public void finishInitialization() {

	}

}
