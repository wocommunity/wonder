package ognl.helperfunction.compatibility;

import com.webobjects.appserver.WOAssociationFactory;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver._private.WOParser;
import com.webobjects.appserver.parser.woml.WOMLNamespaceProvider;
import com.webobjects.foundation.NSArray;

public abstract class WOMiddleManParser extends WOParser {
	public WOMiddleManParser(String referenceName, String HTMLString, String declarationString, NSArray languages, WOAssociationFactory associationFactory, WOMLNamespaceProvider namespaceProvider) {
		super(HTMLString, declarationString, languages);
		throw new IllegalStateException("This should never be called in WO 5.3");
	}
	
	public WOMiddleManParser(String htmlString, String declarationString, NSArray languages) {
		super(htmlString, declarationString, languages);
	}

	public abstract WOElement parse() throws WOMiddleManDeclarationFormatException, WOMiddleManHTMLFormatException, ClassNotFoundException;
	
	public static void setWOHTMLTemplateParserClassName(String parserClassName) {
		WOParser.setWOHTMLTemplateParserClassName(parserClassName);
	}
}