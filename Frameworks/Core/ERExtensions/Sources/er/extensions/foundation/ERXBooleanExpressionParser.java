package er.extensions.foundation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSKeyValueCodingAdditions;

/**
 * Parses a boolean expression and evaluates that boolean expression against a valueProvider object that returns boolean values
 * for the variable symbols found in the boolean expression.
 * 
 * The valueProvider object must implement NSKeyValueCodingAdditions interface.
 * 
 * Acceptable boolean expressions can use the words AND, OR and NOT in upper, lower or mixed case. Parentheses can be
 * used as needed to group elements of the expression.
 * 
 * Variable symbols in the boolean expression can use characters and formatting of typical keys or keyPaths.
 * 
 * All other words besides AND, OR and NOT are assumed to be variables (aka keyPaths) which resolve to Boolean values when
 * valueForKeyPath is invoked on the valueProvider object (which can be a NSDictionary of variable values or any object that
 * implements {@link NSKeyValueCodingAdditions})
 * 
 * For example
 * 	Expression: <code>(canViewPerson AND canEditPerson) OR (isTheBoss AND NOT account.isAccountDisabled)</code>
 * 
 * @author kieran
 *
 */
public class ERXBooleanExpressionParser {
	private static Pattern KEYPATH_TOKEN_PATTERN = Pattern.compile("\\w+([.]\\w*)*");
	private final static String[] BOOLEAN_WORDS = new String[] { "AND", "OR", "NOT" };
	
	private final String expression;
	
	public ERXBooleanExpressionParser(String expression) {
		this.expression = expression;
	}
	
	/**
	 * @return the result of the boolean expression when evaluated with valueProvider
	 */
	public boolean evaluateWithObject(NSKeyValueCodingAdditions valueProvider) {
		return qualifier().evaluateWithObject(valueProvider);
	}
	
	private String _qualifierFormatForBooleanExpression;
	
	/** @return the boolean expression converted to a EOQualifier qualifier format */
	private String qualifierFormatForBooleanExpression() {
		if ( _qualifierFormatForBooleanExpression == null ) {
			StringBuffer sb = new StringBuffer();
			Matcher matcher = KEYPATH_TOKEN_PATTERN.matcher(expression);
			while( matcher.find()) {
				String token = matcher.group();
				// Leave boolean word operators alone
				if (!isBooleanWordToken(token)) {
					String replacement = "(" + matcher.group() + " = 'true')";
					matcher.appendReplacement(sb, replacement);				
				}
			}
			matcher.appendTail(sb);
			_qualifierFormatForBooleanExpression = sb.toString();
		}
		return _qualifierFormatForBooleanExpression;
	}
	
	private EOQualifier _qualifier;
	
	/** @return the EOQualifier resulting from the boolean expression. Leaving this as public since it is useful for debugging the expression morphing in WOComponents */
	public EOQualifier qualifier() {
		if ( _qualifier == null ) {
			_qualifier = EOQualifier.qualifierWithQualifierFormat(qualifierFormatForBooleanExpression(), null);
		}
		return _qualifier;
	}
	
	private boolean isBooleanWordToken(String token) {
		// Case-insensitive match
		token = token.toUpperCase();
		for (int i = 0; i < BOOLEAN_WORDS.length; i++) {
			if (BOOLEAN_WORDS[i].equals(token)) return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return expression;
	}
	
	public String toDebugString() {
		StringBuilder b = new StringBuilder();
		b.append("Expression: ").append(expression);
		b.append("qualifier format: ").append(qualifierFormatForBooleanExpression());
		b.append("EOQualifier: ").append(qualifier());
		return b.toString();
	}
}
