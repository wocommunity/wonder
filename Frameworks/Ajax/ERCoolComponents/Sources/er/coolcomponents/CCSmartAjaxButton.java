package er.coolcomponents;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXNonSynchronizingComponent;

/**
 * Button that can act as an AjaxSubmitButton or a regular CCSubmitLinkButton by
 * enabling the useAjax binding.
 * 
 * @binding useAjax
 * @binding action
 * @binding class
 * @binding dontSubmitForm
 * @binding value
 * @binding alt
 * @binding updateContainerID
 * 
 * @author davidleber
 *
 */
public class CCSmartAjaxButton extends ERXNonSynchronizingComponent {
	
    public CCSmartAjaxButton(WOContext context) {
        super(context);
    }
    
}