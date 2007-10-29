package ognl.helperfunction;
import com.webobjects.appserver.WOAssociationFactory;
import com.webobjects.appserver.parser.woml.WOMLNamespaceProvider;
import com.webobjects.foundation.NSArray;

public class WOHelperFunctionHTMLTemplateParser extends _WOHelperFunctionHTMLTemplateParser {
	public WOHelperFunctionHTMLTemplateParser(String referenceName, String HTMLString, String declarationString, NSArray languages, WOAssociationFactory associationFactory, WOMLNamespaceProvider namespaceProvider) {
		super(referenceName, HTMLString, declarationString, languages, associationFactory, namespaceProvider);
	}
}
