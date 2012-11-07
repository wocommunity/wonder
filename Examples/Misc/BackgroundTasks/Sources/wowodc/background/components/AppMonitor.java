package wowodc.background.components;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

/**
 * A page to monitor app processes, especially background processes
 *
 * @author kieran
 */
public class AppMonitor extends WOComponent {
    public AppMonitor(WOContext context) {
        super(context);
    }
}