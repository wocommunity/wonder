//
// ERXEditDateJavascript.java: Class file for WO Component 'ERXEditDateJavascript'
// Project ERExtensions
//
// Created by bposokho on Thu Jan 16 2003
//
package er.extensions;

import com.webobjects.appserver.*;

public class ERXEditDateJavascript extends WOComponent {
    static final ERXLogger log = ERXLogger.getERXLogger(ERXEditDateJavascript.class);
    private static String _datePickerJavaScriptUrl;
    
    protected int i = 0;
    public String dateString;
    public String dateformat = ERXTimestampFormatter.DEFAULT_PATTERN;

    public ERXEditDateJavascript(WOContext context) {
        super(context);
    }

    
    public String name() { 
    	return "datebox"+hashCode(); 
    }
    public String href() {
        String formName = ERXWOForm.formName(context(), "EditForm");
        return "show_calendar('"+formName+"."+name()+ "', null, null, '"+formatterStringForScript()+"'); return false;";
    }

    public String datePickerJavaScriptUrl() {
        if (_datePickerJavaScriptUrl==null) {
            _datePickerJavaScriptUrl= application().resourceManager().urlForResourceNamed("date-picker.js", "ERExtensions", null, context().request());
        }
        return _datePickerJavaScriptUrl;
    }

    public void appendToResponse(WOResponse aResponse, WOContext aContext) {
        i++;
        super.appendToResponse(aResponse,aContext);
    }
    
    public String formatterStringForScript() {
        String format = ERXLocalizer.currentLocalizer().localizedStringForKeyWithDefault(dateformat);
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
