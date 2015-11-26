/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.misc;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSValidation;

import er.directtoweb.components.ERDCustomEditComponent;
import er.extensions.eof.ERXEOControlUtilities;

/**
 * Confirming an action template.
 */
public class ERDConfirmMessage extends ERDCustomEditComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERDConfirmMessage(WOContext context) { super(context); }
    
    /** logging support */
    public final static Logger log = Logger.getLogger(ERDConfirmMessage.class);
    
    public String message;

    @Override
    public boolean isStateless() { return true; }
    @Override
    public boolean synchronizesVariablesWithBindings() { return false; }

    // Ok to do this, if they enter text then everything is A OK.
    @Override
    public void awake() { message = hasBinding("defaultMessage") ? (valueForBinding("defaultMessage") == null ? "" : (String)valueForBinding("defaultMessage")) : ""; }

    @Override
    public void reset() {
        super.reset();
        _confirmMessageKey = null;
        _list = null;
        _confirmMessageManditoryErrorMessage = null;
        _confirmMessageTextfieldSize = null;
        _confirmMessageTextfieldMaxlength = null;
        _confirmMessageExplanation = null;
    }
    
    private NSArray _list;
    public NSArray list() {
        if (_list == null) {
            if (object() != null && key() != null) {
                _list = (NSArray)objectKeyPathValue();
            } else if (object() != null) {
                _list = new NSArray(object());
            } else if (hasBinding("dataSource")) {
                _list = ERXEOControlUtilities.arrayFromDataSource((EODataSource)valueForBinding("dataSource"));
            } else {
                log.warn("ERConfirmMessage being used without the proper bindings");
                _list = NSArray.EmptyArray;
            }
            if (_list.count() == 0)
                log.warn("ERConfirmMessage: list set to zero");
        }
        return _list;
    }

    private String _confirmMessageKey = null;
    public String confirmMessageKey() {
        if (_confirmMessageKey == null)
            _confirmMessageKey = (String)valueForBinding("confirmMessageKey");
        return _confirmMessageKey;
    }

    private String _confirmMessageManditoryErrorMessage = null;
    public String  confirmMessageManditoryErrorMessage() {
        if (_confirmMessageManditoryErrorMessage == null) {
            _confirmMessageManditoryErrorMessage = (String)valueForBinding("confirmMessageManditoryErrorMessage");
            _confirmMessageManditoryErrorMessage = (_confirmMessageManditoryErrorMessage == null) ?
                "You must enter a <b>confirmation message</b>.":
                _confirmMessageManditoryErrorMessage;
        }
        return _confirmMessageManditoryErrorMessage;
    }
    private String _confirmMessageExplanation = null;
    public String confirmMessageExplanation() {
        if (_confirmMessageExplanation == null) {
            _confirmMessageExplanation = (String)valueForBinding("confirmMessageExplanation");
        }
        return _confirmMessageExplanation;
    }

    public boolean confirmMessageIsTextfield() {
        return booleanValueForBinding("confirmMessageIsTextfield");
    }

    private String _confirmMessageTextfieldSize = null;
    public String confirmMessageTextfieldSize() {
        if (_confirmMessageTextfieldSize == null) {
            _confirmMessageTextfieldSize = (String)valueForBinding("confirmMessageTextfieldSize");
            _confirmMessageTextfieldSize = (_confirmMessageTextfieldSize == null) ?
                "60":
                _confirmMessageTextfieldSize;
        }
        return _confirmMessageTextfieldSize;
    }
    
    private String _confirmMessageTextfieldMaxlength = null;
    public String confirmMessageTextfieldMaxlength() {
        if (_confirmMessageTextfieldMaxlength == null) {
            _confirmMessageExplanation = (String)valueForBinding("confirmMessageTextfieldMaxlength");
        }
        return _confirmMessageTextfieldMaxlength;
    }

    @Override
    public void takeValuesFromRequest(WORequest r, WOContext c) {
        super.takeValuesFromRequest(r, c);
        if (list().count() > 0) {
            if ((message == null || message.equals("")) && booleanValueForBinding("confirmMessageManditory")) {
                validationFailedWithException(new NSValidation.ValidationException(confirmMessageManditoryErrorMessage()), list().objectAtIndex(0),
                                              confirmMessageKey());
            } else if (message != null && !message.equals("")){
                if (confirmMessageKey() == null)
                    throw new IllegalStateException("You must specify a confirmMessageKey for this pageConfiguration!");
                if (log.isDebugEnabled())
                    log.debug("Setting message: " + message + " for key: " + confirmMessageKey() + " on eos: " + list());
                for (Enumeration e = list().objectEnumerator(); e.hasMoreElements();) {
                    EOEnterpriseObject eo = (EOEnterpriseObject)e.nextElement();
                    eo.takeValueForKeyPath(message, confirmMessageKey());
                }
            }
        } else {
            log.warn("List is zero.  If used in a confirm page template, need to set the object or datasource");
        }
    }
}
