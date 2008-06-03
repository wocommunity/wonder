//
// DirectAction.java
// Project PopIdol1B
//
// Created by ravi on Mon Mar 18 2002
//
package org.svgobjects.examples.popidol;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;

public class DirectAction extends WODirectAction {

    public DirectAction(WORequest aRequest) {
        super(aRequest);
    }

    public WOActionResults defaultAction() {
        return pageWithName("Main");
    }

}
