package er.sproutcore;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODynamicElement;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.appserver.ERXResponse;
import er.extensions.foundation.ERXThreadStorage;

public class SCStyleSheet extends WODynamicElement {

    WOAssociation _name;
    WOAssociation _framework;
    
    
    public SCStyleSheet(String arg0, NSDictionary arg1, WOElement arg2) {
        super(arg0, arg1, arg2);
        _name = (WOAssociation) arg1.objectForKey("name");
        _framework = (WOAssociation) arg1.objectForKey("framework");
    }

    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
        String name = (String) _name.valueInComponent(context.component());
        String framework = (String) (_framework == null ? "SproutCore" : _framework.valueInComponent(context.component()));
        ERXResponse styleResponse = ERXResponse.pushPartial(SCPageTemplate.CLIENT_CSS);
        String fullName = framework + "/english.lproj/" + name;
        appendStyle(styleResponse, context, fullName);
        ERXResponse.popPartial();
    }

    public static void appendStyle(WOResponse response, WOContext context, String name) {
        NSMutableArray<String> scripts = (NSMutableArray<String>) ERXThreadStorage.valueForKey("SCRequire.Css");
        if(scripts == null) {
            scripts = new NSMutableArray<String>();
            ERXThreadStorage.takeValueForKey(scripts, "SCRequire.Css");
        }
        if(!scripts.contains(name)) {
            String url = context.urlWithRequestHandlerKey(SproutCore.SC_KEY,name, null);
            response.appendContentString("<style");
            response._appendTagAttributeAndValue("type", "text/css", false);
            response._appendTagAttributeAndValue("src", url, false);
            response.appendContentString("></style>\n");
            scripts.addObject(name);
        }
    }
    
}
