package er.r2d2w.components.misc;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.extensions.components.ERXStatelessComponent;
import er.extensions.localization.ERXLocalizer;

public class R2DLanguageList extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public static final String LANGUAGE_BINDING_KEY = "language";

	private String languageOption;

	public R2DLanguageList(WOContext context) {
		super(context);
	}
	
	@Override
	public void reset() {
		super.reset();
		languageOption = null;
	}

	/**
	 * @return the available languages from ERXLocalizer
	 */
	public NSArray<String> availableLanguages() {
		return ERXLocalizer.availableLanguages();
	}

	/**
	 * @return the languageOption
	 */
	public String languageOption() {
		return languageOption;
	}

	/**
	 * @param languageOption the languageOption to set
	 */
	public void setLanguageOption(String languageOption) {
		this.languageOption = languageOption;
	}

	/**
	 * @return the language code
	 */
	public String languageCode() {
		return ERXLocalizer.localizerForLanguage(languageOption()).languageCode();
	}

	/**
	 * @return the language code
	 */
	public String hrefLang() {
		if(isSelected()) { return null; }
		return ERXLocalizer.localizerForLanguage(languageOption()).languageCode();
	}

	/**
	 * @return the name for the language localized in that language
	 */
	public String displayStringForLanguage() {
		return ERXLocalizer.localizerForLanguage(languageOption()).localizedStringForKeyWithDefault(languageOption());
	}

	/**
	 * Sets the value of the language binding to the language selected.
	 */
	public void languageAction() {
		setValueForBinding(languageOption(), LANGUAGE_BINDING_KEY);
	}

	/**
	 * @return true if the languageOption is the currently selected language
	 */
	public boolean isSelected() {
		return ERXLocalizer.currentLocalizer().language().equals(languageOption());
	}
	
	/**
	 * @return true if the languageOption is not the currently selected language
	 */
	public boolean isNotSelected() {
		return !isSelected();
	}

}