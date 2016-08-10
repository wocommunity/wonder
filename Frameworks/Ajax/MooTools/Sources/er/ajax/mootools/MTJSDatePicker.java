package er.ajax.mootools;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.ajax.AjaxComponent;
import er.ajax.AjaxOption;
import er.ajax.AjaxUtils;

/**
 * @binding pickerClass - default (datepicker) CSS class for the main datepicker container element.
 * @binding toggleElements - default (null) Toggle your datepicker by clicking another element. Specify as a MooTools selector. The resulting elements are mapped to the selected datepickers by index (the 1st toggle works for the 1st input, the 2nd toggle works for the 2nd input, etc.)
 * @binding days - default (['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday']) - Labels for the days, allows for localization.
 * @binding daysShort - default (2) - Length of day-abbreviations used in the datepicker.
 * @binding months - default (['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December']) Labels for the months, allows for localization.
 * @binding monthShort default (3) - Length of month-abbreviations used in the datepicker.
 * @binding startDay default (1) - First day of a week. Can be 0 (Sunday) through 6 (Saturday) - be aware that this may affect your layout, since days in the last column will likely have a style with right-margin: 0px;which would need to be applied to a different day. Default value 1 is Monday.
 * @binding timePicker default (false) - Enable/disable timepicker functionality. See timepicker example below. Hours/Minutes values can be changed using the scrollwheel.
 * @binding timePickerOnly (false) - set to true to use datepicker for time-selection only; recommended formatis H:i; will automatically force timePicker and startView options into timepicker mode
 * @binding yearPicker (true) - Enable/disable yearpicker functionality. Makes it much easier to change years.
 * @binding yearsPerPage (20) - Amount of years to show in the year-picking view. Be aware that this may affect your layout.
 * @binding dateFormat - the dateformat to use.  Same as normal.
 * @binding animationDuration default (400) - Duration of the slide/fade animations in milliseconds.
 * @binding useFadeInOut default (true (false on Internet Explorer)) Whether to fade-in/out the datepicker popup.
 * @binding startView default (month) - Initial view of the datepicker. Allowed values are: time(only when timePicker option is true), month,year, decades
 * @binding allowEmpty default (false) - When set to true the datepicker intializes empty when no value was set (instead of starting at today). In addition the backspace- and delete-key will remove a value from the input. Check out the Allow empty example below.
 * @binding positionOffset default ({ x: 0, y: 0 }) Allows you to tweak the position at which the datepicker appears, relative to the input element. Formatted as an object with x and y properties. Values can be negative.
 * @binding debug default (false) - When enabled, will not hide the original input element. Additionally, any formatting errors will be alerted to the user.
 * @binding onShow default null - function to fire onShow
 * @binding onCancel default null function to fire onCancel
 * @binding onSelect default null function to fire onSelect
 */
public class MTJSDatePicker extends AjaxComponent {

	private static final long serialVersionUID = 1L;

	public MTJSDatePicker(WOContext context) {
		super(context);
	}

	@Override
	public boolean isStateless() {
		return true;
	}

	@Override
	protected void addRequiredWebResources(WOResponse res) {
		MTAjaxUtils.addScriptResourceInHead(context(), res, "MooTools", MTAjaxUtils.MOOTOOLS_CORE_JS);
		MTAjaxUtils.addScriptResourceInHead(context(), res, "MooTools", "scripts/plugins/datepicker/datepicker.js");
		boolean useDefaultCSS = booleanValueForBinding("useDefaultCSS", true);
		if(useDefaultCSS) {
			AjaxUtils.addStylesheetResourceInHead(context(), res, "MooTools", "scripts/plugins/datepicker/datepicker.css");
		}
	}

	@Override
	public WOActionResults handleRequest(WORequest request, WOContext context) {
		return null;
	}

	private static String[][] conversionTable = new String[][] { { "%Y", "Y" }, { "%y", "y" }, { "%B", "F" }, { "%B", "F" },
		{ "%B", "M" }, { "%m", "m" }, { "%A", "l" }, { "%A", "l" }, { "%A", "l" }, { "%A", "D" }, { "%d", "d" },
		{ "%H", "H" }, { "%M", "i" }, { "%S", "s" }, { "%I", "h" }, { "%p", "a" } };  

	public static String convertDateToPhpFormat(String javaFormat) {
		String result = javaFormat;
		for(int i = 0; i < conversionTable.length; i++) {
			result = result.replaceAll(conversionTable[i][0], conversionTable[i][1]);
		}
		return result;
	}

	public static String convertDateToJavaFormat(String phpFormat) {
		String result = phpFormat;
		for(int i = 0; i < conversionTable.length; i++) {
			result = result.replaceAll(conversionTable[i][1], conversionTable[i][0]);
		}
		return result;
	}

	public String classes() {
		String userDefinedClass = valueForStringBinding("class", "");
		userDefinedClass += userDefinedClass.length() > 0 ? " dateinput" : "dateinput";
		return userDefinedClass;
	}

	public String format() {
		String format = (String) valueForBinding("dateformat", "%Y-%m-%d");
		return "'" + convertDateToPhpFormat(format) + "'";
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public NSDictionary createAjaxOptions() {
		
		NSMutableArray ajaxOptionsArray = new NSMutableArray();
		ajaxOptionsArray.addObject(new AjaxOption("pickerClass", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("toggleElements", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("days", AjaxOption.ARRAY));
		ajaxOptionsArray.addObject(new AjaxOption("dayShort", AjaxOption.NUMBER));
		ajaxOptionsArray.addObject(new AjaxOption("months", AjaxOption.ARRAY));
		ajaxOptionsArray.addObject(new AjaxOption("monthShort", AjaxOption.NUMBER));
		ajaxOptionsArray.addObject(new AjaxOption("startDay", AjaxOption.NUMBER));
		ajaxOptionsArray.addObject(new AjaxOption("timePicker", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("timePickerOnly", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("yearPicker", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("yearsPerPage", AjaxOption.NUMBER));
		ajaxOptionsArray.addObject(new AjaxOption("animationDuration", AjaxOption.NUMBER));
		ajaxOptionsArray.addObject(new AjaxOption("useFadeInOut", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("startView", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("allowEmpty", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("positionOffset", AjaxOption.ARRAY));
		ajaxOptionsArray.addObject(new AjaxOption("minDate", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("maxDate", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("debug", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("onShow", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("onClose", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("onSelect", AjaxOption.SCRIPT));

		NSMutableDictionary options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, this);
		options.takeValueForKey(format(), "format");
		options.takeValueForKey(format(), "inputOutputFormat");
		return options;
	}
}
