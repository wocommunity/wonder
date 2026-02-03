package er.r2d2w.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WDisplayString;

import er.extensions.localization.ERXLocalizer;

public class R2D2WDisplayLanguage extends D2WDisplayString {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public R2D2WDisplayLanguage(WOContext context) {
        super(context);
    }

	public Boolean hasWrapper() {
		Boolean b = Boolean.FALSE;
		if(objectPropertyValueIsNonNull()) {
			Object val = d2wContext().valueForKey("wrapperElement");
			b = (val == null || val.toString().equals("")?Boolean.FALSE:Boolean.TRUE);
		}
		return b;
	}

	public String languageDisplayName() {
		Object o = super.objectPropertyValue();
		return ERXLocalizer.currentLocalizer().localizedStringForKey((String)o);
	}
}