//
// ERD2WDebugFlags.java: Class file for WO Component 'ERD2WDebugFlags'
// Project ERDirectToWeb
//
// Created by patrice on Wed Jul 24 2002
//


package er.directtoweb;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

import er.extensions.ERXExtensions;

///////////////////////////////////////////////////////////////////////////////////////////////////////
// This component can be used in the wrapper of a D2W app to provide convenient development time 
//  (as flagged by WOCachingEnabled) access to
//	the log4j configuration
//	ERD2WDebuggingEnabled
///////////////////////////////////////////////////////////////////////////////////////////////////////


public class ERD2WDebugFlags extends WOComponent {

    public ERD2WDebugFlags(WOContext context) {
        super(context);
    }

    public boolean isStateless() {
        return true;
    }

    public WOComponent toggleD2WInfo() {
        boolean currentState=ERDirectToWeb.d2wDebuggingEnabled(session());
        ERDirectToWeb.setD2wDebuggingEnabled(session(), !currentState);
        return null;
    }

    public WOComponent toggleAdaptorLogging() {
        boolean currentState=ERXExtensions.adaptorLogging();
        ERXExtensions.setAdaptorLogging(!currentState);
        return null;
    }

    public WOComponent clearD2WRuleCache() {
        ERD2WModel.erDefaultModel().clearD2WRuleCache();
        return null;
    }
}
