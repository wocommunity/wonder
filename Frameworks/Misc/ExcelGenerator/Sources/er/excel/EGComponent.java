package er.excel;

import org.apache.log4j.Logger;

import com.webobjects.appserver.*;

import er.extensions.*;
import er.extensions.components.ERXStatelessComponent;

/**
 * Class for Excel Component EGComponent.
 *
 * @binding sample sample binding explanation
 *
 * @created ak on Wed Mar 03 2004
 * @project ExcelGenerator
 */

public class EGComponent extends ERXStatelessComponent {

    /** logging support */
    protected final Logger log = Logger.getLogger(getClass());
	
    /**
     * Public constructor
     * @param context the context
     */
    public EGComponent(WOContext context) {
        super(context);
    }
    
    public void reset() {
    	super.reset();
    	
    }
    
    public EGComponent parentExcelComponent() {
    	WOComponent parent = parent();
    	while(parent != null && !(parent instanceof EGComponent)) {
    		parent = parent.parent();
    	}
    	return (EGComponent)parent;
    }
}
