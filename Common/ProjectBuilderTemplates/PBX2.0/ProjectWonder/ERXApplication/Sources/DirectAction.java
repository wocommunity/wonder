//
// DirectAction.java
// Project ÇPROJECTNAMEÈ
//
// Created by ÇUSERNAMEÈ on ÇDATEÈ
//

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import er.extensions.*;

public class DirectAction extends ERXDirectAction {

    public static final ERXLogger log = ERXLogger.getERXLogger(DirectAction.class);

    public DirectAction(WORequest aRequest) {
        super(aRequest);
    }

    public WOActionResults defaultAction() {
        return pageWithName("Main");
    }

}
