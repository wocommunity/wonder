package er.sproutcore;

import java.io.File;
import java.io.FileFilter;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODynamicElement;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOConstantValueAssociation;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.appserver.ERXResponse;
import er.extensions.foundation.ERXFileUtilities;
import er.extensions.foundation.ERXThreadStorage;

public class SCStyleSheet extends WODynamicElement {

    WOAssociation _name;
    WOAssociation _framework;
    WOAssociation _group;
    WOAssociation _key;
    
    public SCStyleSheet(String arg0, NSDictionary arg1, WOElement arg2) {
        super(arg0, arg1, arg2);
        _name = (WOAssociation) arg1.objectForKey("name");
        _framework = (WOAssociation) arg1.objectForKey("framework");
        if (_framework == null) {
            _framework = new WOConstantValueAssociation("SproutCore");
        }
        _key = (WOAssociation) arg1.objectForKey("key");
        if (_key == null) {
            _key = new WOConstantValueAssociation(SCPageTemplate.CLIENT_CSS);
        }
        _group = (WOAssociation) arg1.objectForKey("group");
        if (_group == null) {
            _group = new WOConstantValueAssociation("sproutcore");
        }
    }

    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
        String framework = (String) _framework.valueInComponent(context.component());
        String group = (String) _group.valueInComponent(context.component());
        String key = (String) _key.valueInComponent(context.component());
        ERXResponse styleResponse = ERXResponse.pushPartial(key);
        NSMutableArray<String> scripts = new NSMutableArray<String>();
        if(_name == null) {
            File base = new File(SCUtilities.bundleResourcePath(framework));
            File[] files = ERXFileUtilities.listFiles(new File(base, group + File.separator + "english.lproj"), false, new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.getName().endsWith(".css");
                }});
            if(files != null) {
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    scripts.addObject(file.getName());
                }
            }
        } else {
            String name = (String) _name.valueInComponent(context.component());
            scripts.addObject(name);
        }
        for (String script : scripts) {
            String fullName = framework + "/" + group + "/english.lproj/" + script;
            appendStyle(styleResponse, context, fullName);
        }
        ERXResponse.popPartial();
    }

    @SuppressWarnings("unchecked")
    public static void appendStyle(WOResponse response, WOContext context, String name) {
        NSMutableArray<String> scripts = (NSMutableArray<String>) ERXThreadStorage.valueForKey("SCRequire.Css");
        if(scripts == null) {
            scripts = new NSMutableArray<String>();
            ERXThreadStorage.takeValueForKey(scripts, "SCRequire.Css");
        }
        if(!scripts.contains(name)) {
            String url = context.urlWithRequestHandlerKey(SproutCore.SC_KEY,name, null);
            response.appendContentString("<link");
            response._appendTagAttributeAndValue("type", "text/css", false);
            response._appendTagAttributeAndValue("rel", "stylesheet", false);
            response._appendTagAttributeAndValue("href", url, false);
            response.appendContentString("/>\n");
            scripts.addObject(name);
        }
    }
    
}
