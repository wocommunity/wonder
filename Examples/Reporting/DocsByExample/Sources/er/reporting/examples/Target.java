package er.reporting.examples;

import er.reporting.*;
import er.grouping.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;

public class Target extends WOComponent {

    protected String topPageName;
    protected String bottomPageName;
    
    public Target(WOContext c){
        super(c);
    }

}
