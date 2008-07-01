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

public class SCRequire extends WODynamicElement {

    WOAssociation _name;
    WOAssociation _framework;
    
    
    public SCRequire(String arg0, NSDictionary arg1, WOElement arg2) {
        super(arg0, arg1, arg2);
        _name = (WOAssociation) arg1.objectForKey("name");
        _framework = (WOAssociation) arg1.objectForKey("framework");
    }

    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
        String name = (String) _name.valueInComponent(context.component());
        String framework = (String) (_framework == null ? "SproutCore" : _framework.valueInComponent(context.component()));
        ERXResponse scriptResponse = ERXResponse.pushPartial("javascripts_for_client");
        NSArray<String> scripts = SCUtilities.require(framework, name);
        for (String script : scripts) {
            appendScript(scriptResponse, context, script);
        }
        ERXResponse.popPartial();

    }

    public static void appendScript(WOResponse response, WOContext context, String name) {
        NSMutableArray<String> scripts = (NSMutableArray<String>) ERXThreadStorage.valueForKey("SCRequire.Scripts");
        if(scripts == null) {
            scripts = new NSMutableArray<String>();
            ERXThreadStorage.takeValueForKey(scripts, "SCRequire.Scripts");
        }
        if(!scripts.contains(name)) {
            String url = context.urlWithRequestHandlerKey(SproutCore.SC_KEY,name, null);
            response.appendContentString("<script");
            response._appendTagAttributeAndValue("type", "text/javascript", false);
            response._appendTagAttributeAndValue("src", url, false);
            response.appendContentString("></script>\n");
            scripts.addObject(name);
        }
    }
    
}
