package er.plot;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.StandardChartTheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import er.extensions.ERXExtensions;
import er.extensions.ERXFrameworkPrincipal;
import er.extensions.foundation.ERXProperties;

/**
 *
 * @property er.plot.useLegacyTheme
 */
public class ERPlot extends ERXFrameworkPrincipal {

    public final static Class<?> REQUIRES[] = new Class[] {ERXExtensions.class};

    private static final Logger log = LoggerFactory.getLogger(ERPlot.class);

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
            sharedInstance = ERXFrameworkPrincipal.sharedInstance(ERPlot.class);
        }
        return sharedInstance;
    }

    /**
     * Called when it is time to finish the
     * initialization of the framework.
     */
    @Override
	public void finishInitialization() {
        log.debug("finishInitialization");
        // make charts look like they looked in jfreechart-0.9.x by default
		if(ERXProperties.booleanForKeyWithDefault("er.plot.useLegacyTheme", false)) {
			ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());
		}
    }
}    
