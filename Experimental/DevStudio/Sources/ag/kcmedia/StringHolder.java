//
// Sources/ag/kcmedia/StringHolder.java: Class file for WO Component 'StringHolder'
// Project DevStudio
//
// Created by ak on Thu Jul 25 2002
//
package ag.kcmedia;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import er.extensions.*;

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
