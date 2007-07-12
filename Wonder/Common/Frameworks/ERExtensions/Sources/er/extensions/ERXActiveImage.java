package er.extensions;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

import er.extensions.ERXPatcher.DynamicElementsPatches.ActiveImage;
/**
 * Active image that allows for a tooltip as a binding. Gets patched into the runtime 
 * as WOActiveImage.
 *
 * @author ak
 */
public class ERXActiveImage extends ActiveImage {
    
    protected WOAssociation _alt;

    public ERXActiveImage(String tag, NSDictionary associations, WOElement element) {
        super(tag, associations, element);
        _alt = (WOAssociation) _associations.removeObjectForKey("alt");
     }

    protected void appendConstantAttributesToResponse(WOResponse woresponse, WOContext wocontext) {
        super.appendConstantAttributesToResponse(woresponse, wocontext);
        if(_alt != null) {
            String value = (String) _alt.valueInComponent(wocontext.component());
            if(value != null) {
                woresponse._appendTagAttributeAndValue("title", value, false);
            }
        }
    }
}
