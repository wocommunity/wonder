package er.prototaculous.widgets;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;

import er.ajax.AjaxUtils;
import er.extensions.foundation.ERXProperties;

/**
 * WO wrapper around Rails (Prototype) date picker
 * http://code.google.com/p/calendardateselect/
 * 
 * NOTE: that dateformats must have compatible client side scripts
 * So optionally you may produce your own variants of the client-side date format javascripts and set the properties to use them
 * 
 * @author mendis
 * 
 * @property er.prototaculous.CalendarDateSelect.DateFormats.natural 	Default date format
 * @property er.prototaculous.CalendarDateSelect.DateFormats.american 	US date format
 * @property er.prototaculous.CalendarDateSelect.DateFormats.euro24hYmd		EU date format
 * 
 * @property er.prototaculous.CalendarDateSelect.Scripts.american	Script file for US date format
 * @property er.prototaculous.CalendarDateSelect.Scripts.euro24hYmd		Script file for EU date format
 * 
 * @property er.prototculous.useUnobtrusively	If you want the component to include its JavaScripts and CSS set to false. (This is the default).
 * 												This is to support Unobtrusive Javascript programming.
 *
 */
public class CalendarDateSelect extends WOComponent {
	private static boolean useUnobtrusively = ERXProperties.booleanForKeyWithDefault("er.prototculous.useUnobtrusively", false);
	
	/*
	 * WO date formats 
	 */
	public static interface DateFormats {
		public static String natural = ERXProperties.stringForKeyWithDefault("er.ajax.CalendarDateSelect.DateFormats.natural", "%B %d, %Y");
		public static String american = ERXProperties.stringForKeyWithDefault("er.ajax.CalendarDateSelect.DateFormats.american", "%m/%d/%Y");		
		public static String euro24hYmd = ERXProperties.stringForKeyWithDefault("er.ajax.CalendarDateSelect.DateFormats.euro24hYmd", "%Y.%m.%d");		
	}
	
	/*
	 * Corresponding (client-side) date format javascripts
	 */
	private static interface Scripts {
		public static Object natural = NSKeyValueCoding.NullValue;
		public static String american = ERXProperties.stringForKeyWithDefault("er.ajax.CalendarDateSelect.Scripts.american", "format_american.js");		
		public static String euro24hYmd = ERXProperties.stringForKeyWithDefault("er.ajax.CalendarDateSelect.Scripts.euro24hYmd", "format_euro_24hr_ymd.js");		
	}
	
	/*
	 * Bindings or API of component
	 */
	public static interface Bindings {
    	public static final String dateFormat = "dateFormat";
	}
	
	public static NSArray<String> dateFormats = new NSArray<String>(new String[]{DateFormats.natural, DateFormats.american, DateFormats.euro24hYmd});
	private static NSArray<Object> scripts = new NSArray<Object>(new Object[]{Scripts.natural, Scripts.american, Scripts.euro24hYmd});
	private static NSDictionary<String, Object> _scriptsDict = new NSDictionary<String, Object>(scripts, dateFormats);
		
    public CalendarDateSelect(WOContext context) {
        super(context);
    }
    
    @Override
    public boolean synchronizesVariablesWithBindings() {
    	return false;
    }
    
    public boolean isStateless() {
    	return true;
    }
    
    // accessors
    public String onClick() {
    	return "new CalendarDateSelect( this, {year_range:10} );";
    }
    
    public String dateFormat() {
    	if (hasBinding(Bindings.dateFormat)) {
    		String dateFormat = (String) valueForBinding(Bindings.dateFormat);
    		if (dateFormats.contains(dateFormat)) return dateFormat;
    	} return DateFormats.natural;
    }
    
    public Object dateFormatScript() {
    	return _scriptsDict.objectForKey(dateFormat());
    }
	
	// R/R
	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		AjaxUtils.addStylesheetResourceInHead(context, response, "WO2", "CalendarDateSelect.css");

		// include javascripts if not being used unobtrusively
		if (!useUnobtrusively) {
			AjaxUtils.addScriptResourceInHead(context, response, "prototype.js");
			AjaxUtils.addScriptResourceInHead(context, response, "WO2", "calendar_date_select.js");

			// date format script
			if (!dateFormatScript().equals(NSKeyValueCoding.NullValue)) AjaxUtils.addScriptResourceInHead(context, response, "WO2", (String) dateFormatScript());	
		} super.appendToResponse(response, context);
	}
}