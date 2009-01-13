package ognl.helperfunction;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

public class WOHelperFunctionTagRegistry {
	public static Logger log = Logger.getLogger(WOHelperFunctionTagRegistry.class);

	private static NSMutableDictionary _tagShortcutMap = new NSMutableDictionary();
	private static NSMutableDictionary _tagProcessorMap = new NSMutableDictionary();
	private static boolean _allowInlineBindings = false;

	public static NSDictionary tagShortcutMap() {
		return _tagShortcutMap;
	}
	
	public static NSDictionary tagProcessorMap() {
		return _tagProcessorMap;
	}
	
	public static void registerTagShortcut(String fullElementType, String shortcutElementType) {
		_tagShortcutMap.setObjectForKey(fullElementType, shortcutElementType);
	}

	public static void registerTagProcessorForElementType(WOTagProcessor tagProcessor, String elementType) {
		_tagProcessorMap.setObjectForKey(tagProcessor, elementType);
	}

	public static void setAllowInlineBindings(boolean allowInlineBindings) {
		_allowInlineBindings = allowInlineBindings;
	}

	public static boolean allowInlineBindings() {
		return _allowInlineBindings;
	}

	static {
		WOHelperFunctionTagRegistry.log.setLevel(Level.WARN);

		WOHelperFunctionTagRegistry.registerTagShortcut("WOString", "string");
		WOHelperFunctionTagRegistry.registerTagShortcut("WOString", "str");
		WOHelperFunctionTagRegistry.registerTagShortcut("ERXElse", "else");
		WOHelperFunctionTagRegistry.registerTagShortcut("ERXWOConditional", "if");
		WOHelperFunctionTagRegistry.registerTagShortcut("ERXWOConditional", "condition");
		WOHelperFunctionTagRegistry.registerTagShortcut("ERXWOConditional", "conditional");
		WOHelperFunctionTagRegistry.registerTagShortcut("WOHyperlink", "link");
		WOHelperFunctionTagRegistry.registerTagShortcut("WORepetition", "loop");
		WOHelperFunctionTagRegistry.registerTagShortcut("WOTextField", "textfield");
		WOHelperFunctionTagRegistry.registerTagShortcut("WOCheckBox", "checkbox");
		WOHelperFunctionTagRegistry.registerTagShortcut("WOHiddenField", "hidden");
		WOHelperFunctionTagRegistry.registerTagShortcut("WOPopUpButton", "select");
		WOHelperFunctionTagRegistry.registerTagShortcut("WORadioButton", "radio");
		WOHelperFunctionTagRegistry.registerTagShortcut("WOPasswordField", "password");
		WOHelperFunctionTagRegistry.registerTagShortcut("WOFileUpload", "upload");
		WOHelperFunctionTagRegistry.registerTagShortcut("WOText", "text");
		WOHelperFunctionTagRegistry.registerTagShortcut("WOForm", "form");
		WOHelperFunctionTagRegistry.registerTagShortcut("WOSubmitButton", "submit");
		WOHelperFunctionTagRegistry.registerTagShortcut("ERXLocalizedString", "localized");
		WOHelperFunctionTagRegistry.registerTagShortcut("WOImage", "img");
		WOHelperFunctionTagRegistry.registerTagShortcut("WOImage", "image");

		WOHelperFunctionTagRegistry.registerTagProcessorForElementType(new NotTagProcessor(), "not");
	}

}
