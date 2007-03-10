package ognl.helperfunction;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.webobjects.appserver._private.WODeclarationFormatException;
import com.webobjects.appserver._private.WOHTMLFormatException;
import com.webobjects.foundation._NSStringUtilities;

public class WOHelperFunctionHTMLParser {
	public static Logger log = Logger.getLogger(WOHelperFunctionHTMLParser.class);

	private WOHelperFunctionHTMLParserDelegate _parserDelegate;
	private String _unparsedTemplate;
	private StringBuffer _contentText;
	private static final int STATE_OUTSIDE = 0;
	private static final int STATE_INSIDE_COMMENT = 3;
	private static final String JS_START_TAG = "<script";
	private static final String JS_END_TAG = "</script";
	private static final String WO_END_TAG = "</wo";
	private static final String WO_START_TAG = "<wo";
	private static final String WEBOBJECT_END_TAG = "</webobject";
	private static final String WEBOBJECT_START_TAG = "<webobject";

	static {
		WOHelperFunctionHTMLParser.log.setLevel(Level.WARN);
	}

	public WOHelperFunctionHTMLParser(WOHelperFunctionHTMLParserDelegate parserDelegate, String unparsedTemplate) {
		_parserDelegate = parserDelegate;
		_unparsedTemplate = unparsedTemplate;
		_contentText = new StringBuffer(128);
	}

	public void parseHTML() throws WOHTMLFormatException, WODeclarationFormatException, ClassNotFoundException {
		Object obj = null;
		StringTokenizer templateTokenizer = new StringTokenizer(_unparsedTemplate, "<");
		boolean flag = true;
		int parserState = STATE_OUTSIDE;
		String token;
		if (_unparsedTemplate.startsWith("<") || !templateTokenizer.hasMoreTokens()) {
			token = null;
		}
		else {
			token = templateTokenizer.nextToken("<");
		}
		try {
			do {
				if (!templateTokenizer.hasMoreTokens()) {
					break;
				}
				switch (parserState) {
				case STATE_OUTSIDE:
					if (token != null) {
						if (token.startsWith(">")) {
							token = token.substring(1);
						}
						_contentText.append(token);
					}
					token = templateTokenizer.nextToken(">");
					int tagIndex;
					String tagLowerCase = token.toLowerCase();
					if (tagLowerCase.startsWith(WOHelperFunctionHTMLParser.WEBOBJECT_START_TAG) || tagLowerCase.startsWith(WOHelperFunctionHTMLParser.WO_START_TAG)) {
						if (token.endsWith("/")) {
							startOfWebObjectTag(token.substring(0, token.length() - 1));
							endOfWebObjectTag("/");
						}
						else {
							startOfWebObjectTag(token);
						}
					}
					else if ((tagIndex = tagLowerCase.indexOf(WOHelperFunctionHTMLParser.WEBOBJECT_START_TAG)) > 1 || (tagIndex = tagLowerCase.indexOf(WOHelperFunctionHTMLParser.WO_START_TAG)) > 1) {
						_contentText.append(token.substring(0, token.lastIndexOf("<")));
						if (token.endsWith("/")) {
							startOfWebObjectTag(token.substring(tagIndex, token.length() - 1));
							endOfWebObjectTag("/");
						}
						else {
							startOfWebObjectTag(token.substring(tagIndex, token.length()));
						}
					}
					else if (tagLowerCase.startsWith(WOHelperFunctionHTMLParser.WEBOBJECT_END_TAG) || tagLowerCase.startsWith(WOHelperFunctionHTMLParser.WO_END_TAG)) {
						endOfWebObjectTag(token);
					}
					else if (tagLowerCase.startsWith(WOHelperFunctionHTMLParser.JS_START_TAG)) {
						didParseText();
						_contentText.append(token);
						_contentText.append('>');
						flag = false;
					}
					else if (tagLowerCase.startsWith(WOHelperFunctionHTMLParser.JS_END_TAG)) {
						didParseText();
						_contentText.append(token);
						_contentText.append('>');
						flag = true;
					}
					else if (token.startsWith("<!--") && flag) {
						didParseText();
						_contentText.append(token);
						if (token.endsWith("--")) {
							_contentText.append('>');
							didParseComment();
						}
						else {
							parserState = STATE_INSIDE_COMMENT;
						}
					}
					else {
						_contentText.append(token);
						_contentText.append('>');
					}
					break;

				case STATE_INSIDE_COMMENT:
					token = templateTokenizer.nextToken(">");
					_contentText.append(token);
					_contentText.append('>');
					if (token.endsWith("--")) {
						didParseComment();
						parserState = STATE_OUTSIDE;
					}
					break;

				default:
					break;
				}
				token = null;
				if (parserState == STATE_OUTSIDE) {
					token = templateTokenizer.nextToken("<");
				}
			}
			while (true);
		}
		catch (NoSuchElementException e) {
			log.error(e);
			didParseText();
			return;
		}
		if (token != null) {
			if (token.startsWith(">")) {
				token = token.substring(1);
			}
			_contentText.append(token);
		}
		didParseText();
	}

	private void startOfWebObjectTag(String token) throws WOHTMLFormatException {
		didParseText();
		_contentText.append(token);
		didParseOpeningWebObjectTag();
	}

	private void endOfWebObjectTag(String token) throws WODeclarationFormatException, WOHTMLFormatException, ClassNotFoundException {
		didParseText();
		_contentText.append(token);
		didParseClosingWebObjectTag();
	}

	private void didParseText() {
		if (_contentText != null) {
			if (log.isDebugEnabled()) {
				log.debug("Parsed Text (" + _contentText.length() + ") : " + _contentText);
			}
			if (_contentText.length() > 0) {
				_parserDelegate.didParseText(_NSStringUtilities.stringFromBuffer(_contentText), this);
				_contentText.setLength(0);
			}
		}
	}

	private void didParseOpeningWebObjectTag() throws WOHTMLFormatException {
		if (_contentText != null) {
			if (log.isDebugEnabled()) {
				log.debug("Parsed Opening WebObject (" + _contentText.length() + ") : " + _contentText);
			}
			if (_contentText.length() > 0) {
				_parserDelegate.didParseOpeningWebObjectTag(_NSStringUtilities.stringFromBuffer(_contentText), this);
				_contentText.setLength(0);
			}
		}
	}

	private void didParseClosingWebObjectTag() throws WODeclarationFormatException, WOHTMLFormatException, ClassNotFoundException, ClassNotFoundException {
		if (_contentText != null) {
			if (log.isDebugEnabled()) {
				log.debug("Parsed Closing WebObject (" + _contentText.length() + ") : " + _contentText);
			}
			if (_contentText.length() > 0) {
				_parserDelegate.didParseClosingWebObjectTag(_NSStringUtilities.stringFromBuffer(_contentText), this);
				_contentText.setLength(0);
			}
		}
	}

	private void didParseComment() {
		if (_contentText != null) {
			if (log.isDebugEnabled()) {
				log.debug("Parsed Comment (" + _contentText.length() + ") : " + _contentText);
			}
			if (_contentText.length() > 0) {
				_parserDelegate.didParseComment(_NSStringUtilities.stringFromBuffer(_contentText), this);
				_contentText.setLength(0);
			}
		}
	}
}
