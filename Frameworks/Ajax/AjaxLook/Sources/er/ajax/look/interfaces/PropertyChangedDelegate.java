package er.ajax.look.interfaces;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.foundation.NSArray;

public interface PropertyChangedDelegate {
	/**
	 * 
	 * @param context The d2wContext of the changed property level component
	 */
	public NSArray<String> propertyChanged(D2WContext context);
}
