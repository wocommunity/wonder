package org.svgobjects.examples.componenttour;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.appserver.*;

public class Hyperlink extends WOComponent  {
    
    public Hyperlink(WOContext context) {
        super(context);
    }
    
    /*
    * request/response
    */
    public void appendToResponse(WOResponse response, WOContext context) {
        super.appendToResponse(response, context);
        response.setHeader("image/svg-xml", "Content-Type");
    }
}