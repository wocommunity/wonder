//
// Sources/ag/kcmedia/StringHolder.java: Class file for WO Component 'StringHolder'
// Project DevStudio
//
// Created by ak on Thu Jul 25 2002
//
package ag.kcmedia;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXStatelessComponent;
import er.extensions.logging.ERXLogger;

public class StringHolder extends ERXStatelessComponent {
    static final ERXLogger log = ERXLogger.getERXLogger(StringHolder.class,"components");
    public String string;
    protected boolean isDocumentation;

    public StringHolder(WOContext context) {
        super(context);
    }

    public void setString(String value) {
        string = value;
    }
    public boolean isDocumentation() {
        return isDocumentation;
    }
    public void setIsDocumentation(boolean value) {
        isDocumentation = value;
    }

}
