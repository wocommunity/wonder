package er.coolcomponents;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.extensions.appserver.ERXResponseRewriter;
import er.extensions.components.ERXComponent;

/**
 * Wrapper around http://jscolor.com/
 * this is a first implementation of CCColorPicker that actually use JScolor
 * 
 * @binding value String
 * @bindind id
 * @binding style
 * @binding class
 * 
 * @author amedeomantica
 *
 */

public class CCColorPicker extends ERXComponent {
    
	private String _value;
    
	//public static final String FRAMEWORK_NAME = "ERCoolComponents";
	//public static final String CSS_FILENAME = "";
	//public static final String JS_FILENAME = "CCColorPicker/jscolor.js";
	
	public CCColorPicker(WOContext context) {
        super(context);
    }
	
	@Override
	public boolean isStateless() {
		return true;
	}
	
	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}
	
	@Override
	public void reset() {
		super.reset();
		_value = null;
	}
	
	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		//ERXResponseRewriter.addStylesheetResourceInHead(response, context, FRAMEWORK_NAME, JS_FILENAME);
		super.appendToResponse(response, context);
	}
	
	
	public String fieldClass() {
		if (canGetValueForBinding("class")) {
			return "colorpickerfield " + valueForStringBinding("class", "");
		} else {
			return "colorpickerfield";
		}
	}

	public String value() {
		return stringValueForBinding("value");
	}

	public void setValue(String colorValue) {
		setValueForBinding(colorValue, "value");
	}
}