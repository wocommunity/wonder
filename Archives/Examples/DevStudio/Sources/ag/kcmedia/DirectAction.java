//
// DirectAction.java
// Project ERD2WTemplate
//
// Created by ak on Sun Apr 21 2002
//
package ag.kcmedia;

import java.io.File;

import org.apache.log4j.Logger;

import ag.kcmedia.Jode.ClassProxy;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WORequest;
import com.webobjects.directtoweb.ERD2WRuleEditorModel;
import com.webobjects.foundation.NSArray;

import er.extensions.appserver.ERXDirectAction;

public class DirectAction extends ERXDirectAction {
    static Logger log = Logger.getLogger(DirectAction.class);

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
        nextPage.takeValueForKey(Boolean.valueOf(true), "isDocumentation");

        return nextPage;
    }
    public WOComponent codeAction() {
        WOComponent nextPage = pageWithName("StringHolder");
        if(selectedClass() != null || true)
            nextPage.takeValueForKey(selectedClass().sourceCode(), "string");
        nextPage.takeValueForKey(Boolean.valueOf(false), "isDocumentation");
        return nextPage;
    }
    public WOComponent docsAction() {
        WOComponent nextPage = null;
        if(true) {
            nextPage = pageWithName("StringHolder");
            if(selectedClass() != null)
                nextPage.takeValueForKey(selectedClass().documentation(), "string");
            nextPage.takeValueForKey(Boolean.valueOf(true), "isDocumentation");
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

    public WOComponent dumpRulesAction() {
        WOComponent nextPage = pageWithName("ERXStringHolder");
        String string = "Please a provide fileName parameter";
        String fileName = context().request().stringFormValueForKey("fileName");
        if(fileName != null) {
            ERD2WRuleEditorModel model = new ERD2WRuleEditorModel(new File(fileName));
            string = ((NSArray)model.publicRules().valueForKeyPath("description.@sort.toString")).componentsJoinedByString("\n");
        }
        nextPage.takeValueForKey(string, "value");
        nextPage.takeValueForKey(Boolean.FALSE, "escapeHTML");
        return nextPage;
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
