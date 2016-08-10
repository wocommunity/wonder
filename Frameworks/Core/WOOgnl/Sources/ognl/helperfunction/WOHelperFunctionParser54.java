package ognl.helperfunction;

import com.webobjects.appserver.WOAssociationFactory;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.parser.WOComponentTemplateParser;
import com.webobjects.appserver.parser.WOHTMLFormatException;
import com.webobjects.appserver.parser.declaration.WODeclarationFormatException;
import com.webobjects.appserver.parser.woml.WOMLNamespaceProvider;
import com.webobjects.foundation.NSArray;

public class WOHelperFunctionParser54 extends WOComponentTemplateParser {
	private WOHelperFunctionParser _delegate;

	public WOHelperFunctionParser54(String referenceName, String HTMLString, String declarationString, NSArray languages, WOAssociationFactory associationFactory, WOMLNamespaceProvider namespaceProvider) {
		super(referenceName, HTMLString, declarationString, languages, associationFactory, namespaceProvider);
		_delegate = new WOHelperFunctionParser(HTMLString, declarationString, languages);
	}

	@Override
	public WOElement parse() throws WODeclarationFormatException, WOHTMLFormatException, ClassNotFoundException {
		try {
			return _delegate.parse();
		}
		catch (WOHelperFunctionDeclarationFormatException e) {
			// LAME
			throw new WODeclarationFormatException(e.getMessage(), e);
		}
		catch (WOHelperFunctionHTMLFormatException e) {
			// LAME
			throw new WOHTMLFormatException(e.getMessage());
		}
	}
}
