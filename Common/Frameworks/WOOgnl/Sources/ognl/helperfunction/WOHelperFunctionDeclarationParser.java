package ognl.helperfunction;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import ognl.helperfunction.compatibility.WOMiddleManDeclarationFormatException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver._private.WOConstantValueAssociation;
import com.webobjects.appserver._private.WODeclaration;
import com.webobjects.appserver._private.WOShared;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation._NSStringUtilities;

public class WOHelperFunctionDeclarationParser {
	public static Logger log = Logger.getLogger(WOHelperFunctionDeclarationParser.class);

	private NSMutableDictionary _quotedStrings;
	private static final int STATE_OUTSIDE = 0;
	private static final int STATE_INSIDE_COMMENT = 2;
	private static final String ESCAPED_QUOTE_STRING = "_WO_ESCAPED_QUOTE_";
	private static final String QUOTED_STRING_KEY = "_WODP_";

	static {
		WOHelperFunctionDeclarationParser.log.setLevel(Level.WARN);
	}

	public WOHelperFunctionDeclarationParser() {
		_quotedStrings = new NSMutableDictionary();
	}

	public static NSMutableDictionary declarationsWithString(String declarationStr) throws WOMiddleManDeclarationFormatException {
		WOHelperFunctionDeclarationParser declarationParser = new WOHelperFunctionDeclarationParser();
		NSMutableDictionary declarations = declarationParser.parseDeclarations(declarationStr);
		return declarations;
	}

	public String toString() {
		return "<WOHelperFunctionDeclarationParser quotedStrings = " + _quotedStrings.toString() + ">";
	}

	public NSMutableDictionary parseDeclarations(String declarationStr) throws WOMiddleManDeclarationFormatException {
		String strWithoutComments = _removeOldStyleCommentsFromString(declarationStr);
		strWithoutComments = _removeNewStyleCommentsAndQuotedStringsFromString(strWithoutComments);
		NSMutableDictionary declarations = parseDeclarationsWithoutComments(strWithoutComments);
		return declarations;
	}

	private String _removeOldStyleCommentsFromString(String str) {
		StringBuffer stringbuffer = new StringBuffer(100);
		StringBuffer stringbuffer1 = new StringBuffer(100);
		StringTokenizer tokenizer = new StringTokenizer(str, "/", true);
		int state = WOHelperFunctionDeclarationParser.STATE_OUTSIDE;
		try {
			do {
				if (!tokenizer.hasMoreTokens()) {
					break;
				}
				String token = tokenizer.nextToken();
				switch (state) {
				case STATE_OUTSIDE:
					if (token.equals("/")) {
						token = tokenizer.nextToken();
						if (token.startsWith("*")) {
							state = WOHelperFunctionDeclarationParser.STATE_INSIDE_COMMENT;
							stringbuffer1.append('/');
							stringbuffer1.append(token);
						}
						else {
							stringbuffer.append('/');
							stringbuffer.append(token);
						}
					}
					else {
						stringbuffer.append(token);
					}
					break;

				case STATE_INSIDE_COMMENT:
					stringbuffer1.append(token);
					String s2 = new String(stringbuffer1);
					if (s2.endsWith("*/") && !s2.equals("/*/")) {
						state = WOHelperFunctionDeclarationParser.STATE_OUTSIDE;
					}
					break;
				}
			}
			while (true);
		}
		catch (NoSuchElementException e) {
			log.debug("Parsing failed.", e);
		}
		return _NSStringUtilities.stringFromBuffer(stringbuffer);
	}

	private String _removeNewStyleCommentsAndQuotedStringsFromString(String declarationsStr) {
		String escapedQuoteStr = _NSStringUtilities.replaceAllInstancesOfString(declarationsStr, "\\\"", WOHelperFunctionDeclarationParser.ESCAPED_QUOTE_STRING);
		StringBuffer declarationWithoutCommentsBuffer = new StringBuffer(100);
		// StringBuffer stringbuffer1 = new StringBuffer(100);
		StringTokenizer tokenizer = new StringTokenizer(escapedQuoteStr, "/\"", true);
		try {
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken("/\"");
				if (token.equals("/")) {
					token = tokenizer.nextToken("\n");
					if (token.startsWith("/")) {
						token = _NSStringUtilities.replaceAllInstancesOfString(token, WOHelperFunctionDeclarationParser.ESCAPED_QUOTE_STRING, "\\\"");
						// stringbuffer1.append('/');
						// stringbuffer1.append(token);
						// stringbuffer1.append('\n');
						declarationWithoutCommentsBuffer.append('\n');
						tokenizer.nextToken();
					}
					else {
						declarationWithoutCommentsBuffer.append('/');
						declarationWithoutCommentsBuffer.append(token);
					}
				}
				else if (token.equals("\"")) {
					token = tokenizer.nextToken("\"");
					if (token.equals("\"")) {
						token = "";
					}
					else {
						tokenizer.nextToken();
					}
					String quotedStringKey = WOHelperFunctionDeclarationParser.QUOTED_STRING_KEY + _quotedStrings.count();
					if (NSLog.debugLoggingAllowedForLevelAndGroups(3, 0x0L)) {
						NSLog.debug.appendln("Found a quoted string: " + quotedStringKey + "='" + token + "';");
					}
					token = _NSStringUtilities.replaceAllInstancesOfString(token, WOHelperFunctionDeclarationParser.ESCAPED_QUOTE_STRING, "\"");
					_quotedStrings.setObjectForKey(token, quotedStringKey);
					declarationWithoutCommentsBuffer.append(quotedStringKey);
				}
				else {
					declarationWithoutCommentsBuffer.append(token);
				}
			}
		}
		catch (NoSuchElementException e) {
			log.debug("Parsing failed.", e);
		}
		return _NSStringUtilities.stringFromBuffer(declarationWithoutCommentsBuffer);
	}

	private NSMutableDictionary parseDeclarationsWithoutComments(String declarationWithoutComment) throws WOMiddleManDeclarationFormatException {
		NSMutableDictionary declarations = new NSMutableDictionary();
		NSMutableDictionary rawDeclarations = _rawDeclarationsWithoutComment(declarationWithoutComment);
		String tagName;
		WODeclaration declaration;
		Enumeration rawDeclarationHeaderEnum = rawDeclarations.keyEnumerator();
		while (rawDeclarationHeaderEnum.hasMoreElements()) {
			String declarationHeader = (String) rawDeclarationHeaderEnum.nextElement();
			String declarationBody = (String) rawDeclarations.objectForKey(declarationHeader);
			int colonIndex = declarationHeader.indexOf(':');
			if (colonIndex < 0) {
				throw new WOMiddleManDeclarationFormatException("<WOHelperFunctionDeclarationParser> Missing ':' for declaration:\n" + declarationHeader + " " + declarationBody);
			}
			tagName = declarationHeader.substring(0, colonIndex).trim();
			if (tagName.length() == 0) {
				throw new WOMiddleManDeclarationFormatException("<WOHelperFunctionDeclarationParser> Missing tag name for declaration:\n" + declarationHeader + " " + declarationBody);
			}
			if (declarations.objectForKey(tagName) != null) {
				throw new WOMiddleManDeclarationFormatException("<WOHelperFunctionDeclarationParser> Duplicate tag name '" + tagName + "' in declaration:\n" + declarationBody);
			}
			String type = declarationHeader.substring(colonIndex + 1).trim();
			if (type.length() == 0) {
				throw new WOMiddleManDeclarationFormatException("<WOHelperFunctionDeclarationParser> Missing element name for declaration:\n" + declarationHeader + " " + declarationBody);
			}
			NSMutableDictionary associations = _associationsForDictionaryString(declarationHeader, declarationBody);
			declaration = new WODeclaration(tagName, type, associations);
			declarations.setObjectForKey(declaration, tagName);
		}

		return declarations;
	}

	private NSMutableDictionary _associationsForDictionaryString(String declarationHeader, String declarationBody) throws WOMiddleManDeclarationFormatException {
		NSMutableDictionary associations = new NSMutableDictionary();
		String trimmedDeclarationBody = declarationBody.trim();
		if (!trimmedDeclarationBody.startsWith("{") && !trimmedDeclarationBody.endsWith("}")) {
			throw new WOMiddleManDeclarationFormatException("<WOHelperFunctionDeclarationParser> Internal inconsistency : invalid dictionary for declaration:\n" + declarationHeader + " " + declarationBody);
		}
		int declarationBodyLength = trimmedDeclarationBody.length();
		if (declarationBodyLength <= 2) {
			return associations;
		}
		trimmedDeclarationBody = trimmedDeclarationBody.substring(1, declarationBodyLength - 1).trim();
		NSArray bindings = NSArray.componentsSeparatedByString(trimmedDeclarationBody, ";");
		Enumeration bindingsEnum = bindings.objectEnumerator();
		do {
			if (!bindingsEnum.hasMoreElements()) {
				break;
			}
			String binding = ((String) bindingsEnum.nextElement()).trim();
			if (binding.length() != 0) {
				int equalsIndex = binding.indexOf('=');
				if (equalsIndex < 0) {
					throw new WOMiddleManDeclarationFormatException("<WOHelperFunctionDeclarationParser> Invalid line. No equal in line:\n" + binding + "\nfor declaration:\n" + declarationHeader + " " + declarationBody);
				}
				String key = binding.substring(0, equalsIndex).trim();
				if (key.length() == 0) {
					throw new WOMiddleManDeclarationFormatException("<WOHelperFunctionDeclarationParser> Missing binding in line:\n" + binding + "\nfor declaration:\n" + declarationHeader + " " + declarationBody);
				}
				String value = binding.substring(equalsIndex + 1).trim();
				if (value.length() == 0) {
					throw new WOMiddleManDeclarationFormatException("<WOHelperFunctionDeclarationParser> Missing value in line:\n" + binding + "\nfor declaration:\n" + declarationHeader + " " + declarationBody);
				}
				WOAssociation association = WOHelperFunctionDeclarationParser._associationWithKey(value, _quotedStrings);
				Object quotedString = _quotedStrings.objectForKey(key);
				if (quotedString != null) {
					associations.setObjectForKey(association, quotedString);
				}
				else {
					associations.setObjectForKey(association, key);
				}
			}
		}
		while (true);
		// if (log.isDebugEnabled()) {
		// log.debug("Parsed '" + s + "' declarations:\n" + nsmutabledictionary
		// + "\n--------");
		// }
		return associations;
	}

	public static WOAssociation _associationWithKey(String associationValue, NSDictionary quotedStrings) {
		WOAssociation association = null;
		if (associationValue != null && associationValue.startsWith("~")) {
			int associationValueLength = associationValue.length();
			StringBuffer ognlValue = new StringBuffer();
			int lastIndex = 0;
			int index = 0;
			while ((index = associationValue.indexOf(WOHelperFunctionDeclarationParser.QUOTED_STRING_KEY, lastIndex)) != -1) {
				ognlValue.append(associationValue.substring(lastIndex, index));
				int wodpValueStartIndex = index + WOHelperFunctionDeclarationParser.QUOTED_STRING_KEY.length();
				int wodpValueEndIndex = wodpValueStartIndex;
				for (; wodpValueEndIndex < associationValueLength && Character.isDigit(associationValue.charAt(wodpValueEndIndex)); wodpValueEndIndex++) {
					// do nothing
				}
				String wodpKey = WOHelperFunctionDeclarationParser.QUOTED_STRING_KEY + associationValue.substring(wodpValueStartIndex, wodpValueEndIndex);
				String quotedString = (String) quotedStrings.objectForKey(wodpKey);
				if (quotedString != null) {
					quotedString = quotedString.replaceAll("\\\"", "\\\\\"");
					ognlValue.append("\"");
					ognlValue.append(quotedString);
					ognlValue.append("\"");
				}
				lastIndex = wodpValueEndIndex;
			}
			ognlValue.append(associationValue.substring(lastIndex));
			associationValue = ognlValue.toString();
			association = WOAssociation.associationWithValue(associationValue);
		}
		else {
			String quotedString = (String) quotedStrings.objectForKey(associationValue);
			if (quotedString != null) {
				association = WOAssociation.associationWithValue(quotedString);
			}
			else if (_NSStringUtilities.isNumber(associationValue)) {
				Integer integer = WOShared.unsignedIntNumber(Integer.parseInt(associationValue));
				association = WOAssociation.associationWithValue(integer);
			}
			else if (associationValue.equalsIgnoreCase("true") || associationValue.equalsIgnoreCase("yes")) {
				association = WOConstantValueAssociation.TRUE;
			}
			else if (associationValue.equalsIgnoreCase("false") || associationValue.equalsIgnoreCase("no") || associationValue.equalsIgnoreCase("nil") || associationValue.equalsIgnoreCase("null")) {
				association = WOConstantValueAssociation.FALSE;
			}
			else {
				association = WOAssociation.associationWithKeyPath(associationValue);
			}
		}
		return association;
	}

	private NSMutableDictionary _rawDeclarationsWithoutComment(String declarationStr) {
		NSMutableDictionary declarations = new NSMutableDictionary();
		StringBuffer declarationWithoutCommentBuffer = new StringBuffer(100);
		StringTokenizer tokenizer = new StringTokenizer(declarationStr, "{", true);
		try {
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken("{");
				if (token.equals("{")) {
					token = tokenizer.nextToken("}");
					if (token.equals("}")) {
						token = "";
					}
					else {
						tokenizer.nextToken();
					}
					String declarationWithoutComment = _NSStringUtilities.stringFromBuffer(declarationWithoutCommentBuffer);
					if (declarationWithoutComment.startsWith(";")) {
						declarationWithoutComment = declarationWithoutComment.substring(1);
					}
					declarations.setObjectForKey("{" + token + "}", declarationWithoutComment.trim());
					declarationWithoutCommentBuffer.setLength(0);
				}
				else {
					declarationWithoutCommentBuffer.append(token);
				}
			}
		}
		catch (NoSuchElementException e) {
			log.debug("Failed to parse.", e);
		}
		return declarations;
	}
}
