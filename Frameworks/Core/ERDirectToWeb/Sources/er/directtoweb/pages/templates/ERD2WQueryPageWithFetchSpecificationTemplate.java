//
// ERD2WQueryPageWithFetchSpecificationTemplate.java: Class file for WO Component 'ERD2WQueryPageWithFetchSpecificationTemplate'
// Project ERDirectToWeb
//
// Created by ak on Thu Apr 18 2002
//
package er.directtoweb.pages.templates;
import com.webobjects.appserver.WOContext;

import er.directtoweb.pages.ERD2WQueryPage;

/**
 * @d2wKey entity
 * @d2wKey pageWrapperName
 * @d2wKey border
 * @d2wKey backgroundColorForTable
 * @d2wKey componentName
 * @d2wKey propertyKey
 * @d2wKey findButtonLabel
 */
public class ERD2WQueryPageWithFetchSpecificationTemplate extends ERD2WQueryPage {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERD2WQueryPageWithFetchSpecificationTemplate(WOContext context) {
        super(context);
    }

}
