package er.extensions.components.partials;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.foundation.NSDictionary;

import er.extensions.appserver.ERXResponse;

/**
 * Routes all rendering to the response with the specified key.
 * @author ak
 *
 */
public class ERXPartialWrapper extends WODynamicGroup {

    WOAssociation _key;

    public ERXPartialWrapper(String s, NSDictionary nsdictionary, WOElement woelement) {
        super(s, nsdictionary, woelement);
        _key = (WOAssociation) nsdictionary.objectForKey("key");
    }

    @Override
    public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
        String key = (String) _key.valueInComponent(wocontext.component());
        woresponse = ERXResponse.pushPartial(key);
        super.appendToResponse(woresponse, wocontext);
        woresponse = ERXResponse.popPartial();
    }
}
