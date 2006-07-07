package er.extensions;
import org.apache.log4j.Logger;

import com.webobjects.appserver.*;

/**
 * Displays stats on how long the various phases in the request-response loop took.
 * Be sure to drop it at the bottom of your page or page wrapper, so the appendToResponse times
 * of the parent component are taken into account.
 * @created ak on Wed Sep 24 2003
 * @project ERExtensions
 */

public class ERXDebugTimer extends ERXStatelessComponent {

    /** logging support */
    private static final Logger log = Logger.getLogger(ERXDebugTimer.class);
    protected long _awakeMillis;
    protected boolean _setAwake = true;
    public long totalMillis;

    /**
     * Public constructor
     * @param context the context
     */
    public ERXDebugTimer(WOContext context) {
        super(context);
    }
    
    public void awake() {
        if(_setAwake) {
            _awakeMillis = System.currentTimeMillis();
            _setAwake = false;
        }
    }
    public void appendToResponse(WOResponse r, WOContext c) {
        long currentMillis = System.currentTimeMillis();
        totalMillis = (int)(currentMillis - _awakeMillis);
        super.appendToResponse(r,c);
        _setAwake = true;
    }
}
