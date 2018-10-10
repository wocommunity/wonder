package com.webobjects.eoaccess;

import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;


class _EOExpressionArray
extends NSMutableArray<Object>
implements EOSQLExpression.SQLValue
{
	static final long serialVersionUID = 2726361908862120105L;
	protected String _prefix;
	protected String _infix;
	protected String _suffix;

	public _EOExpressionArray(Object object)
	{
		super(object);
		_initWithPrefixInfixSuffix("", "", "");
	}

	public _EOExpressionArray()
	{
		_initWithPrefixInfixSuffix("", "", "");
	}

	public _EOExpressionArray(String prefix, String infix, String suffix)
	{
		_initWithPrefixInfixSuffix(prefix, infix, suffix);
	}

	private void _initWithPrefixInfixSuffix(String prefix, String infix, String suffix) {
		_prefix = prefix;
		_infix = infix;
		_suffix = suffix;
	}

	public String prefix() {
		return _prefix;
	}

	public String infix() {
		return _infix;
	}

	public String suffix() {
		return _suffix;
	}

	public void setPrefix(String prefix) {
		_prefix = prefix;
	}

	public void setInfix(String infix) {
		_infix = infix;
	}

	public void setSuffix(String suffix) {
		_suffix = suffix;
	}



	public boolean referencesObject(Object object)
	{
		for (int i = 0; i < count(); i++) {
			Object member = objectAtIndex(i);
			if (object == member)
				return true;
			if (((member instanceof _EOExpressionArray)) && (((_EOExpressionArray)member).referencesObject(object))) {
				return true;
			}
		}
		return false;
	}



	public Object clone()
	{
		_EOExpressionArray aCopy = new _EOExpressionArray(_prefix, _infix, _suffix);
		aCopy.addObjectsFromArray(this);
		return aCopy;
	}

	public String toString()
	{
		return super.toString();
	}

	public String valueForSQLExpression(EOSQLExpression context)
	{
		int count = count();

		if (count == 0) {
			return null;
		}

		if ((context != null) && ((objectAtIndex(0) instanceof EORelationship))) {
			return context.sqlStringForAttributePath(this);
		}

		StringBuffer aString = _prefix != null ? new StringBuffer(_prefix) : new StringBuffer(64);
		int pieces;
		for (int i = pieces = 0; i < count; i++) {
			Object expression = objectAtIndex(i);
			String result = valueForSQLExpression(expression, context);
			if ((result != null) && (result.length() > 0)) {
				pieces++; if ((pieces > 1) && 
						(_infix != null)) {
					aString.append(_infix);
				}
				aString.append(result);
			}
		}

		if (pieces == 0)
			return null;
		if (_suffix != null)
			aString.append(_suffix);
		return new String(aString);
	}

	public String valueForSQLExpression(Object expression, EOSQLExpression context)
	{
		if ((expression instanceof EOSQLExpression.SQLValue))
			return ((EOSQLExpression.SQLValue)expression).valueForSQLExpression(context);
		if ((expression instanceof Number))
			return String.valueOf(expression);
		if ((expression instanceof String))
			return (String)expression;
		if (expression == NSKeyValueCoding.NullValue) {
			return "NULL";
		}
		return expression.toString();
	}

	public boolean _isPropertyPath()
	{
		if (count() <= 0) {
			return false;
		}
		Object object = objectAtIndex(0);
		return object instanceof EORelationship;
	}
}

