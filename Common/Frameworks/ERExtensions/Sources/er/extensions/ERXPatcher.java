package er.extensions;

import com.webobjects.foundation.*;

/**
 * Wrapper around the WO-private NSUtilities which allows for some Objective-C-Style poseAs.
 * Using these methods may or may not break in the future
 */
public class ERXPatcher  {

    /** logging support */
    public final static ERXLogger log = ERXLogger.getERXLogger(ERXPatcher.class);

    public ERXPatcher() {
    }

    /**
     * Returns the class registered for the name <code>className</code>.<br/>
     * Uses the private WebObjects class cache.
     *
     * @param className class name
     * @return class for the registered name or null
     */
    public static Class classForName(String className) {
        return _NSUtilities.classWithName(className);
    }
    
    /**
     * Sets the class registered for the name <code>className</code> to the given class.<br/>
     * Changes the private WebObjects class cache.
     *
     * @param clazz class object
     * @param className name for the class - normally clazz.getName()
     */
    public static void setClassForName(Class clazz, String className) {
        _NSUtilities.setClassForName(clazz, className);
    }
}
