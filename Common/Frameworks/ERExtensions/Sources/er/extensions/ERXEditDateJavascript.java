//
// ERXEditDateJavascript.java: Class file for WO Component 'ERXEditDateJavascript'
// Project ERExtensions
//
// Created by bposokho on Thu Jan 16 2003
//
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
//CHECKME: Do we need this? Why not use ERDEditDateJavascript from ERD2W?
// otherwise, where is the dateString binding from the .api?
public class ERXEditDateJavascript extends WOComponent {
    static final ERXLogger log = ERXLogger.getERXLogger(ERXEditDateJavascript.class);
    
    public ERXEditDateJavascript(WOContext context) {
        super(context);
    }

    int i = 0;

    //public boolean isStateless() { return true; }
    //public boolean synchronizesVariablesWithBindings() { return false; }

    protected static final NSTimestampFormatter DATE_FORMAT = new NSTimestampFormatter("%m/%d/%Y");
    protected static final NSTimestampFormatter DATE_FORMAT_YEAR_TWO_DIGITS = new NSTimestampFormatter("%m/%d/%y");
    public String dateString;
    
    public String name() { return "datebox"+hashCode(); }
    public String href() {
        String formName = "EditForm";
        if(context() instanceof ERXMutableUserInfoHolderInterface) {
            formName = (String)((ERXMutableUserInfoHolderInterface)context()).mutableUserInfo().objectForKey("formName");
        }
        return "show_calendar('"+formName+"."+name()+ "'); return false;";
    }
    //public Object value() {return dateString;}

    private static String _datePickerJavaScriptUrl;
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
}
