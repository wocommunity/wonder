//
//  ERXItalianLocalizer.java
//  ERExtensions
//
//  Created by Giorgio Valoti on 25/11/04.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

package er.extensions;

import com.webobjects.foundation.*;
/**
 *  ERXItalianLocalizer is a subclass of {@link ERXLocalizer}.<br/>
 *  <br/>
 *  Overrides <code>plurifiedString</code> from its super class 
 *  and tries to pluralize the string according to italian grammar rules.
 *  It just define a default behavior, but you can provide your own plural strings
 *  by using a dict entry
 *  {
 *   localizerExceptions = {"Foo"=>"Foos"...};
 *  }
 * in your Localizable.strings.
 */
public class ERXItalianLocalizer extends ERXLocalizer {
    static final ERXLogger log = ERXLogger.getERXLogger(ERXItalianLocalizer.class);
    
    public ERXItalianLocalizer(String aLanguage) { 
        super(aLanguage);
    }
    
    public String plurifiedString(String name, int count) {
        if(!language.equals("Italian"))
            return super.plurifiedString(name,count);
        String result = name;
        if(name != null && count > 1) {
            String exception = (String) valueForKeyPath("localizerExceptions." + name);
            if(exception != null) 
                return exception;
            if(result.matches("^.+cie$"))
                return result;
            if(result.matches("^.+[^aeiou][gc]o$")) {
                result = result.substring(0, result.length()-1)+"hi";
            }            
            result = result.substring(0, result.length()-1)+"i";
            if(result.endsWith("ii")) {
                result = result.substring(0, result.length()-1);
            }
        }
        return result;
    }
    
    public String toString() { return "<ERXItalianLocalizer "+language+">"; }
    
}
