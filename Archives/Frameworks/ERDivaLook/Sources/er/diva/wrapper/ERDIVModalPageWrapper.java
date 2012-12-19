package er.diva.wrapper;

import com.webobjects.appserver.WOContext;

import er.diva.ERDIVPageInterface;

public class ERDIVModalPageWrapper extends ERDIVPageWrapper {
    public ERDIVModalPageWrapper(WOContext context) {
        super(context);
    }
    
    // accessors
    public String stylesheet() {
    	return (String) d2wContext().valueForKey(ERDIVPageInterface.Keys.Stylesheet);
    }
}