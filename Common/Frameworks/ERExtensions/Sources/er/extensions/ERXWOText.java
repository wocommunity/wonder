package er.extensions;
import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

/**
 * Patch for WOText to not include the <code>value</code> attribute.
 * This class will get used automatically at startup, so do not use it directly
 * But use <code>WOText</code> instead.
 * 
 * @created ak on Tue Oct 15 2002
 * @project ERExtensions
 */

public class ERXWOText extends ERXPatcher.DynamicElementsPatches.Text {
    /**
     * Public constructor
     * @param context the context
     */
    public ERXWOText(String string, NSDictionary nsdictionary,
                      WOElement woelement) {
            super(string, nsdictionary, woelement);
    }

    /** Overridden from WOInput to not append the <code>value</code> attribute. */
    protected void _appendValueAttributeToResponse(WOResponse woresponse,
                                                   WOContext wocontext) {
        /* empty */
    }
    
}
