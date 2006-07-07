package er.excel;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import er.extensions.*;

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
    private static final ERXLogger log = ERXLogger.getERXLogger(SampleTable.class,"components,excel");
    
	public boolean enabled = false;
    
    /**
     * Public constructor
     * @param context the context
     */
    public SampleTable(WOContext context) {
        super(context);
    }
}
