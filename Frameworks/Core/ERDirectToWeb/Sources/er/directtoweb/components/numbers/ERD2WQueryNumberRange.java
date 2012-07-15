//
// ERD2WQueryNumber.java: Class file for WO Component 'ERD2WQueryNumber'
// Project ERDirectToWeb
//
// Created by giorgio on 05/10/04
//

package er.directtoweb.components.numbers;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WQueryNumberRange;

/**
 * <span class="ja">
 * このプロパティ・レベル・コンポーネントは number のクエリを二つの数値の間でビルドします。
 * </span>
 */
public class ERD2WQueryNumberRange extends D2WQueryNumberRange {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERD2WQueryNumberRange(WOContext context) {
        super(context);
    }

}
