/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.pages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;

import er.directtoweb.interfaces.ERDPickPageInterface;
import er.extensions.eof.ERXConstant;
import er.extensions.foundation.ERXValueUtilities;

// The pick interface is nice in that it doesn't require passing back an EO as
// the SelectPageInterface requires.
// Important D2W Keys:
// explanationComponentName - Used if you want to add an explanation to the top
// of the page.
// choices - Array of choices to be displayed.
// choiceKeyPath - keypath from the component to return a list of choices, ie
// session.choicesForUser
// choiceErrorMessage - error message displayed if the user decides they don't
// want to pick anything.
// choiceDisplayKey - defaults to toString. Allows you to provide custom choice
// objects.
// uiStyle - defaults to radio button, can also be a checkbox list, "radio" or
// "checkbox"
/**
 * @d2wKey selectionManditory
 * @d2wKey choiceDisplayKey
 * @d2wKey choiceErrorMessage
 * @d2wKey choices
 * @d2wKey choiceKeyPath
 * @d2wKey pageConfiguration
 */
public class ERD2WPickTypePage extends ERD2WPage implements ERDPickPageInterface {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERD2WPickTypePage(WOContext context) {
        super(context);
    }

    /** logging support */
    public static final Logger log = LoggerFactory.getLogger(ERD2WPickTypePage.class);

    public boolean selectionManditory() {
        return ERXValueUtilities.booleanValue(d2wContext().valueForKey("selectionManditory"));
    }

    public NSMutableArray selections = new NSMutableArray();

    public Object item;

    public Object selection() {
        return selections.count() > 0 ? selections.lastObject() : null;
    }

    public void setSelection(Object value) {
        selections = value != null ? new NSMutableArray(value) : new NSMutableArray();
    }

    public NSArray selectedObjects() {
        return selections;
    }

    public void setSelectedObjects(NSArray value) {
        selections = new NSMutableArray(value);
    }

    private WOComponent _cancelPage;

    public WOComponent cancelPage() {
        return _cancelPage;
    }

    public void setCancelPage(WOComponent cp) {
        _cancelPage = cp;
    }

    public String choiceDisplayKey() {
        return (String) d2wContext().valueForKey("choiceDisplayKey");
    }

    public String choiceErrorMessage() {
        return (String) d2wContext().valueForKey("choiceErrorMessage");
    }

    private NSArray _choices;

    public NSArray choices() {
        if (_choices == null) {
            if (d2wContext().valueForKey("choices") != null) {
                _choices = (NSArray) d2wContext().valueForKey("choices");
            } else if (d2wContext().valueForKey("choiceKeyPath") != null) {
                _choices = (NSArray) valueForKeyPath((String) d2wContext().valueForKey("choiceKeyPath"));
            } else if (dataSource() != null) {
                _choices = dataSource().fetchObjects();
            } else {
                log.error("Unable to create choices list for pageConfiguration: " + d2wContext().valueForKey("pageConfiguration") + " context: " + d2wContext());
                _choices = ERXConstant.EmptyArray;
            }
        }
        return _choices;
    }

    public void setChoices(NSArray choices) {
        _choices = choices;
    }

    public String displayName() {
        String displayName = null;
        if (item != null) {
            if (item instanceof String) {
                displayName = (String) item;
            } else {
                String choiceDisplayKey = choiceDisplayKey();
                if (item instanceof EOEnterpriseObject) {
                    displayName = (String) ((EOEnterpriseObject) item).valueForKeyPath(choiceDisplayKey);
                } else if (item instanceof Object && choiceDisplayKey.indexOf(".") == -1) {
                    displayName = (String) ((NSKeyValueCoding) item).valueForKey(choiceDisplayKey);
                } else {
                    displayName = (String) NSKeyValueCodingAdditions.Utility.valueForKeyPath(item, choiceDisplayKey);
                }
            }
        }
        return displayName != null ? displayName : " <null> ";
    }

    @Override
    public WOComponent nextPage() {
        if (selections.count() == 0 && selectionManditory()) {
            errorMessage = choiceErrorMessage() != null ? choiceErrorMessage() : "Please make a selection before continuing";
        } else {
            errorMessage = "";
        }
        WOComponent result = null;
        if (errorMessage.equals("")) {
            result = nextPageFromDelegate();
            if (result == null) {
                result = super.nextPage();
            }
        }
        return result;
    }
}
