package er.directtoweb.interfaces;

import com.webobjects.appserver.WOComponent;
import com.webobjects.eocontrol.EOEnterpriseObject;


/**
 * this defines a mechanism to bridge editObjectAction() in ERD2WListPage to
 * render custom edit pages
 * @author santoash
 *
 */
public interface ERDEditObjectDelegate {
    /**
     * this method will be called from {@link er.directtoweb.pages.ERD2WListPage#editObjectAction()}
     * and the implemetation of this method should return a edit page 
     * for the object thats passed in.
     * @param object - object to be edited
     * @param nextPage - page to return to after editing
     * @return Edit page for the object thats passed in
     */
    public WOComponent editObject(EOEnterpriseObject object, WOComponent nextPage); 
}
