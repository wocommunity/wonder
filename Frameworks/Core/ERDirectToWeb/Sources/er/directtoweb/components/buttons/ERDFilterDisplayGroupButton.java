/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.buttons;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.NextPageDelegate;
import com.webobjects.directtoweb.QueryPageInterface;
import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOQualifier;

import er.directtoweb.components.ERDCustomQueryComponent;
import er.extensions.appserver.ERXDisplayGroup;

/**
 * Cool component that can be used in D2W list pages to filter the list, throwing to a D2W query page to restrict.
 * 
 * @binding d2wContext
 * @binding displayGroup
 */

public class ERDFilterDisplayGroupButton extends ERDCustomQueryComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERDFilterDisplayGroupButton(WOContext context) { super(context); }

    public static final Logger log = LoggerFactory.getLogger(ERDFilterDisplayGroupButton.class);

    @Override
    public boolean isStateless() { return true; }
    @Override
    public boolean synchronizesVariablesWithBindings() { return false; }

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
                    EOQualifier q=dbds.fetchSpecification().qualifier();
                    log.debug("Setting qualifier to "+q);
                    _displayGroup.setQualifier(q);
                    _displayGroup.updateDisplayedObjects();
                } else {
                    log.warn("Data source of unknown type: "+eds.getClass().getName());
                }
            }
            return result;
        }
    }

    public WOComponent filter() {
        String pageConfigurationForFiltering=(String)valueForBinding("pageConfigurationForFiltering");
        QueryPageInterface qpi;
        if(pageConfigurationForFiltering!=null) {
            qpi = (QueryPageInterface)D2W.factory().pageForConfigurationNamed(pageConfigurationForFiltering, session());
        } else {
            String entityName = (String)valueForBinding("entityName");
            if(entityName == null && d2wContext() != null) {
                entityName = d2wContext().entity().name();
            }
            if(entityName == null)
                throw new IllegalStateException("entityName or d2wContext can't both be null.");
            qpi = D2W.factory().queryPageForEntityNamed(entityName, session());
        }
        qpi.setNextPageDelegate(new _FilterDelegate(context().page(), displayGroup()));
        D2WContext d2wContext = (D2WContext)((WOComponent)qpi).valueForKey("d2wContext");
        d2wContext.takeValueForKey("filter", "subTask");
        return (WOComponent)qpi;
    }

    public WOComponent clearFilter(){
        displayGroup().setQualifier(null);
        displayGroup().updateDisplayedObjects();
        
        if (displayGroup() instanceof ERXDisplayGroup) {
            ERXDisplayGroup dg = (ERXDisplayGroup) displayGroup();
            dg.clearExtraQualifiers();
        }

        return null;
    }
}
