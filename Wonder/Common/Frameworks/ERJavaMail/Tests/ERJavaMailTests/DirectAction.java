//
// DirectAction.java
// Project ERJavaMailTests
//
// Created by camille on Thu Jul 04 2002
//

import er.extensions.*;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;

public class DirectAction extends ERXDirectAction {

    public DirectAction(WORequest aRequest) {
        super(aRequest);
    }

    public WOActionResults defaultAction() {
        return pageWithName("Main");
    }

}
