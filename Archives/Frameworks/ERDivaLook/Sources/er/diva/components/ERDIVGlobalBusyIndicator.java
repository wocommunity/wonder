package er.diva.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOResponse;

import er.ajax.AjaxUtils;

/**
 * Global busy indicator
 * 
 * @author mendis
 * 
 * @binding 	elementName
 * @binding		id
 *
 */
public class ERDIVGlobalBusyIndicator extends WOComponent {
    public ERDIVGlobalBusyIndicator(WOContext context) {
        super(context);
    }
    
    @Override
    public boolean synchronizesVariablesWithBindings() {
    	return false;
    }
    
    /*
     * API/bindings
     */
    public static interface Bindings {
    	public static final String elementName = "elementName";
    	public static final String id = "id";
    }
    
    // accessors
    public String elementName() {
    	return (_elementName() != null) ? _elementName() : "div";
    }
    
    private String _elementName() {
    	return (String) valueForBinding(Bindings.elementName);
    }
    
    public String id() {
    	return (_id() != null) ? _id() : "busy";
    }
    
    private String _id() {
    	return (String) valueForBinding(Bindings.id);
    }
    
    private String script() {
    	return "Ajax.Responders.register({ onCreate: " + onCreate() + ", onComplete:" + onComplete() + "});";
    }
    
    private String onComplete() {
    	return "function() { if($('" + id() + "') && Ajax.activeRequestCount == 0) Effect.Fade('" + id() + "',{duration: 0.25, queue: 'end'}); }";
    }
    
    private String onCreate() {
    	return "function() { if($('" + id() + "') && Ajax.activeRequestCount > 0) Effect.Appear('" + id() + "',{duration: 0.25, queue: 'end'}); }";
    }
    
    // R&R
    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
    	super.appendToResponse(response, context);
    	
    	AjaxUtils.addScriptCodeInHead(response, context, script());
    }
}