package er.extensions.localization;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSKeyValueCodingAdditions;

import er.extensions.components.ERXStatelessComponent;

/**
 * <div class="en">
 * Examples:
 * <ol>
 * <li>value = "Localize me" -&gt; the localized value of "Localize me"</li>
 * <li>keyPath = "componentName" (note that the path must be a String) -&gt; localized name of the parent component</li>
 * <li>object = bug, an EO -&gt; localized version of bug.userPresentableDescription (may or may not be useful)</li>
 * <li>object = bug, keyPath = "state" -&gt;  localized version of the bugs state</li>
 * <li>templateString = "You have @assignedBugs.count@ Bug(s) assigned to you", object = session.user
 * -&gt; localized template is evaluated</li>
 * </ol>
 * Bindings:
 * @binding escapeHTML when <code>true</code> will escape the value
 * @binding keyPath the keyPath to get of the object which is to be localized
 * @binding object the object to derive the value of, if not given and keyPath is set, parent() is assumed
 * @binding omitWhenEmpty outputs an empty string if <code>true</code> when it would be <code>null</code>
 * @binding otherObject second object to use with templateString
 * @binding templateString the key to the template to evaluate with object and otherObject
 * @binding value string to localize
 * @binding valueWhenEmpty display this value if value evaluates to <code>null</code>. The binding 
 *            <i>omitWhenEmpty</i> will prevent this.
 * </div>
 * 
 * <div class="ja">
 * サンプル：
 * <ol>
 * <li>value = "Localize me" -&gt; "Localize me" のローカライズ済み文字列</li>
 * <li>keyPath = "componentName" (文字列であるべき) -&gt; 親コンポーネントのローカライズ名</li>
 * <li>object = bug, （ EO　です ） -&gt; bug.userPresentableDescription のローカライズ名 (必要かどうかは不明 ^^)</li>
 * <li>object = bug, keyPath = "state" -&gt; bugs state のローカライズ名</li>
 * <li>templateString = "You have @assignedBugs.count@ Bug(s) assigned to you", object = session.user -&gt; ローカライズ済みテンプレート</li>
 * </ol>
 * バインディング：
 * @binding escapeHTML when <code>true</code> will escape the value
 * @binding keyPath - ローカライズ対応オブジェクトへのキーパス
 * @binding object - 値を取り出すオブジェクト、指定されていない場合とキーパスがセットされていると parent() が使用される
 * @binding omitWhenEmpty outputs an empty string if <code>true</code> when it would be <code>null</code>
 * @binding otherObject - テンプレートと使用する第二のオブジェクト
 * @binding templateString - object と otherObject で使用するテンプレート
 * @binding value - ローカライズする文字列
 * @binding valueWhenEmpty display this value if value evaluates to <code>null</code>. The binding 
 *         <i>omitWhenEmpty</i> will prevent this.
 * </div>
 */
public class ERXLocalizedString extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERXLocalizedString(WOContext context) {
        super(context);
    }

    private String objectToString(Object value) {
        String string = null;
        if(value != null) {
            if(value instanceof String)
                string = (String)value;
            else if(value instanceof EOEnterpriseObject)
                string = ((EOEnterpriseObject)value).userPresentableDescription();
            else
                string = value.toString();
        }
        return string;
    }

    public Object object() {
        Object value;
        if(hasBinding("object"))
            value = valueForBinding("object");
        else
            value = parent();
        return value;
    }
    
    public String value() {
        ERXLocalizer localizer = ERXLocalizer.currentLocalizer();
        String stringToLocalize = null, localizedString = null;
        if(!hasBinding("templateString")) {
            if(hasBinding("object") || hasBinding("keyPath")) {
                Object value = object();
                if(hasBinding("keyPath"))
                    value = NSKeyValueCodingAdditions.Utility.valueForKeyPath(value, stringValueForBinding("keyPath"));
                stringToLocalize = objectToString(value);
            } else if(hasBinding("value")) {
            	stringToLocalize = stringValueForBinding("value");
            	if(booleanValueForBinding("omitWhenEmpty") && localizer.localizedStringForKey(stringToLocalize) == null) {
            		stringToLocalize = "";
            	}
            }
            if(stringToLocalize == null && hasBinding("valueWhenEmpty")) {
                stringToLocalize = stringValueForBinding("valueWhenEmpty");
            }
            if(stringToLocalize != null) {
                localizedString = localizer.localizedStringForKeyWithDefault(stringToLocalize);
            }
        } else {
        	String templateString = stringValueForBinding("templateString");
            Object otherObject = valueForBinding("otherObject");
        	localizedString = localizer.localizedTemplateStringForKeyWithObjectOtherObject(templateString, object(), otherObject);
        }
        return localizedString;
    }
}
