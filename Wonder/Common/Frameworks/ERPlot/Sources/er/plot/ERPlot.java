package er.plot;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import er.extensions.*;
import java.util.*;
import java.lang.reflect.*;

public class ERPlot extends ERXFrameworkPrincipal {
    
    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERPlot.class);

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