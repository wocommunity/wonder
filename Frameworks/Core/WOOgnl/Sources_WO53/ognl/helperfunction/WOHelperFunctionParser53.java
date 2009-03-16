package ognl.helperfunction;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOElement;
import com.webobjects.appserver._private.WODeclarationFormatException;
import com.webobjects.appserver._private.WOHTMLFormatException;
import com.webobjects.appserver._private.WOParser;
import com.webobjects.foundation.NSArray;

public class WOHelperFunctionParser53 extends WOParser {
	public static Logger log = Logger.getLogger(WOHelperFunctionParser53.class);

	private WOHelperFunctionParser _delegate;

	public WOHelperFunctionParser53(String htmlString, String declarationString, NSArray languages) {
		super(htmlString, declarationString, languages);
		_delegate = new WOHelperFunctionParser(htmlString, declarationString, languages);
	}

	public WOElement parse() throws WODeclarationFormatException, WOHTMLFormatException, ClassNotFoundException {
		try {
			return _delegate.parse();
		}
		catch (WOHelperFunctionDeclarationFormatException e) {
			// LAME
			throw new WODeclarationFormatException(e.getMessage());
		}
		catch (WOHelperFunctionHTMLFormatException e) {
			// LAME
			throw new WOHTMLFormatException(e.getMessage());
		}
	}
}
