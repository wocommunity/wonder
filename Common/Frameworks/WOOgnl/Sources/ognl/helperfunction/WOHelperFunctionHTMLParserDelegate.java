package ognl.helperfunction;

import ognl.helperfunction.compatibility.WOMiddleManDeclarationFormatException;
import ognl.helperfunction.compatibility.WOMiddleManHTMLFormatException;

public interface WOHelperFunctionHTMLParserDelegate {

	public void didParseOpeningWebObjectTag(String content, WOHelperFunctionHTMLParser htmlParser) throws WOMiddleManHTMLFormatException;

	public void didParseClosingWebObjectTag(String content, WOHelperFunctionHTMLParser htmlParser) throws WOMiddleManDeclarationFormatException, WOMiddleManHTMLFormatException, ClassNotFoundException;

	public void didParseComment(String comment, WOHelperFunctionHTMLParser htmlParser);

	public void didParseText(String text, WOHelperFunctionHTMLParser htmlParser);
}
