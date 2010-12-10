package er.modern.look.components;

import com.webobjects.appserver.WOContext;

/**
 * Wrapper component for Sections. Determines if the section will be wrapped with a div (inspect)
 * or fieldset (edit, query, create, etc).
 * 
 * @author davidleber
 *
 */
public class ERMODSection extends ERMODComponent {
	
	private String _sectionElementName;
	
    public ERMODSection(WOContext context) {
        super(context);
    }

	public String sectionElementName() {
		if (_sectionElementName == null) {
			_sectionElementName = "insepect".equals(d2wContext().task()) ? "div" : "fieldset";
		}
		return _sectionElementName;
	}
    
}