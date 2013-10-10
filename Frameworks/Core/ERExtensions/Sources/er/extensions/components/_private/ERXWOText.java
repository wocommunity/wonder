package er.extensions.components._private;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

import er.extensions.foundation.ERXPatcher;

/**
 * Patch for WOText to not include the <code>value</code> attribute.
 * This class will get used automatically at startup, so do not use it directly
 * But use <code>WOText</code> instead.
 * 
 * @author ak on Tue Oct 15 2002
 * @deprecated use {@link er.extensions.foundation.ERXPatcher.DynamicElementsPatches.Text} as parent class instead
 */
@Deprecated
public class ERXWOText extends ERXPatcher.DynamicElementsPatches.Text {
    /**
     * Public constructor
     */
    public ERXWOText(String string, NSDictionary nsdictionary,
                      WOElement woelement) {
            super(string, nsdictionary, woelement);
    }

    /** Overridden from WOInput to not append the <code>value</code> attribute. */
    @Override
    protected void _appendValueAttributeToResponse(WOResponse woresponse,
                                                   WOContext wocontext) {
        /* empty */
    }
}
