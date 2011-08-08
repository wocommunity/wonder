package wowodc.background.components;

import wowodc.background.utilities.Utilities;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.components.ERXComponent;

public class PageWrapper extends ERXComponent {
    public PageWrapper(WOContext context) {
        super(context);
    }
    
    @Override
	public boolean synchronizesVariablesWithBindings() {
		// makes this component non-synchronizing
		return false;
	}

	@Override
	public boolean isStateless() {
		// makes this component stateless
		return true;
	}

	@Override
	public void reset() {
		// resets ivars at the end or RR phases
		super.reset();
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