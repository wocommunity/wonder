//
// DirectAction.java
// Project ERD2WTemplate
//
// Created by ak on Sun Apr 21 2002
//
package ag.kcmedia;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import er.extensions.*;
import ag.kcmedia.Jode.*;

public class DirectAction extends ERXDirectAction {
    static ERXLogger log = ERXLogger.getERXLogger(DirectAction.class);

    public DirectAction(WORequest aRequest) {
        super(aRequest);
    }

    ClassProxy selectedClass() {
        String className = context().request().stringFormValueForKey("className");
        if(className == null) {
            className = (String)valueForKeyPath("context.mutableUserInfo.className");
        }
        ClassProxy selectedClass = Jode.classProxyForName(className);
        return selectedClass;
    }

    public WOComponent methodsAction() {
        WOComponent nextPage = pageWithName("StringHolder");
        nextPage.takeValueForKey(selectedClass().methods() +"", "string");
        nextPage.takeValueForKey(new Boolean(true), "isDocumentation");

        return nextPage;
    }
    public WOComponent codeAction() {
        WOComponent nextPage = pageWithName("StringHolder");
        if(selectedClass() != null || true)
            nextPage.takeValueForKey(selectedClass().sourceCode(), "string");
        nextPage.takeValueForKey(new Boolean(false), "isDocumentation");
        return nextPage;
    }
    public WOComponent docsAction() {
        WOComponent nextPage = null;
        if(true) {
            nextPage = pageWithName("StringHolder");
            if(selectedClass() != null)
                nextPage.takeValueForKey(selectedClass().documentation(), "string");
            nextPage.takeValueForKey(new Boolean(true), "isDocumentation");
        } else {
            nextPage = pageWithName("WORedirect");
            if(selectedClass() != null)
                nextPage.takeValueForKey("file:///System" + selectedClass().documentationPath(), "URL");
        }
        return nextPage;
    }
    
    public WOActionResults defaultAction() {
        if(false) {
            WOComponent nextPage = pageWithName("JavaDocViewer");
            nextPage.takeValueForKey("com.webobjects.appserver.WOContext", "className");
            return nextPage;
        }
        return pageWithName("Main");
    }

    public WOActionResults findAction() {
        JavaBrowser jb = (JavaBrowser)pageWithName("JavaBrowser");
        jb.setStringToFind(context().request().stringFormValueForKey("what"));
        return jb;
    }

    public WOActionResults performActionNamed(String name) {
        int dot = name.indexOf(".");
        if(dot > 0) {
            String action = name.substring(0,dot);
            String className = name.substring(dot+1);
            takeValueForKeyPath(className, "context.mutableUserInfo.className");
            name = action;
        }
        return super.performActionNamed(name);
    }
}
