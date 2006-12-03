package er.extensions;

import com.webobjects.eoaccess.EOQualifierSQLGeneration;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOQualifierSQLGeneration._OrQualifierSupport;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOKeyComparisonQualifier;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EONotQualifier;
import com.webobjects.eocontrol.EOOrQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOQualifierVisitor;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

/**
 * ERXInOrQualifierSupport replaces the stock _OrQualifierSupport and turns
 * qualifying EOOrQualifiers into IN-set SQL statements instead of enormous
 * strings of OR's.
 * <p>
 * To register this as the generation support for EOOrQualifiers, register
 * with:
 * <p>
 * EOQualifierSQLGeneration.Support.setSupportForClass(new ERXInOrQualifierSupport(), EOOrQualifier._CLASS);
 *         
 * @author mschrag
 */
public class ERXInOrQualifierSupport extends _OrQualifierSupport {
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
		private NSMutableArray _values;
		private boolean _firstVisit;

		public OrIsInVisitor() {
			_firstVisit = true;
			_canBeRepresentedAsInSet = true;
			_values = new NSMutableArray();
		}

		public boolean canBeRepresentedAsInSet() {
			return _canBeRepresentedAsInSet;
		}

		public String key() {
			return _key;
		}

		public NSMutableArray values() {
			return _values;
		}

		public void visitKeyValueQualifier(EOKeyValueQualifier qualifier) {
			if (qualifier.selector() == EOQualifier.QualifierOperatorEqual) {
				_firstVisit = false;
				String key = qualifier.key();
				if (_key != null && !_key.equals(key)) {
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
			if (_firstVisit) {
				_firstVisit = false;
			}
			else {
				_canBeRepresentedAsInSet = false;
			}
		}

		public void visitUnknownQualifier(EOQualifier qualifier) {
			_canBeRepresentedAsInSet = false;
		}
	}
}