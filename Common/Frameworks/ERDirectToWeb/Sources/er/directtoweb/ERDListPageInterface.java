package er.directtoweb;

import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;

/** Extended {@link ListPageInterface} so we can get at the displayGroup. */
public interface ERDListPageInterface extends ListPageInterface {

    /**
     * Returns the displayGroup for this list page.
     * @return the displayGroup for this list page
     */
    public WODisplayGroup displayGroup();
    
}
