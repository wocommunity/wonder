package wowodc.background.components;

import wowodc.background.utilities.Utilities;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.components.ERXStatelessComponent;

public class PageWrapper extends ERXStatelessComponent {
    public PageWrapper(WOContext context) {
        super(context);
    }

	public boolean hasErrors() {
		return Utilities.hasErrors();
	}
	
	/**
	 * Why do it this way instead of a repetition? Just for fun....
	 * 
	 * @return the li html elements with the error messages
	 */
	public String errorString() {
		NSMutableArray<String> errorMessages = Utilities.errorMessages();
		StringBuilder b = new StringBuilder();
		if (errorMessages.count() > 0) {
			for (String message : errorMessages) {
				b.append("<li class=\"error\" >");
				b.append(message);
				b.append("</li>");
			}
		}
		return b.toString();
	}
}
