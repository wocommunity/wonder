/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.appserver.*;

/**
 * Given a count and a string pluralizes the string if count &gt; 1.<br />
 * 
 * @binding value
 * @binding count
 * @binding showNumber" defaults="Boolean
 */

public class ERXPluralString extends WOComponent {

    public ERXPluralString(WOContext aContext) {
        super(aContext);
    }

    public boolean isStateless() { return true; }
    public boolean synchronizesVariablesWithBindings() { return false; }

    public String value() {
        Number c=(Number)valueForBinding("count");
        return ERXLocalizer.localizerForSession(session()).plurifiedString((String)valueForBinding("value"), c!=null ? c.intValue() : 0);
    }
/*
    public static String plurify(String s, int howMany) {
        String result=s;
        if (s!=null && howMany!=1) {
            if (s.endsWith("y"))
                result=s.substring(0,s.length()-1)+"ies";
            else if (s.endsWith("s") && ! s.endsWith("ss")) {
                // we assume it's already plural. There are a few words this will break this heuristic
                // e.g. gas --> gases
                // but otherwise for Documents we get Documentses..
            } else if (s.endsWith("s") || s.endsWith("ch") || s.endsWith("sh") || s.endsWith("x"))
                result+="es";
            else
                result+= "s";
        }
        return result;
    }

    public static String singularify(String value) {
        String result = value;
        if (value!=null) {
            if (value.endsWith("ies"))
                result = value.substring(0,value.length()-3)+"y";
            else if (value.endsWith("hes"))
                result = value.substring(0,value.length()-2);
            else if (!value.endsWith("ss") && (value.endsWith("s") || value.endsWith("ses")))
                result = value.substring(0,value.length()-1);
        }
        return result;  
    }
*/
    
    public boolean showNumber() {
        return hasBinding("showNumber") ? ERXValueUtilities.booleanValue(valueForBinding("showNumber")) : true;
        //boolean result=true;
        //Integer showNumber=(Integer);
        //if (showNumber!=null && showNumber.intValue()==0)
        //    result=false;
        //return result;
    }
}
