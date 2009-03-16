//
//  ERXItalianLocalizer.java
//  ERExtensions
//
//  Created by Giorgio Valoti on 25/11/04.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

package er.extensions.localization;

import org.apache.log4j.Logger;

/**
 *  ERXItalianLocalizer is a subclass of {@link ERXLocalizer}.<br/>
 *  <br/>
 *  Overrides <code>plurify</code> from its super class 
 *  and tries to pluralize the string according to italian grammar rules.
 */
public class ERXItalianLocalizer extends ERXLocalizer {
    static final Logger log = Logger.getLogger(ERXItalianLocalizer.class);
    
    public ERXItalianLocalizer(String aLanguage) { 
        super(aLanguage);
    }
    
    protected String plurify(String name, int count) {
        String result = name;
        if(name != null && count > 1) {
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
}
