package ognl.helperfunction;

import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.association.WOAssociationFactory;
import com.webobjects.appserver.parser.WOComponentTemplateParser;
import com.webobjects.appserver.parser.WOHTMLFormatException;
import com.webobjects.appserver.parser.declaration.WODeclarationFormatException;
import com.webobjects.appserver.parser.woml.WOMLNamespaceProvider;

public class WOHelperFunctionTemplateParser extends WOComponentTemplateParser {
	public static Logger log = Logger.getLogger(WOHelperFunctionTagRegistry.class);

	private WOHelperFunctionParser _delegate;

//	public WOHelperFunctionTemplateParser(String referenceName, String HTMLString, String declarationString, NSArray<String> languages, WOAssociationFactory associationFactory, WOMLNamespaceProvider namespaceProvider) {
//		super(referenceName, HTMLString, declarationString, languages, associationFactory, namespaceProvider);
//		_delegate = new WOHelperFunctionParser(HTMLString, declarationString, WOLocaleUtilities.localeListForLanguages(languages));
//	}
//
	public WOHelperFunctionTemplateParser(String frameworkName, String referenceName, String HTMLString, String declarationString, List<Locale> languages, WOAssociationFactory associationFactory, WOMLNamespaceProvider namespaceProvider) {
		super(frameworkName, referenceName, HTMLString, declarationString, languages, associationFactory, namespaceProvider);
		_delegate = new WOHelperFunctionParser(HTMLString, declarationString, languages);
	}

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
