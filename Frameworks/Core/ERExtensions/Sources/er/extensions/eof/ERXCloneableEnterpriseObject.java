//
// ERXCloneableEnterpriseObject.java
// Project ERExtensions
//
// Created by max on Fri Oct 04 2002
//
package er.extensions.eof;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;

/**
 * Interface for cloning enterprise objects.
 */
public interface ERXCloneableEnterpriseObject extends EOEnterpriseObject {

    /**
     * Clones a given enterprise object into the specified editing context.
     * @param ec editing context to clone into
     * @return clone of the enterprise object
     */
    public ERXCloneableEnterpriseObject clone(EOEditingContext ec);
    
}
