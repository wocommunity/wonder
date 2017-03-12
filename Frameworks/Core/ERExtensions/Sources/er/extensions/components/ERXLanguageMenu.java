package er.extensions.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXValueUtilities;
import er.extensions.localization.ERXLocalizer;

/**
 * A simple popup menu for language selection. The menu itself is can be
 * localized in two different ways. It allows all language names to be presented
 * in the current localized language, or each language name can be presented in
 * its native language. The Localizable.strings keys for these language names
 * are simply the English language name for that language. For example, if you
 * were to localize this menu in an application that supports English and
 * German, you would have:
 * 
 * <pre>
 * &quot;English&quot; = &quot;English&quot;;
 * &quot;German&quot; = &quot;German&quot;;
 * </pre>
 * 
 * in your English.lproj/Localizable.strings file, and
 * 
 * <pre>
 * &quot;English&quot; = &quot;Englisch&quot;;
 * &quot;German&quot; = &quot;Deutsch&quot;;
 * </pre>
 * 
 * in your German.lproj/Localizable.strings file. Beyond that, you only need to
 * stick it in a form and bind the language binding to something that stores a
 * language value. For example, your WOD file might contain:
 * <code>LanguageMenu: ERXLanguageMenu { language = session.language; }</code>
 * 
 * @author Ramsey Gurley
 * @binding class (optional) the html class attribute string
 * @binding dir (optional) the html dir attribute string. Defines the text
 *          direction and should only be equal to "ltr" for "left to
 *          right" text direction or "rtl" for "right to left" text direction.
 * @binding disabled (optional) the html disabled attribute string. This is
 *          passed directly to the component so you should only bind the string
 *          value "disabled" if you wish to disable the component. Otherwise,
 *          leave it unbound.
 * @binding id (optional) the html id attribute string. Each id value should be
 *          unique and not repeated on the same page.
 * @binding language the language selected in the menu.
 * @binding localizeDisplayStrings (optional) if true, this will cause each
 *          language name to be displayed in its native language. If false, all
 *          languages will be presented in the current language.
 * @binding noSelectionString (optional) the string presented to indicate a null
 *          or empty selection.
 * @binding size (optional) the html size attribute string. This defines the
 *          maximum number of menu items to display.
 * @binding style (optional) the html style attribute string
 * @binding title (optional) the html title attribute string
 * 
 */
public class ERXLanguageMenu extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private static NSDictionary<String, String> displayStringForLanguages;
	private static final String localizeDisplayStringsKey = "localizeDisplayStrings";
	
	private String _languageOption;

	public ERXLanguageMenu(WOContext context) {
		super(context);
	}

	@Override
	public void reset() {
		super.reset();
		_languageOption = null;
	}

	public NSArray availableLanguages() {
		return ERXLocalizer.availableLanguages();
	}

	/**
	 * @return the languageOption
	 */
	public String languageOption() {
		return _languageOption;
	}

	/**
	 * @param languageOption
	 *            the languageOption to set
	 */
	public void setLanguageOption(String languageOption) {
		_languageOption = languageOption;
	}

	/**
	 * @return the displayString value for the language
	 */
	public String displayStringForLanguage() {
		if (ERXValueUtilities.booleanValueWithDefault(valueForBinding(localizeDisplayStringsKey), true)) {
			return displayStringForLanguages().get(languageOption());
		}
		return ERXLocalizer.currentLocalizer().localizedStringForKey(languageOption());
	}

	/**
	 * @return an array of localized language names
	 */
	private NSDictionary<String, String> displayStringForLanguages() {
		if (displayStringForLanguages == null) {
			NSMutableDictionary<String, String> displayStrings = new NSMutableDictionary<>();
			for (Object o : availableLanguages()) {
				String languageKey = (String) o;
				ERXLocalizer loc = ERXLocalizer.localizerForLanguage(languageKey);
				String displayString = loc.localizedStringForKey(languageKey);
				if (ERXStringUtilities.stringIsNullOrEmpty(displayString)) {
					displayString = languageKey;
				}
				displayStrings.put(languageKey, displayString);
			}
			displayStringForLanguages = displayStrings.immutableClone();
		}
		return displayStringForLanguages;
	}

}