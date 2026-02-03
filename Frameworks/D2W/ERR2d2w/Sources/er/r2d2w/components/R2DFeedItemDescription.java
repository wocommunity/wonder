package er.r2d2w.components;

import com.webobjects.appserver.WOContext;

import er.directtoweb.components.ERD2WStatelessComponent;
import er.extensions.foundation.ERXValueUtilities;

public class R2DFeedItemDescription extends ERD2WStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public R2DFeedItemDescription(WOContext context) {
        super(context);
    }
    
    public String stringValue() {
    	String value = (String)objectPropertyValue();
    	boolean escapeHTML = ERXValueUtilities.booleanValueWithDefault(d2wContext().valueForKey("escapeHTML"), true);
    	if(!escapeHTML) {
    		//TODO Use this when CData patch is accepted
    		//value = ERXStringUtilities.escapePCData(value);
    		value = escapePCData(value);
    	}
    	return value;
    }
    
    /**
     * Escapes the given PCDATA string as CDATA.
     * @param pcdata The string to escape
     * @return the escaped string
     */
    public static String escapePCData(String pcdata) {
    	if(pcdata == null) { return null; }
    	
    	int start = 0;
    	int end = 0;
    	String close = "]]>";
    	String escape = "]]]]><![CDATA[>";

    	StringBuffer sb = new StringBuffer("<![CDATA[");
    	
    	do {
        	end = pcdata.indexOf(close, start);
    		sb.append(pcdata.substring(start, (end==-1?pcdata.length():end)));
    		if(end != -1) { sb.append(escape); }
    		start = end;
    		start += 3;
    	} while (end != -1);
    	
    	sb.append(close);
    	
    	return sb.toString();
    }

}