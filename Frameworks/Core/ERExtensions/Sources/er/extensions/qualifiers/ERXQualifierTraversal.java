/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.qualifiers;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOKeyComparisonQualifier;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EONotQualifier;
import com.webobjects.eocontrol.EOOrQualifier;
import com.webobjects.eocontrol.EOQualifierEvaluation;

import er.extensions.eof.qualifiers.ERXExistsQualifier;

/**
 * Traverse a network of qualifiers until a traversal method returns false.
 * Subclass and implement the methods you need.
 */
public class ERXQualifierTraversal {

	/** logging support */
	public static final Logger log = Logger.getLogger(ERXQualifierTraversal.class);

	/**
	 * Catch-all visitor, will get called for each qualifier.
	 * 
	 * @param q
	 *            the qualifier to process
	 */
	protected void visit(EOQualifierEvaluation q) {

	}

	/**
	 * Should traverse unknown qualifier? Logs an error.
	 * 
	 * @param q
	 *            the qualifier to process
	 * @return should traverse boolean qualifier
	 */
	protected boolean traverseUnknownQualifier(EOQualifierEvaluation q) {
		log.error("Found unknown qualifier type:" + q.getClass().getName());
		return true;
	}

	/**
	 * Should traverse not qualifier?
	 * 
	 * @param q
	 *            the qualifier to process
	 * @return should traverse not qualifier
	 */
	protected boolean traverseNotQualifier(EONotQualifier q) {
		return true;
	}

	/**
	 * Should traverse or qualifier?
	 * 
	 * @param q
	 *            the qualifier to process
	 * @return should traverse or qualifier
	 */
	protected boolean traverseOrQualifier(EOOrQualifier q) {
		return true;
	}

	/**
	 * Should traverse and qualifier?
	 * 
	 * @param q
	 *            the qualifier to process
	 * @return should traverse and qualifier
	 */
	protected boolean traverseAndQualifier(EOAndQualifier q) {
		return true;
	}

	/**
	 * Should traverse a key value qualifier?
	 * 
	 * @param q
	 *            the qualifier to process
	 * @return should traverse key value qualifier
	 */
	protected boolean traverseKeyValueQualifier(EOKeyValueQualifier q) {
		return true;
	}

	/**
	 * Should traverse key comparison qualifier?
	 * 
	 * @param q
	 *            the qualifier to process
	 * @return should traverse key comparison qualifier
	 */
	protected boolean traverseKeyComparisonQualifier(EOKeyComparisonQualifier q) {
		return true;
	}

	/**
	 * Should traverse true qualifier?
	 *
	 * @param q
	 *            the qualifier to process
	 * @return should traverse true qualifier
	 */
	protected boolean traverseTrueQualifier(ERXTrueQualifier q) {
		return true;
	}

	/**
	 * Should traverse false qualifier?
	 *
	 * @param q
	 *            the qualifier to process
	 * @return should traverse false qualifier
	 */
	protected boolean traverseFalseQualifier(ERXFalseQualifier q) {
		return true;
	}

	/**
	 * Should traverse exists qualifier?
	 *
	 * @param q
	 *            the qualifier to process
	 * @return should traverse exists qualifier
	 */
	protected boolean traverseExistsQualifier(ERXExistsQualifier q) {
		return true;
	}

	/**
	 * Traverses the supplied qualifier
	 * 
	 * @param q
	 *            the qualifier to process
	 * @param postOrder
	 *            if true, the qualifier is traversed from the bottom to the top
	 * @return whether or not to traverse the qualifier
	 */
	@SuppressWarnings("cast")
	private boolean traverseQualifier(EOQualifierEvaluation q, boolean postOrder) {
		Boolean result = null;
		if (q == null)
			result = Boolean.TRUE;
		else {
			visit(q);
			if (q instanceof EOOrQualifier) {
				EOOrQualifier aq = (EOOrQualifier) q;
				if (!postOrder) {
					result = traverseOrQualifier(aq) ? Boolean.TRUE : Boolean.FALSE;
				}
				if (result == null || result.booleanValue()) {
					for (Enumeration e = aq.qualifiers().objectEnumerator(); e.hasMoreElements();) {
						if (!traverseQualifier((EOQualifierEvaluation) e.nextElement(), postOrder)) {
							result = Boolean.FALSE;
							break;
						}
					}
				}
				if (postOrder && (result == null || result.booleanValue())) {
					result = traverseOrQualifier(aq) ? Boolean.TRUE : Boolean.FALSE;
				}
			}
			else if (q instanceof EOAndQualifier) {
				EOAndQualifier aq = (EOAndQualifier) q;
				if (!postOrder) {
					result = traverseAndQualifier(aq) ? Boolean.TRUE : Boolean.FALSE;
				}
				if (result == null || result.booleanValue()) {
					for (Enumeration e = aq.qualifiers().objectEnumerator(); e.hasMoreElements();) {
						if (!traverseQualifier((EOQualifierEvaluation) e.nextElement(), postOrder)) {
							result = Boolean.FALSE;
							break;
						}
					}
				}
				if (postOrder && (result == null || result.booleanValue())) {
					result = traverseAndQualifier(aq) ? Boolean.TRUE : Boolean.FALSE;
				}
			}
			else if (q instanceof EONotQualifier) {
				EONotQualifier aq = (EONotQualifier) q;
				if (!postOrder) {
					result = traverseNotQualifier(aq) ? Boolean.TRUE : Boolean.FALSE;
				}
				if (result == null || result.booleanValue()) {
					result = traverseQualifier((EOQualifierEvaluation) aq.qualifier(), postOrder) ? Boolean.TRUE : Boolean.FALSE;
				}
				if (postOrder && (result == null || result.booleanValue())) {
					result = traverseNotQualifier(aq) ? Boolean.TRUE : Boolean.FALSE;
				}
			}
			else if (q instanceof EOKeyValueQualifier) {
				result = traverseKeyValueQualifier((EOKeyValueQualifier) q) ? Boolean.TRUE : Boolean.FALSE;
			}
			else if (q instanceof EOKeyComparisonQualifier) {
				result = traverseKeyComparisonQualifier((EOKeyComparisonQualifier) q) ? Boolean.TRUE : Boolean.FALSE;
			}
			else if (q instanceof ERXTrueQualifier) {
				result = traverseTrueQualifier((ERXTrueQualifier) q) ? Boolean.TRUE : Boolean.FALSE;
			}
			else if (q instanceof ERXFalseQualifier) {
				result = traverseFalseQualifier((ERXFalseQualifier) q) ? Boolean.TRUE : Boolean.FALSE;
			}
			else if (q instanceof ERXExistsQualifier) {
				result = traverseExistsQualifier((ERXExistsQualifier) q) ? Boolean.TRUE : Boolean.FALSE;
			}
			else {
				result = traverseUnknownQualifier(q) ? Boolean.TRUE : Boolean.FALSE;
			}
			if (result == null) {
				throw new RuntimeException("Found unknown qualifier type:" + q.getClass().getName());
			}
		}
		return result.booleanValue();
	}

	/**
	 * Visit every descendent qualifier in the given qualifier tree in a preorder traversal.
	 * 
	 * @param q
	 *            the root qualifier to traverse
	 */
	public void traverse(EOQualifierEvaluation q) {
		traverseQualifier(q, false);
	}

	/**
	 * Visit every descendent qualifier in the given qualifier tree.
	 * 
	 * @param q
	 *            the root qualifier to traverse
	 * @param postOrder
	 *            if true, the qualifier is traversed from the bottom to the top
	 */
	public void traverse(EOQualifierEvaluation q, boolean postOrder) {
		traverseQualifier(q, postOrder);
	}
}
