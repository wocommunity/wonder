package er.directtoweb.interfaces;

import com.webobjects.directtoweb.ErrorPageInterface;

/**
 * Extends ErrorPageInterface to provide means to set the actual exception.
 * Not being able to do so makes localization a pain.
 *
 * @created ak on Thu Jan 29 2004
 * @project ERDirectToWeb
 */

public interface ERDErrorPageInterface extends ErrorPageInterface {
    
    /** Sets the exception. */
    public void setException(Exception ex);

}
