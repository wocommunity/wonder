package er.attributeextension;

import java.lang.reflect.Method;

import er.extensions.ERXExtensions;
import er.extensions.ERXFrameworkPrincipal;

public class ERAttributeExtension extends ERXFrameworkPrincipal {
    public static final Class<?>[] REQUIRES = new Class[] { ERXExtensions.class };

    protected static ERAttributeExtension sharedInstance;

    // Registers the class as the framework principal
    static {
        setUpFrameworkPrincipalClass(ERAttributeExtension.class);
    }

    @Override
    protected void initialize() {
    	super.initialize();
    	Method valueFactoryClassNameMethod = null;
    	try {
    		Class eoAttributeClass = Class.forName("com.webobjects.eoaccess.EOAttribute");
    		valueFactoryClassNameMethod = eoAttributeClass.getMethod("valueFactoryClassName", (java.lang.Class[])null);
    	} catch (ClassNotFoundException cnfe) {
    		log.error("Cannot find EOAttribute class. Something is very wrong.");
    	} catch (NoSuchMethodException nsme) {
    		valueFactoryClassNameMethod = null;
    	}
    	if (valueFactoryClassNameMethod == null)
    		log.error("The ERAttributeExtension framework requires the Project Wonder version of the EOAttribute class and "+
    				"that does not seem to be the version of the class which is loaded first in the classpath. Examine your build "+
    				"path to ensure that the Project Wonder frameworks occur before the Apple frameworks.");
    }

    public static ERAttributeExtension sharedInstance() {
        if (sharedInstance == null) {
            sharedInstance = sharedInstance(ERAttributeExtension.class);
        }
        return sharedInstance;
    }

    @Override
    public void finishInitialization() {
    	log.debug("ERAttributeExtension loaded");
    }
}
