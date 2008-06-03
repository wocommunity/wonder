package ognl.helperfunction.compatibility;
/*
import com.webobjects.appserver._private.WODeclarationFormatException;

public class WOMiddleManDeclarationFormatException extends WODeclarationFormatException {
	public WOMiddleManDeclarationFormatException(String s) {
		super(s);
	}
}
*/
import com.webobjects.appserver.parser.declaration.WODeclarationFormatException;

public class WOMiddleManDeclarationFormatException extends WODeclarationFormatException {
	public WOMiddleManDeclarationFormatException(String s) {
		super(s);
	}
}
