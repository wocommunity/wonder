package org.svgobjects.examples.ezforms;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.appserver.*;

public class ContactDetails extends WOComponent  {
    public Applicant contact;
    public EOEnterpriseObject spouse;
    
    public ContactDetails(WOContext context) {
        super(context);
    }
}