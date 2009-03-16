//
// DirectAction.java
// Project ERD2WTemplate
//
// Created by ak on Sun Apr 21 2002
//
package er.excel;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;

import er.extensions.appserver.ERXDirectAction;

public class DirectAction extends ERXDirectAction {
    static Logger log = Logger.getLogger(DirectAction.class);

    public DirectAction(WORequest aRequest) {
        super(aRequest);
    }

    public WOActionResults defaultAction() {
        return pageWithName("Main");
    }

}
