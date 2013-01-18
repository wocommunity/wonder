/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.embed;

import java.io.Serializable;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WEmbeddedComponent;
import com.webobjects.directtoweb.D2WSwitchComponent;
import com.webobjects.directtoweb.NextPageDelegate;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.directtoweb.delegates.ERDBranchDelegate;
import er.extensions.eof.ERXEOControlUtilities;

public class D2WPick extends D2WEmbeddedComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

public D2WPick(WOContext context) { super(context); }
    
    static class _D2WPickActionDelegate implements NextPageDelegate, Serializable {
    	/**
    	 * Do I need to update serialVersionUID?
    	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
    	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
    	 */
    	private static final long serialVersionUID = 1L;

        public static NextPageDelegate instance=new _D2WPickActionDelegate ();
        public WOComponent nextPage(WOComponent sender) {
            WOComponent target = D2WEmbeddedComponent.findTarget(sender);
            WOComponent nextPage = null;
            if (target.hasBinding("branchDelegate")) {
                ERDBranchDelegate delegate = (ERDBranchDelegate)target.valueForBinding("branchDelegate");
                if (delegate == null) {
                    throw new RuntimeException("Null branch delegate. Sender: " + sender + " Target: " + target);
                } else {
                    nextPage = delegate.nextPage(sender);
                }
            } else {
                if (target.hasBinding("selectedObjects") && target.canSetValueForBinding("selectedObjects")) {
                    target.setValueForBinding(sender.valueForKey("selectedObjects"),"selectedObjects");
                }
                nextPage = (WOComponent)target.valueForBinding("action");
            }
            //Here we refresh the list of selectedObjects after hitting any of the buttons on the PickList.
            sender.takeValueForKey(new NSMutableArray(), "selectedObjects");
            return nextPage;
        }
//        public EODataSource dataSource() {
//            return dataSource();
//        }
    }

    static  {
        try {
            D2WSwitchComponent.addToPossibleBindings("selectedObjects"); // Used by D2WPick
        } catch (ExceptionInInitializerError e) {
            Throwable e2=e.getException();
            e2.printStackTrace();
        }
    }

    // Need to do this so that the action binding is not mandatory
    @Override
    public NextPageDelegate actionPageDelegate() { return _D2WPickActionDelegate.instance; }
    @Override
    public NextPageDelegate newPageDelegate() { return _D2WPickActionDelegate.instance; }
    
/*    public EODataSource dataSource() {
        EODataSource ds = null;
        if (hasBinding("list")) {
            NSArray list = (NSArray)valueForBinding("list");
            ds =  er.extensions.ERXExtensions.dataSourceForArray(list);
        } else if (hasBinding("dataSource")) {
            ds = (EODataSource)valueForBinding("dataSource");
        }
        return ds;
    }*/

    public EODataSource internalDataSource() {
        EODataSource ds = dataSource();
        ds = (ds == null) ? ERXEOControlUtilities.dataSourceForArray(list()) : ds;
        return ds;
    }
    public void setInternalDataSource(Object foo) { /* do nothing you silly D2WPick! */ }

    public NSArray list() {
        return (hasBinding("list") ? (NSArray)valueForBinding("list") : null);
    }

    public EODataSource dataSource() {
        return hasBinding("dataSource") ? (EODataSource)valueForBinding("dataSource") : null;
    }
}

