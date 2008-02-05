//
// ERDQueryPageInterface.java
// Project ERDirectToWeb
//
// Created by bposokho on Fri Oct 18 2002
//
package er.directtoweb;

import com.webobjects.directtoweb.NextPageDelegate;
import com.webobjects.directtoweb.QueryPageInterface;
import com.webobjects.eocontrol.EODataSource;

public interface ERDQueryPageInterface extends QueryPageInterface {

    // CHECKME AK: what's this for?
    public void setCancelDelegate(NextPageDelegate cancelDelegate);
    
    /**
     * Preselect a query match value
     * @param value
     * @param selector
     * @param key
     */
    public void setQueryMatchForKey(Object value, String selector, String key);
    
    /**
     * Returns the query datasource. Not having this is annoying...
     */
    public EODataSource queryDataSource();
    
}
