package er.modern.ajax.components;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXNonSynchronizingComponent;

/**
 * Button that can act as an AjaxSubmitButton or a regular ERMSubmitLinkButton by
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
public class ERMSmartAjaxButton extends ERXNonSynchronizingComponent {
	
    public ERMSmartAjaxButton(WOContext context) {
        super(context);
    }
    
}