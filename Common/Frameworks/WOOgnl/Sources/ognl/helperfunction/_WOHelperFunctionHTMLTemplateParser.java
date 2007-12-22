package ognl.helperfunction;

import java.util.Enumeration;

import ognl.helperfunction.compatibility.WOMiddleManDeclarationFormatException;
import ognl.helperfunction.compatibility.WOMiddleManHTMLFormatException;
import ognl.helperfunction.compatibility.WOMiddleManParser;
import ognl.webobjects.WOOgnl;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver._private.WOConstantValueAssociation;
import com.webobjects.appserver._private.WODeclaration;
import com.webobjects.appserver._private.WOHTMLCommentString;
import com.webobjects.appserver._private.WOKeyValueAssociation;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

public class _WOHelperFunctionHTMLTemplateParser extends WOMiddleManParser implements WOHelperFunctionHTMLParserDelegate {
	public static Logger log = Logger.getLogger(WOHelperFunctionHTMLTemplateParser.class);

	private static NSMutableDictionary _tagShortcutMap = new NSMutableDictionary();
	private static NSMutableDictionary _tagProcessorMap = new NSMutableDictionary();
	private static boolean _allowInlineBindings = false;
	
	private static String WO_REPLACEMENT_MARKER = "__REPL__";
	
	public static void registerTagShortcut(String fullElementType, String shortcutElementType) {
		_tagShortcutMap.setObjectForKey(fullElementType, shortcutElementType);
	}

	public static void registerTagProcessorForElementType(WOTagProcessor tagProcessor, String elementType) {
		_tagProcessorMap.setObjectForKey(tagProcessor, elementType);
	}

	public static void setAllowInlineBindings(boolean allowInlineBindings) {
		_allowInlineBindings = allowInlineBindings;
	}
	
	static {
		WOHelperFunctionHTMLTemplateParser.log.setLevel(Level.WARN);

		WOHelperFunctionHTMLTemplateParser.registerTagShortcut("WOString", "string");
		WOHelperFunctionHTMLTemplateParser.registerTagShortcut("WOString", "str");
		WOHelperFunctionHTMLTemplateParser.registerTagShortcut("ERXElse", "else");
		WOHelperFunctionHTMLTemplateParser.registerTagShortcut("ERXWOConditional", "if");
		WOHelperFunctionHTMLTemplateParser.registerTagShortcut("ERXWOConditional", "condition");
		WOHelperFunctionHTMLTemplateParser.registerTagShortcut("ERXWOConditional", "conditional");
		WOHelperFunctionHTMLTemplateParser.registerTagShortcut("WOHyperlink", "link");
		WOHelperFunctionHTMLTemplateParser.registerTagShortcut("WORepetition", "loop");
		WOHelperFunctionHTMLTemplateParser.registerTagShortcut("WOTextField", "textfield");
		WOHelperFunctionHTMLTemplateParser.registerTagShortcut("WOCheckBox", "checkbox");
		WOHelperFunctionHTMLTemplateParser.registerTagShortcut("WOHiddenField", "hidden");
		WOHelperFunctionHTMLTemplateParser.registerTagShortcut("WOPopUpButton", "select");
		WOHelperFunctionHTMLTemplateParser.registerTagShortcut("WORadioButton", "radio");
		WOHelperFunctionHTMLTemplateParser.registerTagShortcut("WOPasswordField", "password");
		WOHelperFunctionHTMLTemplateParser.registerTagShortcut("WOFileUpload", "upload");
		WOHelperFunctionHTMLTemplateParser.registerTagShortcut("WOText", "text");
		WOHelperFunctionHTMLTemplateParser.registerTagShortcut("WOForm", "form");
		WOHelperFunctionHTMLTemplateParser.registerTagShortcut("WOSubmitButton", "submit");
		WOHelperFunctionHTMLTemplateParser.registerTagShortcut("ERXLocalizedString", "localized");
		
		WOHelperFunctionHTMLTemplateParser.registerTagProcessorForElementType(new NotTagProcessor(), "not");
	}

	private WOHTMLWebObjectTag _currentWebObjectTag;
	private NSMutableDictionary _declarations;
	private int _inlineBindingCount;

	public _WOHelperFunctionHTMLTemplateParser(String referenceName, String HTMLString, String declarationString, NSArray languages, Object associationFactory, Object namespaceProvider) {
		super(referenceName, HTMLString, declarationString, languages, null, null);
		_declarations = null;
		_currentWebObjectTag = new WOHTMLWebObjectTag();
	}
	
	public _WOHelperFunctionHTMLTemplateParser(String htmlString, String declarationString, NSArray languages) {
		super(htmlString, declarationString, languages);
		_declarations = null;
		_currentWebObjectTag = new WOHTMLWebObjectTag();
	}

	protected void parseInlineAssociation(StringBuffer keyBuffer, StringBuffer valueBuffer, NSMutableDictionary bindings) throws WOMiddleManHTMLFormatException {
		String key = keyBuffer.toString().trim();
		String value = valueBuffer.toString().trim();
		NSDictionary quotedStrings;
		if (value.startsWith("\"")) {
			value = value.substring(1);
			if (value.endsWith("\"")) {
				value = value.substring(0, value.length() - 1);
			}
			else {
				throw new WOMiddleManHTMLFormatException(valueBuffer + " starts with quote but does not end with one.");
			}
			if (value.startsWith("$")) {
				value = value.substring(1);
				if (value.endsWith("VALID")) {
					value = value.replaceFirst("\\s*//\\s*VALID", "");
				}
				quotedStrings = new NSDictionary();
			}
			else {
				value = value.replaceAll("\\\\\\$", "\\$");
				value = value.replaceAll("\\\"", "\"");
				quotedStrings = new NSDictionary(value, "_WODP_0");
				value = "_WODP_0";
			}
		}
		else {
			quotedStrings = new NSDictionary();
		}
		WOAssociation association = WOHelperFunctionDeclarationParser._associationWithKey(value, quotedStrings);
		bindings.setObjectForKey(association, key);
	}

	protected WODeclaration parseInlineBindings(String tag, int colonIndex) throws WOMiddleManHTMLFormatException {
		StringBuffer keyBuffer = new StringBuffer();
		StringBuffer valueBuffer = new StringBuffer();
		StringBuffer elementTypeBuffer = new StringBuffer();
		NSMutableDictionary associations = new NSMutableDictionary();
		StringBuffer currentBuffer = elementTypeBuffer;
		boolean changeBuffers = false;
		boolean inQuote = false;
		int length = tag.length();
		for (int index = colonIndex + 1; index < length; index++) {
			char ch = tag.charAt(index);
			if (!inQuote && (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r')) {
				changeBuffers = true;
			}
			else if (!inQuote && ch == '=') {
				changeBuffers = true;
			}
			else if (inQuote && ch == '\\') {
				index++;
				if (index == length) {
					throw new WOMiddleManHTMLFormatException("'" + tag + "' has a '\\' as the last character.");
				}
				if (tag.charAt(index) == '$') {
					currentBuffer.append("\\$");
				}
				else {
					currentBuffer.append(tag.charAt(index));
				}
			}
			else {
				if (changeBuffers) {
					if (currentBuffer == elementTypeBuffer) {
						currentBuffer = keyBuffer;
					}
					else if (currentBuffer == keyBuffer) {
						currentBuffer = valueBuffer;
					}
					else if (currentBuffer == valueBuffer) {
						parseInlineAssociation(keyBuffer, valueBuffer, associations);
						currentBuffer = keyBuffer;
					}
					currentBuffer.setLength(0);
					changeBuffers = false;
				}
				if (ch == '"') {
					inQuote = !inQuote;
				}
				currentBuffer.append(ch);
			}
		}
		if (inQuote) {
			throw new WOMiddleManHTMLFormatException("'" + tag + "' has a quote left open.");
		}
		if (keyBuffer.length() > 0) {
			if (valueBuffer.length() > 0) {
				parseInlineAssociation(keyBuffer, valueBuffer, associations);
			}
			else {
				throw new WOMiddleManHTMLFormatException("'" + tag + "' defines a key but no value.");
			}
		}
		String elementType = elementTypeBuffer.toString();
		String shortcutType = (String) _tagShortcutMap.objectForKey(elementType);
		if (shortcutType != null) {
			elementType = shortcutType;
		}
		else if (elementType.startsWith(WO_REPLACEMENT_MARKER)) {
			// Acts only on tags, where we have "dynamified" inside the tag parser
			// this takes the value found after the "wo:" part in the element and generates a WOGenericContainer with that value
			// as the elementName binding
			elementType = elementType.replaceAll(WO_REPLACEMENT_MARKER, "");
			associations.setObjectForKey(WOAssociation.associationWithValue(elementType), "elementName");
			elementType = "WOGenericContainer";
		}
		String elementName;
		synchronized (this) {
			elementName = "_" + elementType + "_" + _inlineBindingCount;
			_inlineBindingCount++;
		}
		WOTagProcessor tagProcessor = (WOTagProcessor) _tagProcessorMap.objectForKey(elementType);
		WODeclaration declaration;
		if (tagProcessor == null) {
			declaration = new WODeclaration(elementName, elementType, associations);
		}
		else {
			declaration = tagProcessor.createDeclaration(elementName, elementType, associations);
		}
		_declarations.setObjectForKey(declaration, elementName);
		processDeclaration(declaration);
		return declaration;
	}

	public void didParseOpeningWebObjectTag(String s, WOHelperFunctionHTMLParser htmlParser) throws WOMiddleManHTMLFormatException {
		if (_allowInlineBindings) {
			int spaceIndex = s.indexOf(' ');
			int colonIndex;
			if (spaceIndex != -1) {
				colonIndex = s.substring(0, spaceIndex).indexOf(':');
			}
			else {
				colonIndex = s.indexOf(':');
			}
			if (colonIndex != -1) {
				WODeclaration declaration = parseInlineBindings(s, colonIndex);
				s = "<wo name = \"" + declaration.name() + "\"";
			}
		}
		_currentWebObjectTag = new WOHTMLWebObjectTag(s, _currentWebObjectTag);
		if (log.isDebugEnabled()) {
			log.debug("inserted WebObject with Name '" + _currentWebObjectTag.name() + "'.");
		}
	}

	public void didParseClosingWebObjectTag(String s, WOHelperFunctionHTMLParser htmlParser) throws WOMiddleManDeclarationFormatException, WOMiddleManHTMLFormatException, ClassNotFoundException {
		WOHTMLWebObjectTag webobjectTag = _currentWebObjectTag.parentTag();
		if (_currentWebObjectTag == null || webobjectTag == null) {
			throw new WOMiddleManHTMLFormatException("<" + getClass().getName() + "> Unbalanced WebObject tags. Either there is an extra closing </WEBOBJECT> tag in the html template, or one of the opening <WEBOBJECT ...> tag has a typo (extra spaces between a < sign and a WEBOBJECT tag ?).");
		}
		WOElement element = _currentWebObjectTag.dynamicElement(_declarations, _languages);
		_currentWebObjectTag = webobjectTag;
		_currentWebObjectTag.addChildElement(element);
	}

	public void didParseComment(String comment, WOHelperFunctionHTMLParser htmlParser) {
		WOHTMLCommentString wohtmlcommentstring = new WOHTMLCommentString(comment);
		_currentWebObjectTag.addChildElement(wohtmlcommentstring);
	}

	public void didParseText(String text, WOHelperFunctionHTMLParser htmlParser) {
		_currentWebObjectTag.addChildElement(text);
	}

	private void parseDeclarations() throws WOMiddleManDeclarationFormatException {
		if (_declarations == null && _declarationString != null) {
			_declarations = WOHelperFunctionDeclarationParser.declarationsWithString(_declarationString);
		}
	}

	private WOElement parseHTML() throws WOMiddleManHTMLFormatException, WOMiddleManDeclarationFormatException, ClassNotFoundException {
		WOElement currentWebObjectTemplate = null;
		if (_HTMLString != null && _declarations != null) {
			WOHelperFunctionHTMLParser htmlParser = new WOHelperFunctionHTMLParser(this, _HTMLString);
			htmlParser.parseHTML();
			String webobjectTagName = _currentWebObjectTag.name();
			if (webobjectTagName != null) {
				throw new WOMiddleManHTMLFormatException("There is an unbalanced WebObjects tag named '" + webobjectTagName + "'.");
			}
			currentWebObjectTemplate = _currentWebObjectTag.template();
		}
		return currentWebObjectTemplate;
	}

	public WOElement parse() throws WOMiddleManDeclarationFormatException, WOMiddleManHTMLFormatException, ClassNotFoundException {
		parseDeclarations();
		for (Enumeration e = declarations().objectEnumerator(); e.hasMoreElements();) {
			WODeclaration declaration = (WODeclaration) e.nextElement();
			processDeclaration(declaration);
		}
		WOElement woelement = parseHTML();
		return woelement;
	}

	protected void processDeclaration(WODeclaration declaration) {
		NSMutableDictionary associations = (NSMutableDictionary) declaration.associations();
		Enumeration bindingNameEnum = associations.keyEnumerator();
		while (bindingNameEnum.hasMoreElements()) {
			String bindingName = (String) bindingNameEnum.nextElement();
			WOAssociation association = (WOAssociation) associations.valueForKey(bindingName);
			WOAssociation helperAssociation = parserHelperAssociation(association);
			if (helperAssociation != association) {
				associations.setObjectForKey(helperAssociation, bindingName);
			}
		}
		// This will replace constant associations with ognl associations
		// when needed.
		WOOgnl.factory().convertOgnlConstantAssociations(associations);
	}

	protected WOAssociation parserHelperAssociation(WOAssociation originalAssociation) {
		WOAssociation association = originalAssociation;
		String originalKeyPath = null;
		if (association instanceof WOKeyValueAssociation) {
			WOKeyValueAssociation kvAssociation = (WOKeyValueAssociation) association;
			originalKeyPath = kvAssociation.keyPath();
		}
		// else if (association instanceof WOConstantValueAssociation) {
		// WOConstantValueAssociation constantAssociation =
		// (WOConstantValueAssociation) association;
		// Object constantValue = constantAssociation.valueInComponent(null);
		// if (constantValue instanceof String) {
		// originalKeyPath = (String) constantValue;
		// }
		// }

		if (originalKeyPath != null) {
			int pipeIndex = originalKeyPath.indexOf('|');
			if (pipeIndex != -1) {
				String targetKeyPath = originalKeyPath.substring(0, pipeIndex).trim();
				String frameworkName = WOHelperFunctionRegistry.APP_FRAMEWORK_NAME;
				String helperFunctionName = originalKeyPath.substring(pipeIndex + 1).trim();
				String otherParams = null;
				int openParenIndex = helperFunctionName.indexOf('(');
				if (openParenIndex != -1) {
					int closeParenIndex = helperFunctionName.indexOf(')', openParenIndex + 1);
					otherParams = helperFunctionName.substring(openParenIndex + 1, closeParenIndex);
					helperFunctionName = helperFunctionName.substring(0, openParenIndex);
				}
				int helperFunctionDotIndex = helperFunctionName.indexOf('.');
				if (helperFunctionDotIndex != -1) {
					frameworkName = helperFunctionName.substring(0, helperFunctionDotIndex);
					helperFunctionName = helperFunctionName.substring(helperFunctionDotIndex + 1);
				}
				StringBuffer ognlKeyPath = new StringBuffer();
				ognlKeyPath.append("~");
				ognlKeyPath.append("@" + WOHelperFunctionRegistry.class.getName() + "@registry()._helperInstanceForFrameworkNamed(#this, \"");
				ognlKeyPath.append(helperFunctionName);
				ognlKeyPath.append("\", \"");
				ognlKeyPath.append(targetKeyPath);
				ognlKeyPath.append("\", \"");
				ognlKeyPath.append(frameworkName);
				ognlKeyPath.append("\").");
				ognlKeyPath.append(helperFunctionName);
				ognlKeyPath.append("(");
				ognlKeyPath.append(targetKeyPath);
				if (otherParams != null) {
					ognlKeyPath.append(",");
					ognlKeyPath.append(otherParams);
				}
				ognlKeyPath.append(")");
				if (log.isDebugEnabled()) {
					log.debug("Converted " + originalKeyPath + " into " + ognlKeyPath);
				}
				association = new WOConstantValueAssociation(ognlKeyPath.toString());
			}
		}
		return association;
	}

	public NSMutableDictionary declarations() {
		return _declarations;
	}

	public void setDeclarations(NSMutableDictionary value) {
		_declarations = value;
	}

	public String declarationString() {
		return _declarationString;
	}

	public void setDeclarationString(String value) {
		_declarationString = value;
	}
}
