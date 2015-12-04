/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.strings;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WDisplayString;
import com.webobjects.eocontrol.EOEnterpriseObject;

/**
 * <div class="en">
 * Full blown display string with all the bells and whistles.
 * Of the value displayed is an EO, uses the userPresentableDescription()
 * </div>
 * 
 * <div class="ja">
 * このプロパティ・レベル・コンポーネントは文字列表示を担当します。
 * EO の場合には、keyWhenRelationship 又は未設定の場合には userPresentableDescription() を使用します。
 * </div>
 * 
 * @d2wKey class <div class="en"></div>
 *               <div class="ja"></div>
 * @d2wKey escapeHTML <div class="en"></div>
 *                    <div class="ja">HTML をエスケープするかどうか</div>
 * @d2wKey keyWhenRelationship <div class="en"></div>
 *                             <div class="ja">EO の場合のリレーションシップ・キー</div>
 * @d2wKey omitTags <div class="en"></div>
 *                  <div class="ja"></div>
 * @d2wKey valueWhenEmpty <div class="en"></div>
 *                        <div class="ja">null 値の場合に表示する値</div>
 */
public class ERD2WDisplayString extends D2WDisplayString {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERD2WDisplayString(WOContext context) {
        super(context);
    }
    
    @Override
    public Object objectPropertyValue() {
      Object object = super.objectPropertyValue();
      if (object instanceof EOEnterpriseObject) {
        EOEnterpriseObject eo = (EOEnterpriseObject) object;

        String keyWhenRelationship = keyWhenRelationship();
        if (keyWhenRelationship != null || !"userPresentableDescription".equalsIgnoreCase(keyWhenRelationship)) {
          Object val = eo.valueForKey(keyWhenRelationship);
          if(val != null) {
            return val;
          }
        }
        return eo.userPresentableDescription();
      }
      return super.objectPropertyValue();
    }
}
