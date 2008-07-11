package er.sproutcore;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODynamicElement;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOConstantValueAssociation;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.appserver.ERXResponse;
import er.extensions.foundation.ERXThreadStorage;

public class SCJavaScript extends WODynamicElement {
    
    protected Logger log = Logger.getLogger(getClass());

    WOAssociation _name;
    WOAssociation _framework;
    WOAssociation _key;
    WOAssociation _group;

    public SCJavaScript(String arg0, NSDictionary arg1, WOElement arg2) {
        super(arg0, arg1, arg2);
		_name = (WOAssociation) arg1.objectForKey("name");
		_framework = (WOAssociation) arg1.objectForKey("framework");
		if (_framework == null) {
			_framework = new WOConstantValueAssociation("SproutCore");
		}
        _key = (WOAssociation) arg1.objectForKey("key");
        if (_key == null) {
            _key = new WOConstantValueAssociation(SCPageTemplate.CLIENT_JS);
        }
        _group = (WOAssociation) arg1.objectForKey("group");
        if (_group == null) {
            _group = new WOConstantValueAssociation("sproutcore");
        }
	}

    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
        ERXResponse scriptResponse = ERXResponse.pushPartial((String) _key.valueInComponent(context.component()));
        String framework = (String) _framework.valueInComponent(context.component());
        String group = (String) _group.valueInComponent(context.component());
        if(framework.equals("SproutCore")) {
            appendScript(scriptResponse, context, "SproutCore/prototype/prototype.js");
        }
        NSArray<String> scripts;
        if(_name == null) {
            scripts = SCUtilities.requireAll(framework, group);
        } else {
            scripts = SCUtilities.require(framework, group, (String)_name.valueInComponent(context.component()));
        }
        log.debug("adding: " +scripts);
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
