package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.directtoweb.*;

/** Extended {@see ListPageInterface} so we can get at the displayGroup. */
public interface ERDListPageInterface extends ListPageInterface {

    /**
     * Returns the displayGroup for this list page.
     * @returns the displayGroup for this list page
     */
    public WODisplayGroup displayGroup();
    
}
