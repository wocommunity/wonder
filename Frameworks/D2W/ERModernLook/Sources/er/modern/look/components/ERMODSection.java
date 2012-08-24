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
  /**
   * Do I need to update serialVersionUID?
   * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
   * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
   */
  private static final long serialVersionUID = 1L;

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