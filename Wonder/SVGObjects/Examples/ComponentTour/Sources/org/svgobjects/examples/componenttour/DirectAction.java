package org.svgobjects.examples.componenttour;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.appserver.*;

public class DirectAction extends WODirectAction  {

    public DirectAction(WORequest aRequest) {
        super(aRequest);
    }
    /*
    * direct actions
    */
    public WOActionResults defaultAction() {
        return this.pageWithName("Main");
    }
    
    public WOActionResults navigationAction() {
        return pageWithName("MainNavigation");
    }

    public WOActionResults imageAction() {
        return pageWithName("EmbossImage");
    }

    public WOActionResults animateAction() {
        return pageWithName("FilterPattern");
    }

    public WOActionResults circleAction() {
        return pageWithName("Circles");
    }

    public WOActionResults xmllinkAction() {
        return pageWithName("Hyperlink");
    }
}