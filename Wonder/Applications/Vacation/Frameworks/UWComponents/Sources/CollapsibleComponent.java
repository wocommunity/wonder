// This collapsible component is made specifically for use with Forms

package com.uw.shared;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;

public class CollapsibleComponent extends WOComponent {
    public boolean state;
    public boolean initialState;
    public boolean usedInitialState;
    public String title;

    public WOComponent swapState() {
        
        if (state==true) { state = false; }
        else state = true;

        return null;
    }

    public void setTitle(String newTitle) {
        title = newTitle;
    }

    public void setInitialState(boolean newState) {
        if (usedInitialState!=true) { state = newState; usedInitialState=true; }
}

}
