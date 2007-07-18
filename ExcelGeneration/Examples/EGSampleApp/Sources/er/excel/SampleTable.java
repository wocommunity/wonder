package er.excel;

import org.apache.log4j.*;

import com.webobjects.appserver.*;

/**
 * Class for Excel Component SampleTable.
 *
 * @binding sample sample binding explanation
 *
 * @created ak on Thu Mar 04 2004
 * @project EGSampleApp
 */

public class SampleTable extends WOComponent {

    /** logging support */
    private static final Logger log = Logger.getLogger(SampleTable.class);
    
	public boolean enabled = false;
    
    /**
     * Public constructor
     * @param context the context
     */
    public SampleTable(WOContext context) {
        super(context);
    }
}
