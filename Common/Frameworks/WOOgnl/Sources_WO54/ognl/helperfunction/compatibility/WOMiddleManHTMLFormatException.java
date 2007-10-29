package ognl.helperfunction.compatibility;

/*
import com.webobjects.appserver._private.WOHTMLFormatException;

public class WOMiddleManHTMLFormatException extends WOHTMLFormatException {
	public WOMiddleManHTMLFormatException(String s) {
		super(s);
	}
}
*/
import com.webobjects.appserver.parser.WOHTMLFormatException;

public class WOMiddleManHTMLFormatException extends WOHTMLFormatException {
	public WOMiddleManHTMLFormatException(String s) {
		super(s);
	}
}
