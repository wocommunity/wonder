package er.reporting.examples;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

public class Target extends WOComponent {

    protected String topPageName;
    protected String bottomPageName;
    
    public Target(WOContext c){
        super(c);
    }

}
