
package er.reporting.examples;

import java.util.Date;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

public class Report extends WOComponent  {

    public Report(WOContext c) {
        super(c);
    }


    public WOComponent back() {
        WOComponent np = (WOComponent)this.session().objectForKey("Main");
        return np;
    }

    public void appendToResponse(WOResponse r, WOContext c){
        Date ts1 = new Date();
        long ts1L = ts1.getTime();
        //System.out.println("ts1L: "+ts1L);
        super.appendToResponse(r, c);
        Date ts2 = new Date();
        long ts2L = ts2.getTime();
        //System.out.println("ts2L: "+ts1L);
        long delta = ts2L - ts1L;
        System.out.println("delta WRReport rendering only: "+(double)delta/(double)1000.0);
    }


    public WOComponent refresh()
    {
        return null;
    }

}