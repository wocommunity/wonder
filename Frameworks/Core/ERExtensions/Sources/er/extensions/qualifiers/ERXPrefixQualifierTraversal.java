package er.extensions.qualifiers;

import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOKeyComparisonQualifier;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EONotQualifier;
import com.webobjects.eocontrol.EOOrQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOQualifierEvaluation;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSRange;

import er.extensions.eof.ERXKey;
import er.extensions.eof.qualifiers.ERXExistsQualifier;

/**
 * Takes a qualifier, traverses every subqualifier, and prepends every keypath
 * with an extra keypath. This should be end up being similar to
 * EOQualifierSQLGeneration.Support.qualifierMigratedFromEntityRelationshipPath
 * except that it does not deal in entities.
 * 
 * @author mschrag
 */
public class ERXPrefixQualifierTraversal extends ERXQualifierTraversal {
	private String _prefix;
	private NSMutableArray<EOQualifier> _qualifiers;

	protected ERXPrefixQualifierTraversal(ERXKey prefix) {
		_prefix = prefix.key() + ".";
	}

	/**
	 * Returns a qualifier with the given key prepended to every qualifier's
	 * key.
	 * 
	 * @param qualifier
	 *            the qualifier to prepend with a key
	 * @param prefix
	 *            the key to prepend
	 * @return a new matching qualifier with the prefix prepended
	 */
	public static synchronized EOQualifier prefixQualifierWithKey(EOQualifierEvaluation qualifier, ERXKey prefix) {
		ERXPrefixQualifierTraversal prefixTraversal = new ERXPrefixQualifierTraversal(prefix);
		prefixTraversal.traverse(qualifier, true);
		EOQualifier prefixedQualifier = prefixTraversal._qualifiers.lastObject();
		return prefixedQualifier;
	}

	@Override
	protected boolean traverseUnknownQualifier(EOQualifierEvaluation q) {
		throw new UnsupportedOperationException("Unknown qualifier type '" + q.getClass().getName() + "'.");
	}

	@Override
	protected boolean traverseNotQualifier(EONotQualifier q) {
		ERXNotQualifier nq = new ERXNotQualifier(_qualifiers.lastObject());
		_qualifiers.removeLastObject();
		_qualifiers.addObject(nq);
		return true;
	}

	@Override
	protected boolean traverseOrQualifier(EOOrQualifier q) {
		if (q.qualifiers().isEmpty()) {
			_qualifiers.addObject(new ERXOrQualifier());
		} else {
			NSRange range = new NSRange(_qualifiers.count() - q.qualifiers().count(), q.qualifiers().count());
			ERXOrQualifier oq = new ERXOrQualifier(_qualifiers.subarrayWithRange(range));
			_qualifiers.removeObjectsInRange(range);
			_qualifiers.addObject(oq);
		}
		return true;
	}

	@Override
	protected boolean traverseAndQualifier(EOAndQualifier q) {
		if (q.qualifiers().isEmpty()) {
			_qualifiers.addObject(new ERXAndQualifier());
		} else {
			NSRange range = new NSRange(_qualifiers.count() - q.qualifiers().count(), q.qualifiers().count());
			ERXAndQualifier aq = new ERXAndQualifier(_qualifiers.subarrayWithRange(range));
			_qualifiers.removeObjectsInRange(range);
			_qualifiers.addObject(aq);
		}
		return true;
	}

	@Override
	protected boolean traverseKeyValueQualifier(EOKeyValueQualifier q) {
		ERXKeyValueQualifier kvq = new ERXKeyValueQualifier(_prefix + q.key(), q.selector(), q.value());
		_qualifiers.addObject(kvq);
		return true;
	}

	@Override
	protected boolean traverseKeyComparisonQualifier(EOKeyComparisonQualifier q) {
		ERXKeyComparisonQualifier kcq = new ERXKeyComparisonQualifier(_prefix + q.leftKey(), q.selector(), _prefix + q.rightKey());
		_qualifiers.addObject(kcq);
		return true;
	}

	@Override
	protected boolean traverseFalseQualifier(ERXFalseQualifier q) {
		_qualifiers.addObject(q);
		return true;
	}

	@Override
	protected boolean traverseTrueQualifier(ERXTrueQualifier q) {
		_qualifiers.addObject(q);
		return true;
	}

	@Override
	protected boolean traverseExistsQualifier(ERXExistsQualifier q) {
		String newBaseKeyPath = q.baseKeyPath() != null ? _prefix + q.baseKeyPath() : _prefix.substring(0, _prefix.length() - 1);
		_qualifiers.add(new ERXExistsQualifier(q.subqualifier(), newBaseKeyPath, q.usesInQualInstead()));
		return true;
	}

	@Override
	public synchronized void traverse(EOQualifierEvaluation q, boolean postOrder) {
		if (!postOrder) {
			throw new IllegalArgumentException("ERXPrefixQualifierTraversal requires a postOrder traversal.");
		}
		_qualifiers = new NSMutableArray<>();
		super.traverse(q, postOrder);
	}
}
