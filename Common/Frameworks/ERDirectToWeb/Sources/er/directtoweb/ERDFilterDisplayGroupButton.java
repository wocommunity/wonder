/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.directtoweb.*;
import org.apache.log4j.Category;

public class ERDFilterDisplayGroupButton extends WOComponent {

    public ERDFilterDisplayGroupButton(WOContext context) { super(context); }

    public static final Category cat = Category.getInstance("er.directtoweb.components.ERDFilterDisplayGroupButton");

    public boolean isStateless() { return true; }
    public D2WContext d2wContext() { return (D2WContext)valueForBinding("d2wContext"); }
    
    public WODisplayGroup displayGroup() { return (WODisplayGroup)valueForBinding("displayGroup"); }

    public static class _FilterDelegate implements NextPageDelegate {
        private WOComponent _nextPage;
        private WODisplayGroup _displayGroup;
        public _FilterDelegate(WOComponent nextPage,
                               WODisplayGroup displayGroup) {
            _nextPage=nextPage;
            _displayGroup=displayGroup;
        }
        public WOComponent nextPage(WOComponent sender) {
            WOComponent result=_nextPage;
            QueryPageInterface qpi=(QueryPageInterface)sender;
            EODataSource eds=qpi.queryDataSource();
            if (eds!=null) {
                if (eds instanceof EODatabaseDataSource) {
                    EODatabaseDataSource dbds=(EODatabaseDataSource)eds;
                    EOQualifier q=dbds.auxiliaryQualifier();
                    cat.debug("Setting qualifier to "+q);
                    _displayGroup.setQualifier(q);
                    _displayGroup.updateDisplayedObjects();
                } else {
                    cat.warn("Data source of unknown type: "+eds.getClass().getName());
                }
            }
            return _nextPage;
        }
    }

    public WOComponent filter() {
        String pageConfigurationForFiltering=(String)d2wContext().valueForKey("pageConfigurationForFiltering");
        QueryPageInterface qpi=pageConfigurationForFiltering!=null ?
            (QueryPageInterface)D2W.factory().pageForConfigurationNamed(pageConfigurationForFiltering, session()) :
            D2W.factory().queryPageForEntityNamed(d2wContext().entity().name(), session());
        qpi.setNextPageDelegate(new _FilterDelegate(context().page(), displayGroup()));
        return (WOComponent)qpi;
    }

    public WOComponent clearFilter(){
        displayGroup().setQualifier(null);
        displayGroup().updateDisplayedObjects();
        return null;
    }
}
