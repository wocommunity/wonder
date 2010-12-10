//
// Session.java
// Project ValidityExample
//
// Created by msacket on Mon Jun 11 2001
//

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;

public class Session extends WOSession {

    public Session() {
        super();
        
        /* ** Put your per-session initialization code here ** */
    }

    public void takeValuesFromRequest(WORequest request, WOContext context) {
        request.setDefaultFormValueEncoding("UTF8");
        super.takeValuesFromRequest(request, context);
    }

    public void appendToResponse(WOResponse response, WOContext context) {
        response.setContentEncoding("UTF8");
        super.appendToResponse(response, context);
        response.setHeader("text/html; charset=UTF-8", "Content-Type");
    }
    
}
