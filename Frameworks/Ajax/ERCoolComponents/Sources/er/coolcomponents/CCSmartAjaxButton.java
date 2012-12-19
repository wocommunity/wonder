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
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public CCSmartAjaxButton(WOContext context) {
        super(context);
    }
    
}