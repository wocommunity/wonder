package er.reporting;

import er.grouping.*;
import java.lang.*;
import java.util.*;
import java.io.*;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import webobjectsexamples.wocomponentelements.*;

/* WRNestedList.h created by Administrator on Sun 29-Nov-1998 */
public class WRNestedList extends WXNestedList  {

    public WRNestedList(WOContext aContext)  {
        super(aContext);
    }


/*

    public int currentLevel() {
        // ** this required by Key Value Coding
    }


    public void setCurrentLevel(int aChildLevel) {
        // ** Whatever the child passes, we add 1.  By the time this gets
        // ** to the root, it reflects the number of levels in between.
        this.setValueForBinding(new Integer(aChildLevel+1), "level");
    }
*/



    public boolean notSublistConditional() {
        return !this.hasBinding("showParentContent");
    }


    public void takeValuesFromRequest(WORequest r, WOContext c) {
        //Abort call to super to save all this processing time
    }


}