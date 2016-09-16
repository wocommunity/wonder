package er.extensions.eof.qualifiers;

import com.webobjects.eoaccess.EOQualifierSQLGeneration;
import com.webobjects.eoaccess.EOQualifierSQLGeneration._OrQualifierSupport;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOKeyComparisonQualifier;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EONotQualifier;
import com.webobjects.eocontrol.EOOrQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOQualifierVisitor;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.eof.ERXConstant;
import er.extensions.jdbc.ERXSQLHelper;
import er.extensions.qualifiers.ERXKeyValueQualifier;

/**
 * ERXInOrQualifierSupport replaces the stock _OrQualifierSupport and turns qualifying EOOrQualifiers into IN-set SQL
 * statements instead of enormous strings of OR's.
 * <p>
 * To register this as the generation support for EOOrQualifiers, register with:
 * <p>
 * EOQualifierSQLGeneration.Support.setSupportForClass(new ERXInOrQualifierSupport(), EOOrQualifier._CLASS);
 * 
 * @author mschrag
 */
public class ERXInOrQualifierSupport extends _OrQualifierSupport {
	@Override
	public String sqlStringForSQLExpression(EOQualifier qualifier, EOSQLExpression sqlExpression) {
		OrIsInVisitor visitor = new OrIsInVisitor();
		qualifier._accept(visitor, false);
		String sqlString;
		NSArray values = visitor.values();
		if (visitor.canBeRepresentedAsInSet() && values.count() > 0) {
			if (values.count() == 1) {
				EOKeyValueQualifier singleValueQualifier = new EOKeyValueQualifier(visitor.key(), EOQualifier.QualifierOperatorEqual, visitor.values().objectAtIndex(0));
				sqlString = EOQualifierSQLGeneration.Support.supportForClass(EOKeyValueQualifier.class).sqlStringForSQLExpression(singleValueQualifier, sqlExpression);
			}
			else {
				sqlString = ERXSQLHelper.newSQLHelper(sqlExpression).sqlWhereClauseStringForKey(sqlExpression, visitor.key(), visitor.values());
			}
		}
		else {
			sqlString = super.sqlStringForSQLExpression(qualifier, sqlExpression);
		}
		return sqlString;
	}

	protected static class OrIsInVisitor implements EOQualifierVisitor {
		private boolean _canBeRepresentedAsInSet;
		private String _key;
		private NSMutableArray<Object> _values;

		public OrIsInVisitor() {
			_canBeRepresentedAsInSet = true;
			_values = new NSMutableArray<>();
		}

		public boolean canBeRepresentedAsInSet() {
			return _canBeRepresentedAsInSet;
		}

		public String key() {
			return _key;
		}

		public NSMutableArray<Object> values() {
			return _values;
		}

		public void visitKeyValueQualifier(EOKeyValueQualifier qualifier) {
			if (_canBeRepresentedAsInSet) {
				Class qualifierClass = qualifier.getClass();
				// MS: You might end up with subclasses of EOKeyValueQualifier, which could 
				// cause terrible problems, so here we want to check for specific classes that we support
				// and only convert them to in-sets if it's in the list. 
				if (qualifierClass == EOKeyValueQualifier.class || qualifierClass == ERXKeyValueQualifier.class) {
					if (qualifier.selector() == EOQualifier.QualifierOperatorEqual) {
						String key = qualifier.key();
						Object value = qualifier.value();
						// ak: this ends up in value.toString() (we should really use bind vars for the IN qualifier)
						// so we need to exclude the obvious cases where the value produces garbage
						if ((_key != null && !_key.equals(key)) || (value != null && (
								(value instanceof ERXConstant.NumberConstant) ||
								(value instanceof Number && !value.getClass().getName().startsWith("java.")) ||
								(value == NSKeyValueCoding.NullValue)
								))) {
							_canBeRepresentedAsInSet = false;
						}
						else {
							_key = key;
							_values.addObject(qualifier.value());
						}
					}
					else {
						_canBeRepresentedAsInSet = false;
					}
				}
				else {
					_canBeRepresentedAsInSet = false;
				}
			}
		}

		public void visitAndQualifier(EOAndQualifier qualifier) {
			_canBeRepresentedAsInSet = false;
		}

		public void visitKeyComparisonQualifier(EOKeyComparisonQualifier qualifier) {
			_canBeRepresentedAsInSet = false;
		}

		public void visitNotQualifier(EONotQualifier qualifier) {
			_canBeRepresentedAsInSet = false;
		}

		public void visitOrQualifier(EOOrQualifier qualifier) {
			// MS: nested or statements are ok as long as it meets all
			// the same criteria, so:
			// (a = 5 or (a = 6 or a = 7) or a = 8) is a in (5,6,7,8)
		}

		public void visitUnknownQualifier(EOQualifier qualifier) {
			_canBeRepresentedAsInSet = false;
		}
	}
}