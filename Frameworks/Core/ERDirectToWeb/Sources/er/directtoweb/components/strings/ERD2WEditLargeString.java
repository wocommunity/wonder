/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.strings;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WEditLargeString;

import er.extensions.foundation.ERXValueUtilities;

/**
 * <div class="en">
 * Same as D2WEditLargeString except that it allows you to
 * have empty strings in fields that don't allow null.
 * You need to set <code>isMandatory</code> to false and the null
 * value is morphed to the empty string. It also pulls
 * the <code>disabled</code> binding from the WOContext, allowing
 * you to have a readonly field.
 * </div>
 * 
 * <div class="ja">
 * D2WEditLargeString と基本的には同じ動作しますが、
 * null を許可しない空文字列を対応しています。
 * 
 * <code>isMandatory</code> を false にセットすると null 値が空文字列に変換されます。
 * さらに <code>disabled</code> バインディングでリードオンリー・フィールドを作成できます。
 * </div>
 * 
 * @d2wKey id <div class="en"></div>
 *            <div class="ja">id タグ</div>
 * @d2wKey title <div class="en"></div>
 *               <div class="ja">title タグ</div>
 * @d2wKey name <div class="en"></div>
 *              <div class="ja">name タグ</div>
 * @d2wKey length <div class="en"></div>
 *                <div class="ja">cols タグ</div>
 * @d2wKey rows <div class="en"></div>
 *              <div class="ja">rows タグ</div>
 * @d2wKey disabled <div class="en"></div>
 *                  <div class="ja">編集禁止</div>
 * @d2wKey isMandatory <div class="en"></div>
 *                     <div class="ja">入力必須</div>
 * @d2wKey staySmall <div class="en"></div>
 *                   <div class="ja"></div>
 * @d2wKey readOnly <div class="en"></div>
 *                  <div class="ja"></div>
 * 
 * @d2wKey staySmall - true の場合 EditLargeString の替わりに EditString を使用する (see Rule)
 * @d2wKey readOnly - 編集禁止
 */
public class ERD2WEditLargeString extends D2WEditLargeString {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERD2WEditLargeString(WOContext context) {
        super(context);
    }

    private Object fixValue(Object value) {
        if("".equals(value)) {
            // AK: this is probably obsolete. It fixes that WOText would give you
            // an empty string instead of null, which was was WOTextField is doing
            // This seems to be fixed in >=5.3.1 where context.stringFormValueForKey returns 
            // null on empty strings.
            value = null;
        }
        if (value == null) {
            boolean fixNullValue = d2wContext().attribute() != null && !d2wContext().attribute().allowsNull();
            if(fixNullValue) {
                fixNullValue = !ERXValueUtilities.booleanValue(d2wContext().valueForKey("isMandatory"));
            }
            if(fixNullValue) {
                value = "";
            }
        }
        return value;
    }

    public boolean disabled() {
      if(ERXValueUtilities.booleanValue(d2wContext().valueForKey("disabled"))) {
        return true;
      }
      if(ERXValueUtilities.booleanValue(d2wContext().valueForKey("readOnly"))) {
        return true;
      }
      return false;
    }

    @Override
    public void validationFailedWithException(Throwable theException, Object theValue, String theKeyPath) {
        // This is for number formatting exceptions
        String keyPath = theKeyPath.equals("value") ? propertyKey() : theKeyPath;
        parent().validationFailedWithException(theException, theValue, keyPath);
    }

    @Override
    public Object validateTakeValueForKeyPath(Object value, String keyPath) throws ValidationException {
        value = fixValue(value);
        return super.validateTakeValueForKeyPath(value, keyPath);
    }

    @Override
    public void setValue(Object value) {
        value = fixValue(value);
        super.setValue(value);
    }
}
