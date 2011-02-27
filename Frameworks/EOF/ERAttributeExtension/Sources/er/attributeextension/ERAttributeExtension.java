package er.attributeextension;

import org.apache.log4j.Logger;

import er.extensions.ERXExtensions;
import er.extensions.ERXFrameworkPrincipal;

public class ERAttributeExtension extends ERXFrameworkPrincipal {
	public static final Class<?>[] REQUIRES = new Class[] { ERXExtensions.class };

	private static final Logger log = Logger.getLogger(ERAttributeExtension.class);

	protected static ERAttributeExtension sharedInstance;

	// Registers the class as the framework principal
	static {
		log.debug("Static Initializer for ERAttributeExtension");
		setUpFrameworkPrincipalClass(ERAttributeExtension.class);
	}

	public static ERAttributeExtension sharedInstance() {
		if (sharedInstance == null) {
			sharedInstance = sharedInstance(ERAttributeExtension.class);
		}
		return sharedInstance;
	}

	@Override
	public void finishInitialization() {
	}

}
