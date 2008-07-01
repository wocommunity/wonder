package er.sproutcore;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODynamicElement;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

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
        String[] scripts = SCUtilities.require(framework, name);
        for (int i = 0; i < scripts.length; i++) {
            String script = scripts[i];
            appendScript(response, context, script);
        }
    }

    private void appendScript(WOResponse response, WOContext context, String name) {
        String url = context.urlWithRequestHandlerKey(SproutCore.SC_KEY,name, null);
        response.appendContentString("<script");
        response._appendTagAttributeAndValue("src", url, false);
        response._appendTagAttributeAndValue("type", "text/javascript", false);
        response.appendContentString("></script>");
    }
    
}
