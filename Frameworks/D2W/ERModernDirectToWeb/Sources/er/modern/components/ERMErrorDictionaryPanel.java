package er.modern.components;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXErrorDictionaryPanel;

/**
 * Modern ErrorDictionaryPanel
 * 
 * @binding errorMessages
 * @binding extraErrorMessage
 * @binding errorKeyOrder
 * 
 * @author davidleber
 *
 */
public class ERMErrorDictionaryPanel extends ERXErrorDictionaryPanel
{
    public ERMErrorDictionaryPanel(WOContext context) {
        super(context);
    }
    
}