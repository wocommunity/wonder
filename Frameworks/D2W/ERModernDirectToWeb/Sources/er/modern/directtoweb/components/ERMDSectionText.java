package er.modern.directtoweb.components;

import com.webobjects.appserver.WOContext;

import er.directtoweb.components.misc.ERDSectionText;

/**
 * Display component for the section heading
 * 
 * @d2wKey displayNameForSectionKey
 * @d2wKey sectionElementName
 * 
 * @author davidleber
 *
 */
public class ERMDSectionText extends ERDSectionText {
	
	public static interface Keys {
		 public static final String sectionElementName = "sectionElementName";
	}
	
    public ERMDSectionText(WOContext context) {
        super(context);
    }
    
    public String sectionElementName() {
    	String value = (String)valueForBinding(Keys.sectionElementName);
    	if (value == null || value.length() == 0) {
    		value = "span";
    	}
    	return null;
    }
    
}