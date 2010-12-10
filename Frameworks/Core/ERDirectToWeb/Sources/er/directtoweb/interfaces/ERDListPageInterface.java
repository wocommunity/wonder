package er.directtoweb.interfaces;

import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.directtoweb.ListPageInterface;

/** Extended {@link ListPageInterface} so we can get at the displayGroup. */
public interface ERDListPageInterface extends ListPageInterface {

    /**
     * Returns the displayGroup for this list page.
     * @return the displayGroup for this list page
     */
    public WODisplayGroup displayGroup();
    
}
