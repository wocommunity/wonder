//
// ERXEditDateJavascript.java: Class file for WO Component 'ERXEditDateJavascript'
// Project ERExtensions
//
// Created by bposokho on Thu Jan 16 2003
//
package er.extensions;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;

public class ERXEditDateJavascript extends ERXStatelessComponent {
	static final Logger log = Logger.getLogger(ERXEditDateJavascript.class);
	private static String _datePickerJavaScriptUrl;

	private String elementID;
	
	public ERXEditDateJavascript(WOContext context) {
		super(context);
	}

	public void awake() {
		elementID = context().elementID().replace('.', '_');
	}
	
	public String dateformat() {
		String format = (String) stringValueForBinding("dateformat");
		if (format == null) {
			format = ERXTimestampFormatter.DEFAULT_PATTERN;
		}
		return format;
	}

	public void setDateformat(String value) {
		setValueForBinding(value, "dateformat");
	}

	public String dateString() {
		return (String) stringValueForBinding("dateString");
	}

	public void setDateString(String value) {
		setValueForBinding(value, "dateString");
	}

	public String name() {
		return "datebox" + elementID;
	}

	public String href() {
		String formName = ERXWOForm.formName(context(), "EditForm");
		return "show_calendar('" + formName + "." + name() + "', null, null, '" + formatterStringForScript() + "'); return false;";
	}

	public String datePickerJavaScriptUrl() {
		if (_datePickerJavaScriptUrl == null) {
			_datePickerJavaScriptUrl = application().resourceManager().urlForResourceNamed("date-picker.js", "ERExtensions", null, context().request());
		}
		return _datePickerJavaScriptUrl;
	}

	public String formatterStringForScript() {
		String format = ERXLocalizer.currentLocalizer().localizedStringForKeyWithDefault(dateformat());
		return ERXEditDateJavascript.formatterStringForScript(format);
	}

	public static String formatterStringForScript(String format) {
		String result = format;
		result = ERXStringUtilities.replaceStringByStringInString("%Y", "yyyy", result);
		result = ERXStringUtilities.replaceStringByStringInString("%y", "yy", result);
		result = ERXStringUtilities.replaceStringByStringInString("%m", "MM", result);
		result = ERXStringUtilities.replaceStringByStringInString("%d", "dd", result);
		result = ERXStringUtilities.replaceStringByStringInString("%b", "MON", result);
		return result;
	}
}
