package er.plot;

import org.apache.log4j.Logger;

import er.extensions.ERXExtensions;
import er.extensions.ERXFrameworkPrincipal;

public class ERPlot extends ERXFrameworkPrincipal {

    public final static Class REQUIRES[] = new Class[] {ERXExtensions.class};

    /** logging support */
    public static final Logger log = Logger.getLogger(ERPlot.class);

    /** holds the shared instance reference */
    protected static ERPlot sharedInstance;

    /**
     * Registers the class as the framework principal
     */
    static {
        setUpFrameworkPrincipalClass(ERPlot.class);
    }

    /**
     * Gets the shared instance of the ERPlot.
     * @return shared instance.
     */
    public static ERPlot sharedInstance() {
        if (sharedInstance == null) {
            sharedInstance = (ERPlot)ERXFrameworkPrincipal.sharedInstance(ERPlot.class);
        }
        return sharedInstance;
    }

    /**
     * Called when it is time to finish the
     * initialization of the framework.
     */
    public void finishInitialization() {
        log.debug("finishInitialization");
    }
}    