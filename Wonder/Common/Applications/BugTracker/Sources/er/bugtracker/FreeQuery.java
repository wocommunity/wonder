/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.bugtracker;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.directtoweb.*;
import java.util.*;

public class FreeQuery extends WOComponent {

    public FreeQuery(WOContext aContext) {
        super(aContext);
    }

    protected String string;

    public WOComponent find() {
        NSArray a=NSArray.componentsSeparatedByString(string," ");
        NSMutableArray quals=new NSMutableArray();
        for (Enumeration e=a.objectEnumerator(); e.hasMoreElements();) {
            String s=(String)e.nextElement();
            try {
                Integer i=new Integer(s);
                quals.addObject(new EOKeyValueQualifier("id", EOQualifier.QualifierOperatorEqual, i));
                
            } catch (NumberFormatException ex) {}
        }
        EOOrQualifier or=new EOOrQualifier(quals);
        EODatabaseDataSource ds=new EODatabaseDataSource(session().defaultEditingContext(), "Bug");
        EOFetchSpecification fs=new EOFetchSpecification("Bug",or,null);
        ds.setFetchSpecification(fs);
        ListPageInterface lpi=(ListPageInterface)D2W.factory().listPageForEntityNamed("Bug",session());
        lpi.setDataSource(ds);
        lpi.setNextPage(context().page());
        return (WOComponent)lpi;            
    }
}
