package er.ajax;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSTimestampFormatter;

import er.extensions.appserver.ERXResponseRewriter;
import er.extensions.formatters.ERXJodaFormat;
import er.extensions.localization.ERXLocalizer;

/**
 * Shameless port and adoption of Rails Date Kit.  This input understands the format symbols
 * %A, %d, %e, %b, %m, %B, %y, and %Y. See the NSTimestampFormatter for 
 * what these symbols do. This component can also understand the corresponding symbols from 
 * java.text.SimpleDateFormat.  The translation from SimpleDateFormat symbols to NSTimestampFormatter
 * symbols may have some defects.
 * <p>Only one of format or formatter may be bound, if both are unbound the default of %m %d %Y is used.
 * If format is bound, the pattern is used to create an internal formatter for validation.  If formatter
 * is bound, its pattern is extracted and used in place of format. The format/formatter is used to control
 * the initial display in the input, the format of the value that the date picker places into the input, and 
 * validation of the input contents on form submission. The use of formatter over format is
 * preferred for reasons of efficiency and localization.</p>
 * <p>FL: The component uses the default Locale to determine the start day of the week. It also uses the current
 * language in ERXLocalizer to translate the day and month names (you must set up the localizations).</p>
 * 
 * <p><b>NOTE</b>: the AjaxDatePicker does <b>NOT</b> play nice with the AjaxModalDialogOpener.  There is some sort of 
 * initialization conflict (I think) with Prototype that leaves you with a blank page and the browser waiting
 * forever for something (and I have not been able to determine what it is) as soon as calendar.js loads and
 * initialized.  It will work if the page the AMD appears on explicitly loads the calendar.js in it's HEAD:</p>
 * <pre>
 *  public void appendToResponse(WOResponse response, WOContext context) {
 *       super.appendToResponse(response, context);
 *       ERXResponseRewriter.addScriptResourceInHead(response, context(), "Ajax", "calendar.js");
 *   }
 * </pre>
 * 
 * @binding value the value that will be shown in the input field and set by the date picker (required)
 * @binding format the format to use in the input field (only one of format or formatter may be bound)
 * @binding formatter the formatter to use with the input field (only one of format or formatter may be bound)
 *
 * @binding id HTML ID passed to the input field 
 * @binding class CSS class passed to the input field 
 * @binding style CSS style passed to the input field 
 * @binding size size attribute passed to the input field 
 * @binding maxlength maxlength attribute passed to the input field 
 * @binding name name attribute passed to the input field
 * @binding disabled passed to the input field
 * @binding onDateSelect JavaScript to execute when a date is selected from the calendar
 * @binding fireEvent false if the onChange event for the input should NOT be fired when a date is selected in the calendar, defaults to true
 * 
 * @binding startDay specify the first day of week to use 0(Sunday)-6(Saturday). The default use the current localizer.
 * @binding dayNames list of day names (Sunday to Saturday) for localization, English is the default
 * @binding monthNames list of month names for localization, English is the default
 * @binding imagesDir directory to take images from, takes them from Ajax.framework by default
 * @binding locale FL: locale can be set if ERXLocalizer returns the wrong one. IE the English localizer returns a US Locale. If you want the UK one then set this binding.
 * @binding showYearControls: display the prev and next year controls. Default to true.
 * 
 * @binding calendarCSS name of CSS resource with classed for calendar, defaults to "calendar.css"
 * @binding calendarCSSFramework name of framework (null for application) containing calendarCSS resource, defaults to "Ajax"
 *
 * @see java.text.SimpleDateFormat
 * @see com.webobjects.foundation.NSTimestampFormatter
 * 
 * @see <a href="http://www.methods.co.nz/rails_date_kit/rails_date_kit.html">Rails Date Kit</a>
 *
 * @author ported by Chuck Hill
 */
public class AjaxDatePicker extends AjaxComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    private static final NSArray<String> _dayNames = new NSArray<String>(new String[] {"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"}); 
    private static final NSArray<String> _monthNames = new NSArray<String>(new String[] {"January","February","March","April","May","June","July","August","September","October","November","December"}); 

    private static String defaultImagesDir;
	
	private NSMutableDictionary<String, String> options;
	private Format formatter;
	private String format;
	
    public AjaxDatePicker(WOContext context) {
        super(context);
        
        // I am not expecting the images to get localized, so this can be set once
        // This is hacky, but I wanted to avoid changing the JS to take the path for each image in options
        // and WO does not expose this path any other way.  Still half thinking I should have changed the JS...
        if (defaultImagesDir == null) {
			defaultImagesDir = application().resourceManager().urlForResourceNamed("calendar_prev.png", "Ajax", null, context().request()).toString();
			int lastSeperator = defaultImagesDir.lastIndexOf("%2F");
			if (lastSeperator == -1) {
				lastSeperator = defaultImagesDir.lastIndexOf('/');
			}
			defaultImagesDir = defaultImagesDir.substring(0, lastSeperator);
			
			// Need to pre-populate the cache for WOResourceManager
			application().resourceManager().urlForResourceNamed("calendar_next.png", "Ajax", null, context().request()).toString();
        }
    }
    
    /**
     * @return <code>true</code>
     */
    @Override
    public boolean isStateless() {
    	return true;
    }
    
    /**
     * Sets up format / formatter values.
     */
    @Override
    public void awake() {
		super.awake();

		if ( ! (hasBinding("formatter") || hasBinding("format"))) {
			format = "%m %d %Y";  // Default
			formatter = new NSTimestampFormatter(format);
		}
		else if (hasBinding("formatter")) {
    		formatter = (Format) valueForBinding("formatter");
    		if (formatter instanceof NSTimestampFormatter) {
    			format = translateSimpleDateFormatSymbols(((NSTimestampFormatter)formatter).pattern());
    		}
    		else if (formatter instanceof SimpleDateFormat) {
    			format = ((SimpleDateFormat)formatter).toPattern();
    		}
    		else if (formatter instanceof ERXJodaFormat) {
    			format = ((ERXJodaFormat)formatter).pattern();
    		}
    		else {
    			throw new RuntimeException("Can't handle formatter of class " + formatter.getClass().getCanonicalName());
    		}
    	}
    	else {
    		format = (String) valueForBinding("format");
    		formatter = new NSTimestampFormatter(format);
    	}
		
		format = translateSimpleDateFormatSymbols(format);
    }
    
    /**
     * Clear cached values.
     */
    @Override
    public void reset() {
    	options = null;
    	formatter = null;
    	format = null;
    	super.reset();
    }
    
    public Locale locale() {
    	return (Locale)valueForBinding("locale", ERXLocalizer.currentLocalizer().locale());
    }
    
    public Integer startDay() {
    	// Get first day of week from current localizer Locale.
    	return Integer.valueOf(new GregorianCalendar(locale()).getFirstDayOfWeek() - 1);
    }
    
    private NSArray<String> localizeStringArray(NSArray<String> strings) {
    	NSMutableArray<String> localizedStrings = new NSMutableArray<String>(strings.count());
    	ERXLocalizer l = ERXLocalizer.currentLocalizer();
    	for (String string : strings)
    		localizedStrings.add(l.localizedStringForKeyWithDefault(string));
    	return localizedStrings.immutableClone();
    }

    public NSArray<String> dayNames() {
    	if (hasBinding("dayNames"))
    		return (NSArray<String>)valueForBinding("dayNames");
    	return localizeStringArray(_dayNames);
    }

    public NSArray<String> monthNames() {
    	if (hasBinding("monthNames"))
    		return (NSArray<String>)valueForBinding("monthNames");
    	return localizeStringArray(_monthNames);
    }

    /**
     * Sets up AjaxOptions prior to rendering.
     * 
     * @param res the HTTP response that an application returns to a
     *        Web server to complete a cycle of the request-response loop
     * @param ctx context of a transaction
     */
    @Override
    public void appendToResponse(WOResponse res, WOContext ctx) {
		
		NSMutableArray<AjaxOption> ajaxOptionsArray = new NSMutableArray<AjaxOption>();
		
		// The "constant" form of AjaxOption is used so that we can rename the bindings or convert the values
		ajaxOptionsArray.addObject(new AjaxConstantOption("format", "format", format(), AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("month_names", "monthNames", monthNames(), AjaxOption.ARRAY));
		ajaxOptionsArray.addObject(new AjaxOption("day_names", "dayNames", dayNames(), AjaxOption.ARRAY));
		
		// FL Added to support start day, defaults to 0 (Sunday - choice made in calendar.js).
		ajaxOptionsArray.addObject(new AjaxOption("start_day", "startDay", startDay(), AjaxOption.NUMBER));
		ajaxOptionsArray.addObject(new AjaxOption("showYearControls", "showYearControls", showYearControls(), AjaxOption.BOOLEAN));
		
		ajaxOptionsArray.addObject(new AjaxOption("onDateSelect", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("fireEvent", AjaxOption.BOOLEAN));

		ajaxOptionsArray.addObject(new AjaxOption("images_dir", "imagesDir", defaultImagesDir, AjaxOption.STRING));
		
		options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, this);
    	super.appendToResponse(res, ctx);
    }
    
    private Boolean showYearControls() {
		return Boolean.valueOf(booleanValueForBinding("showYearControls", true));
	}

	/**
     * @return JavaScript for onFocus binding of HTML input
     */
    public String onFocusScript() {
        return showCalendarScript();
    }
    
    /**
     * @return JavaScript for onClick binding of HTML input
     */
    public String onClickScript() {
        	StringBuilder script = new StringBuilder(200);
           	script.append("event.cancelBubble=true; ");
         	script.append(showCalendarScript());
            return script.toString();
    }
    
    /**
     * @return JavaScript to load CSS and show calendar display
     */
    public String showCalendarScript() {
    	StringBuffer script = new StringBuffer(200);
    	// Load the CSS like this to avoid odd race conditions when this is used in an AjaxModalDialog: at times
    	// the CSS does not appear to be available and the calendar appears in the background
    	script.append("AOD.loadCSS('");
    	script.append(application().resourceManager().urlForResourceNamed(cssFileName(), cssFileFrameworkName(), null, context().request()).toString());
    	script.append("'); ");
    	script.append("this.select(); calendar_open(this, ");
    	AjaxOptions.appendToBuffer(options(), script, context());
    	script.append(");");
        return script.toString();
    }

    /**
     * Quick and rude translation of formatting symbols from SimpleDateFormat to the symbols
     * that this component uses.
     *
     * @param symbols the date format symbols to translate
     * @return translated date format symbols 
     */
    public String translateSimpleDateFormatSymbols(String symbols) {
    	// Wildly assume that there is no translation needed if we see a % character
    	if (symbols.indexOf('%') > -1) {
    		return symbols;
    	}
    	
    	StringBuilder sb = new StringBuilder(symbols);
    	replace(sb, "dd", "%~");
    	replace(sb, "d", "%d");
    	replace(sb, "%~", "%d");
    	replace(sb, "MMMM", "%B");
    	replace(sb, "MMM", "%b");
    	replace(sb, "MM", "%m");
    	replace(sb, "M", "%m");
    	replace(sb, "yyyy", "%Y");
    	replace(sb, "yyy", "%~");
    	replace(sb, "yy", "%~");
    	replace(sb, "y", "%y");
    	replace(sb, "%~", "%y");
    	
    	return sb.toString();
    }
    
    /**
     * Helper method for translateSimpleDateFormatSymbols.
     */
    private void replace(StringBuilder builder, String original, String replacement) {
    	int index = builder.indexOf(original);
    	if (index > -1) {
    		builder.replace(index, index + original.length(), replacement);
    	}
    }
    
    /**
     * @return format string used by date picker
     */
    public String format() {
    	return format;
    }
    
    /**
     * @return formatter controlling initial contents of input and validation
     */
    public Format formatter() {
    	return formatter;
    }
    
    /**
     * @return cached Ajax options for date picker JavaScript
     */
    public NSMutableDictionary options() {
    	return options;
    }
    
    /**
     * Includes calendar.css and calendar.js.
     */
	@Override
	protected void addRequiredWebResources(WOResponse response) {
		ERXResponseRewriter.addScriptResourceInHead(response, context(), "Ajax", "prototype.js");
		ERXResponseRewriter.addScriptResourceInHead(response, context(), "Ajax", "wonder.js");
		ERXResponseRewriter.addScriptResourceInHead(response, context(), "Ajax", "calendar.js");
		ERXResponseRewriter.addScriptResourceInHead(response, context(), "Ajax", "date.js");
		ERXResponseRewriter.addStylesheetResourceInHead(response, context(), cssFileFrameworkName(), cssFileName());
	}
	
	/**
	 * No action so nothing for us to handle.
	 */
	@Override
	public WOActionResults handleRequest(WORequest request, WOContext context) {
		return null;
	}
    	
    /**
     * Overridden so that parent will handle in the same manner as if this were a dynamic element. 
     * @param t the exception thrown during validation
     * @param value the given value to be validated
     * @param keyPath the key path associated with this value, identifies the property of an object
     */
	@Override
    public void validationFailedWithException(Throwable t, Object value, String keyPath) {
    	if (keyPath != null && "<none>".equals(keyPath) && t instanceof ValidationException) {
    		ValidationException e = (ValidationException) t;
    		WOAssociation valueAssociation = (WOAssociation) _keyAssociations.valueForKey("value");
    		if (valueAssociation != null) {
    			keyPath = valueAssociation.keyPath();
    		}
    		t = new ValidationException(e.getMessage(), e.object(), keyPath);
    	}
    	parent().validationFailedWithException(t, value, keyPath);
    }
    
    /**
     * @return value for calendarCSS binding, or default of "calendar.css"
     */
    protected String cssFileName() {
    	return (String)valueForBinding("calendarCSS", "calendar.css");
    }
    
    /**
     * @return value for calendarCSSFramework binding, or default of "Ajax"
     */
    protected String cssFileFrameworkName() {
    	return (String)valueForBinding("calendarCSSFramework", "Ajax");
    }
}
