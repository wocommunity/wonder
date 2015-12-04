package er.prototaculous;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.appserver.ERXWOContext;

/**
 * An Ajax.Updater as a button (that also submits the form and form values).
 * <p>
 * In order to use this, the form elements(i.e WOTextField, etc) need to have their name attribute bound to concrete values.
 * The Prototype Ajax.Updater form is parametized using these names. WOElements won't correctly take form values otherwise.
 * Also Prototype/WO integration requires the use of &lt;button&gt; rather than &lt;input&gt; WOSubmitButtons.
 * <p>
 * So set:
 * <blockquote>er.extensions.foundation.ERXPatcher.DynamicElementsPatches.SubmitButton.useButtonTag=true</blockquote>
 * 
 * @see AjaxUpdater
 * @author mendis
 */
public class AjaxUpdaterButton extends AjaxUpdater {
    public AjaxUpdaterButton(WOContext context) {
        super(context);
    }
    
    @Override
    public boolean isStateless() {
    	return true;
    }
    
    // accessors    
    @Override
    protected NSArray<String> _options() {
    	NSMutableArray<String> _options = super._options().mutableClone();    	
    	
    	// add options
    	_options.add("parameters: this.form.serialize(true)");    	
    	return _options.immutableClone();
    }
    
    @Override
    protected String url() {
    	if (hasBinding(Bindings.action)) {
    		return "'" + context().componentActionURL(application().ajaxRequestHandlerKey()) + "'";
    	} else return super.url();
    }
    
    // actions
    public WOActionResults invokeAction() {
		if (hasBinding(Bindings.action)) {
			WOActionResults action = (WOActionResults) valueForBinding(Bindings.action);
			if (action != null) {
				if (action instanceof WOComponent)  ((WOComponent) action)._setIsPage(true);	// cache is pageFrag cache
				return action;	
			}
		} return context().page();
    }
}
