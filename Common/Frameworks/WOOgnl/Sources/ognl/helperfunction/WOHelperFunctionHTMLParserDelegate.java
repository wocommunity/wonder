package ognl.helperfunction;

import com.webobjects.appserver._private.WODeclarationFormatException;
import com.webobjects.appserver._private.WOHTMLFormatException;

public interface WOHelperFunctionHTMLParserDelegate {

	public void didParseOpeningWebObjectTag(String content, WOHelperFunctionHTMLParser htmlParser) throws WOHTMLFormatException;

	public void didParseClosingWebObjectTag(String content, WOHelperFunctionHTMLParser htmlParser) throws WODeclarationFormatException, WOHTMLFormatException, ClassNotFoundException;

	public void didParseComment(String comment, WOHelperFunctionHTMLParser htmlParser);

	public void didParseText(String text, WOHelperFunctionHTMLParser htmlParser);
}
