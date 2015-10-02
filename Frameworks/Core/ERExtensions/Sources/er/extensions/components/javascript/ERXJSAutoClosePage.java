/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components.javascript;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

/**
 * When returned will close the current page by calling <code>onload=window.close()</code>.
 * Useful for server side determining if a window should be closed.
 */
public class ERXJSAutoClosePage extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private String additionalJavaScript;
	
	
    /** Public constructor */
    public ERXJSAutoClosePage(WOContext aContext) {
        super(aContext);
    }
    
    /** component is stateless */
    @Override
    public boolean isStateless() { return true; }
    
    
    /**
     * @return JavaScript for onLoad function, additionalJavaScript() (if any) followed by "window.close()"
     */
    public String onLoad() {
    	return (additionalJavaScript() != null ? additionalJavaScript() : "") +  " window.close();";
    }
    
    /**
     * Convenience method to set additionalJavaScript() to "opener.location.reload(true);")
     */
    public void refreshOpener() {
    	setAdditionalJavaScript("opener.location.reload(true);");
    }

	/**
	 * @return additionalJavaScript to execute before closing the current window
	 */
	public String additionalJavaScript() {
		return additionalJavaScript;
	}

	/**
	 * @param javaScript the additional JavaScript to execute before closing the current window
	 */
	public void setAdditionalJavaScript(String javaScript) {
		additionalJavaScript = javaScript;
	}
}
