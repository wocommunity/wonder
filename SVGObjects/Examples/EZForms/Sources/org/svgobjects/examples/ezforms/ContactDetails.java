package org.svgobjects.examples.ezforms;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.appserver.*;

public class ContactDetails extends WOComponent  {
    protected Applicant contact;
    protected EOEnterpriseObject spouse;
    
    public ContactDetails(WOContext context) {
        super(context);
    }
}