package ognl.helperfunction;

import java.util.Enumeration;

import ognl.webobjects.WOOgnl;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver._private.WOConstantValueAssociation;
import com.webobjects.appserver._private.WODeclaration;
import com.webobjects.appserver._private.WOHTMLAttribute;
import com.webobjects.appserver._private.WOHTMLCommentString;
import com.webobjects.appserver._private.WOKeyValueAssociation;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

public class WOHelperFunctionParser {
	public static Logger log = Logger.getLogger(WOHelperFunctionParser.class);

	public static boolean _debugSupport;

	private static String WO_REPLACEMENT_MARKER = "__REPL__";

	private WOHTMLWebObjectTag _currentWebObjectTag;
	private NSMutableDictionary _declarations;
	private int _inlineBindingCount;

	private String _declarationString;
	private String _HTMLString;
	private NSArray _languages;

	public WOHelperFunctionParser(String htmlString, String declarationString, NSArray languages) {
		_HTMLString = htmlString;
		_declarationString = declarationString;
		_languages = languages;
		_declarations = null;
		_currentWebObjectTag = new WOHTMLWebObjectTag();
	}

	public WOElement parse() throws WOHelperFunctionDeclarationFormatException, WOHelperFunctionHTMLFormatException, ClassNotFoundException {
		parseDeclarations();
		for (Enumeration e = declarations().objectEnumerator(); e.hasMoreElements();) {
			WODeclaration declaration = (WODeclaration) e.nextElement();
			processDeclaration(declaration);
		}
		WOElement woelement = parseHTML();
		return woelement;
	}

	public void didParseOpeningWebObjectTag(String s, WOHelperFunctionHTMLParser htmlParser) throws WOHelperFunctionHTMLFormatException {
		if (WOHelperFunctionTagRegistry.allowInlineBindings()) {
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

	public void didParseClosingWebObjectTag(String s, WOHelperFunctionHTMLParser htmlParser) throws WOHelperFunctionDeclarationFormatException, WOHelperFunctionHTMLFormatException, ClassNotFoundException {
		WOHTMLWebObjectTag webobjectTag = _currentWebObjectTag.parentTag();
		if (webobjectTag == null) {
			throw new WOHelperFunctionHTMLFormatException("<" + getClass().getName() + "> Unbalanced WebObject tags. Either there is an extra closing </WEBOBJECT> tag in the html template, or one of the opening <WEBOBJECT ...> tag has a typo (extra spaces between a < sign and a WEBOBJECT tag ?).");
		}
		try {
			WOElement element = _currentWebObjectTag.dynamicElement(_declarations, _languages);
			_currentWebObjectTag = webobjectTag;
			_currentWebObjectTag.addChildElement(element);
		}
		catch (RuntimeException e) {
			throw new RuntimeException("Unable to load the component named '" + componentName(_currentWebObjectTag) + "' with the declaration " + prettyDeclaration((WODeclaration) _declarations.objectForKey(_currentWebObjectTag.name())) + ". Make sure the .wo folder is where it's supposed to be and the name is spelled correctly.", e);
		}
	}

	public void didParseComment(String comment, WOHelperFunctionHTMLParser htmlParser) {
		WOHTMLCommentString wohtmlcommentstring = new WOHTMLCommentString(comment);
		_currentWebObjectTag.addChildElement(wohtmlcommentstring);
	}

	public void didParseText(String text, WOHelperFunctionHTMLParser htmlParser) {
		_currentWebObjectTag.addChildElement(text);
	}

	protected WODeclaration parseInlineBindings(String tag, int colonIndex) throws WOHelperFunctionHTMLFormatException {
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
					throw new WOHelperFunctionHTMLFormatException("'" + tag + "' has a '\\' as the last character.");
				}
				if (tag.charAt(index) == '\"') {
					currentBuffer.append("\"");
				}
				else if (tag.charAt(index) == 'n') {
					currentBuffer.append("\n");
				}
				else if (tag.charAt(index) == 'r') {
					currentBuffer.append("\r");
				}
				else if (tag.charAt(index) == 't') {
					currentBuffer.append("\t");
				}
				else {
					currentBuffer.append('\\');
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
			throw new WOHelperFunctionHTMLFormatException("'" + tag + "' has a quote left open.");
		}

		if (keyBuffer.length() > 0) {
			if (valueBuffer.length() > 0) {
				parseInlineAssociation(keyBuffer, valueBuffer, associations);
			}
			else {
				throw new WOHelperFunctionHTMLFormatException("'" + tag + "' defines a key but no value.");
			}
		}
		String elementType = elementTypeBuffer.toString();
		String shortcutType = (String) WOHelperFunctionTagRegistry.tagShortcutMap().objectForKey(elementType);
		if (shortcutType != null) {
			elementType = shortcutType;
		}
		else if (elementType.startsWith(WO_REPLACEMENT_MARKER)) {
			// Acts only on tags, where we have "dynamified" inside the tag parser
			// this takes the value found after the "wo:" part in the element and generates a WOGenericContainer with that value
			// as the elementName binding
			elementType = elementType.replaceAll(WO_REPLACEMENT_MARKER, "");
			associations.setObjectForKey(WOHelperFunctionAssociation.associationWithValue(elementType), "elementName");
			elementType = "WOGenericContainer";
		}
		String elementName;
		synchronized (this) {
			elementName = "_" + elementType + "_" + _inlineBindingCount;
			_inlineBindingCount++;
		}
		WOTagProcessor tagProcessor = (WOTagProcessor) WOHelperFunctionTagRegistry.tagProcessorMap().objectForKey(elementType);
		WODeclaration declaration;
		if (tagProcessor == null) {
			declaration = WOHelperFunctionParser.createDeclaration(elementName, elementType, associations);
		}
		else {
			declaration = tagProcessor.createDeclaration(elementName, elementType, associations);
		}
		_declarations.setObjectForKey(declaration, elementName);
		processDeclaration(declaration);
		return declaration;
	}

	protected void parseInlineAssociation(StringBuffer keyBuffer, StringBuffer valueBuffer, NSMutableDictionary bindings) throws WOHelperFunctionHTMLFormatException {
		String key = keyBuffer.toString().trim();
		String value = valueBuffer.toString().trim();
		NSDictionary quotedStrings;
		if (value.startsWith("\"")) {
			value = value.substring(1);
			if (value.endsWith("\"")) {
				value = value.substring(0, value.length() - 1);
			}
			else {
				throw new WOHelperFunctionHTMLFormatException(valueBuffer + " starts with quote but does not end with one.");
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
				StringBuilder ognlKeyPath = new StringBuilder();
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

	protected String prettyDeclaration(WODeclaration declaration) {
		StringBuilder declarationStr = new StringBuilder();
		if (declaration == null) {
			declarationStr.append("[none]");
		}
		else {
			declarationStr.append("Component Type = " + declaration.type());
			declarationStr.append(", Bindings = { ");
			Enumeration keyEnum = declaration.associations().keyEnumerator();
			while (keyEnum.hasMoreElements()) {
				String key = (String) keyEnum.nextElement();
				Object assoc = declaration.associations().objectForKey(key);
				if (assoc instanceof WOKeyValueAssociation) {
					declarationStr.append(key + "=" + ((WOKeyValueAssociation) assoc).keyPath());
				}
				else if (assoc instanceof WOConstantValueAssociation) {
					declarationStr.append(key + "='" + ((WOConstantValueAssociation) assoc).valueInComponent(null) + "'");
				}
				else {
					declarationStr.append(key + "=" + assoc);
				}
				if (keyEnum.hasMoreElements()) {
					declarationStr.append(", ");
				}
			}
			declarationStr.append(" }");
		}
		return declarationStr.toString();
	}

	private WOElement parseHTML() throws WOHelperFunctionHTMLFormatException, WOHelperFunctionDeclarationFormatException, ClassNotFoundException {
		WOElement currentWebObjectTemplate = null;
		if (_HTMLString != null && _declarations != null) {
			WOHelperFunctionHTMLParser htmlParser = new WOHelperFunctionHTMLParser(this, _HTMLString);
			htmlParser.parseHTML();
			String webobjectTagName = _currentWebObjectTag.name();
			if (webobjectTagName != null) {
				throw new WOHelperFunctionHTMLFormatException("There is an unbalanced WebObjects tag named '" + webobjectTagName + "'.");
			}
			currentWebObjectTemplate = _currentWebObjectTag.template();
		}
		return currentWebObjectTemplate;
	}

	protected boolean isInline(WOHTMLWebObjectTag tag) {
		String name = tag.name();
		return name != null && name.startsWith("_") && name.length() > 1 && name.indexOf('_', 1) != -1;
	}

	protected String componentName(WOHTMLWebObjectTag tag) {
		String name = tag.name();
		// This goofiness reparses back out inline binding names
		if (name == null) {
			name = "[none]";
		}
		else if (isInline(tag)) {
			int secondUnderscoreIndex = name.indexOf('_', 1);
			if (secondUnderscoreIndex != -1) {
				name = name.substring(1, secondUnderscoreIndex);
			}
		}
		return name;
	}

	public String declarationString() {
		return _declarationString;
	}

	public void setDeclarationString(String value) {
		_declarationString = value;
	}

	public NSMutableDictionary declarations() {
		return _declarations;
	}

	public void setDeclarations(NSMutableDictionary value) {
		_declarations = value;
	}

	private void parseDeclarations() throws WOHelperFunctionDeclarationFormatException {
		if (_declarations == null && _declarationString != null) {
			_declarations = WOHelperFunctionDeclarationParser.declarationsWithString(_declarationString);
		}
	}

	public static WODeclaration createDeclaration(String declarationName, String declarationType, NSMutableDictionary associations) {
		WODeclaration declaration = new WODeclaration(declarationName, declarationType, associations);

		if (WOHelperFunctionParser._debugSupport && associations != null && associations.objectForKey(WOHTMLAttribute.Debug) == null) {
			//associations.setObjectForKey(new WOConstantValueAssociation(Boolean.TRUE), WOHTMLAttribute.Debug);
			Enumeration associationsEnum = associations.keyEnumerator();
			while (associationsEnum.hasMoreElements()) {
				String bindingName = (String) associationsEnum.nextElement();
				WOAssociation association = (WOAssociation) associations.objectForKey(bindingName);
				association.setDebugEnabledForBinding(bindingName, declarationName, declarationType);
				association._setDebuggingEnabled(false);
			}
		}

		return declaration;
	}
}
