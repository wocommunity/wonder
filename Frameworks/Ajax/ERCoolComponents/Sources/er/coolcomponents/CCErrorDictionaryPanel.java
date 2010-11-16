package er.coolcomponents;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXErrorDictionaryPanel;

/**
 * Modernized ErrorDictionaryPanel
 * 
 * @binding errorMessages
 * @binding extraErrorMessage
 * @binding errorKeyOrder
 * 
 * @author davidleber
 *
 */
public class CCErrorDictionaryPanel extends ERXErrorDictionaryPanel {
	
    public CCErrorDictionaryPanel(WOContext context) {
        super(context);
    }
    
}