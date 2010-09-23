//
// Sources/ag/kcmedia/StringHolder.java: Class file for WO Component 'StringHolder'
// Project DevStudio
//
// Created by ak on Thu Jul 25 2002
//
package ag.kcmedia;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXStatelessComponent;

public class StringHolder extends ERXStatelessComponent {
    static final Logger log = Logger.getLogger(StringHolder.class);
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
