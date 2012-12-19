package er.extensions.localization;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSKeyValueCodingAdditions;

import er.extensions.components.ERXStatelessComponent;

/**
 * <span class="en">
 * Examples:
 * 1) value = "Localize me" -> the localized value of "Localize me"
 * 2) keyPath = "componentName" (note that the path must be a String) -> localized name of the parent component
 * 3) object = bug, an EO -> localized version of bug.userPresentableDescription (may or may not be useful)
 * 4) object = bug, keyPath = "state" ->  localized version of the bugs state
 * 5) templateString = "You have @assignedBugs.count@ Bug(s) assigned to you", object = session.user
 * -> localized template is evaluated
 * 
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
 * </span>
 * 
 * <span class="ja">
 * サンプル：
 * 1) value = "Localize me" -> "Localize me" のローカライズ済み文字列
 * 2) keyPath = "componentName" (文字列であるべき) -> 親コンポーネントのローカライズ名
 * 3) object = bug, （ EO　です ） -> bug.userPresentableDescription のローカライズ名 (必要かどうかは不明 ^^)
 * 4) object = bug, keyPath = "state" ->  bugs state のローカライズ名
 * 5) templateString = "You have @assignedBugs.count@ Bug(s) assigned to you", object = session.user -> ローカライズ済みテンプレート
 * 
 * バインディング：
 * @binding escapeHTML when <code>true</code> will escape the value
 * @binding keyPath - ローカライズ対応オブジェクトへのキーパス
 * @binding object - 値を取り出すオブジェクト、指定されていない場合とキーパスがセットされていると parrent() が使用される
 * @binding omitWhenEmpty outputs an empty string if <code>true</code> when it would be <code>null</code>
 * @binding otherObject - テンプレートと使用する第二のオブジェクト
 * @binding templateString - object と otherObject で使用するテンプレート
 * @binding value - ローカライズする文字列
 * @binding valueWhenEmpty display this value if value evaluates to <code>null</code>. The binding 
 *         <i>omitWhenEmpty</i> will prevent this.
 * </span>
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
