package er.prototaculous.widgets;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;

import er.extensions.appserver.ERXResponseRewriter;
import er.extensions.foundation.ERXProperties;

/**
 * WO wrapper around Rails (Prototype) date picker
 * 
 * NOTE: that dateformats must have compatible client side scripts
 * So optionally you may produce your own variants of the client-side date format javascripts and set the properties to use them
 * 
 * @see "http://code.google.com/p/calendardateselect/"
 *
 * @author mendis
 * 
 * @property er.prototaculous.AjaxCalendarDateSelect.DateFormats.natural 	Default date format
 * @property er.prototaculous.AjaxCalendarDateSelect.DateFormats.american 	US date format
 * @property er.prototaculous.AjaxCalendarDateSelect.DateFormats.euro24hYmd		EU date format
 * 
 * @property er.prototaculous.AjaxCalendarDateSelect.Scripts.american	Script file for US date format
 * @property er.prototaculous.AjaxCalendarDateSelect.Scripts.euro24hYmd		Script file for EU date format
 * 
 * @property er.prototaculous.useUnobtrusively	If you want the component to include its JavaScripts and CSS set to false. (This is the default).
 * 												This is to support Unobtrusive Javascript programming.
 * @property er.ajax.AjaxCalendarDateSelect.DateFormats.natural
 * @property er.ajax.AjaxCalendarDateSelect.DateFormats.american
 * @property er.ajax.AjaxCalendarDateSelect.DateFormats.euro24hYmd
 *
 * @binding value
 * @binding id
 * @binding name
 * @binding dateFormat
 * @binding size
 */
public class CalendarDateSelect extends WOComponent {
	private static boolean useUnobtrusively = ERXProperties.booleanForKeyWithDefault("er.prototaculous.useUnobtrusively", false);
	
	/*
	 * WO date formats 
	 */
	public static interface DateFormats {
		public static String natural = ERXProperties.stringForKeyWithDefault("er.ajax.AjaxCalendarDateSelect.DateFormats.natural", "%B %d, %Y");
		public static String american = ERXProperties.stringForKeyWithDefault("er.ajax.AjaxCalendarDateSelect.DateFormats.american", "%m/%d/%Y");		
		public static String euro24hYmd = ERXProperties.stringForKeyWithDefault("er.ajax.AjaxCalendarDateSelect.DateFormats.euro24hYmd", "%Y.%m.%d");		
	}
	
	/*
	 * Corresponding (client-side) date format javascripts
	 */
	private static interface Scripts {
		public static Object natural = NSKeyValueCoding.NullValue;
		public static String american = ERXProperties.stringForKeyWithDefault("er.ajax.AjaxCalendarDateSelect.Scripts.american", "format_american.js");		
		public static String euro24hYmd = ERXProperties.stringForKeyWithDefault("er.ajax.AjaxCalendarDateSelect.Scripts.euro24hYmd", "format_euro_24hr_ymd.js");		
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
		// include javascripts if not being used unobtrusively
		if (!useUnobtrusively) {
			ERXResponseRewriter.addStylesheetResourceInHead(response, context, "ERPrototaculous", "CalendarDateSelect.css");
			ERXResponseRewriter.addScriptResourceInHead(response, context, "Ajax", "prototype.js");
			ERXResponseRewriter.addScriptResourceInHead(response, context, "ERPrototaculous", "calendar_date_select.js");

			// date format script
			if (!dateFormatScript().equals(NSKeyValueCoding.NullValue)) ERXResponseRewriter.addScriptResourceInHead(response, context, "ERPrototaculous", (String) dateFormatScript());	
		} super.appendToResponse(response, context);
	}
}
