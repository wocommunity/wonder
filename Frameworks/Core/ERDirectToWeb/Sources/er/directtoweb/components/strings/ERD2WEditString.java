/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.strings;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.directtoweb.D2WEditString;

import er.extensions.foundation.ERXValueUtilities;
import er.extensions.validation.ERXValidationException;
import er.extensions.validation.ERXValidationFactory;

/**
 * <span class="en">
 * Allows specifing the maxLength for a WOTextField.
 * 
 * @d2wKey id
 * @d2wKey title
 * @d2wKey name
 * @d2wKey maxLength
 * @d2wKey length
 * 
 * @d2wKey readOnly
 * </span>
 * 
 * <span class="ja">
 * このプロパティ・レベル・コンポーネントは編集可能文字列を表示します。
 * 
 * @d2wKey id - id タグ
 * @d2wKey title - title タグ
 * @d2wKey name - name タグ
 * @d2wKey maxLength - maxLength タグ
 * @d2wKey length - 入力枠のサイズ
 * 
 * @d2wKey readOnly - 編集禁止
 * </span>
 */
public class ERD2WEditString extends D2WEditString {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERD2WEditString(WOContext context) { super(context); }
    
    @Override
    public void validationFailedWithException(Throwable theException,Object theValue, String theKeyPath) {
        // This is for number formatting exceptions
        String keyPath = theKeyPath.equals("value") ? propertyKey() : theKeyPath;
        parent().validationFailedWithException(theException, theValue, keyPath);
    }
    
    @Override
	public void takeValuesFromRequest(WORequest arg0, WOContext arg1) {
		super.takeValuesFromRequest(arg0, arg1);
		// AK: meh... this would belong right in D2WComponent... it's so you can have fake keys that behave like attibutes
		if (ERXValueUtilities.booleanValue(d2wContext().valueForKey("displayRequiredMarker")) && d2wContext().valueForKey("attribute") == null && value() == null) {
			ERXValidationException exception = ERXValidationFactory.defaultFactory().createException(object(), propertyKey(), value(), "NullPropertyException");
			validationFailedWithException(exception, value(), propertyKey());
		}
	}
}
