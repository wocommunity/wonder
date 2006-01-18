/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* WOOgnlHTMLTemplateParser.java created by max on Fri 28-Sep-2001 */

package ognl.webobjects;

import java.util.*;

import com.webobjects.appserver.*;
import com.webobjects.appserver._private.*;
import com.webobjects.foundation.*;
//import org.apache.log4j.Logger;

public class WOOgnlHTMLTemplateParser extends WOHTMLTemplateParser {

    //public static final Logger log = Logger.getLogger("ognl.webobjects.WOOgnlHTMLTemplateParser");
    
    public WOOgnlHTMLTemplateParser(String s, String s1, NSArray array) {
        super(s, s1, array);
    }

    public NSDictionary declarations() { return _declarations; }
    public void setDeclarations(NSDictionary value) { _declarations = value; }

    public String declarationString() { return _declarationString; }
    public void setDeclarationString(String value) { _declarationString = value; }
    
    protected void parseDeclarations() throws WODeclarationFormatException {
        if(declarations() == null && declarationString() != null)
            setDeclarations(WODeclarationParser.declarationsWithString(declarationString()));
    }
    
    public WOElement parse() throws WODeclarationFormatException, WOHTMLFormatException, ClassNotFoundException {
        // This needs to be done before the call to super or 
        parseDeclarations();
        for (Enumeration e = declarations().objectEnumerator(); e.hasMoreElements();) {
            WODeclaration declaration = (WODeclaration)e.nextElement();
            // This will replace constant associations with ognl associations when needed.
            WOOgnl.factory().convertOgnlConstantAssociations((NSMutableDictionary)declaration.associations());
        }
        return super.parse();
    }
}
