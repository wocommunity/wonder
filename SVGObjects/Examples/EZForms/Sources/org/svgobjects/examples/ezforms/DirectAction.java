package org.svgobjects.examples.ezforms;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;

public class DirectAction extends WODirectAction  {

    /*
    * constructor
    */
    public DirectAction(WORequest aRequest) {
        super(aRequest);
    }

    /*
    * actions
    */
    public WOActionResults defaultAction() {
        return pageWithName("Main");
    }
}