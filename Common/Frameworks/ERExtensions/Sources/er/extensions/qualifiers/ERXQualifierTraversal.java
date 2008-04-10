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
	 */
	protected void visit(EOQualifierEvaluation q) {

	}

	/**
	 * Should traverse unknown qualifier? Logs an error.
	 * 
	 * @return should traverse boolean qualifier
	 */
	protected boolean traverseUnknownQualifier(EOQualifierEvaluation q) {
		log.error("Found unknown qualifier type:" + q.getClass().getName());
		return true;
	}

	/**
	 * Should traverse not qualifier?
	 * 
	 * @return should traverse not qualifier
	 */
	protected boolean traverseNotQualifier(EONotQualifier q) {
		return true;
	}

	/**
	 * Should traverse or qualifier?
	 * 
	 * @return should traverse or qualifier
	 */
	protected boolean traverseOrQualifier(EOOrQualifier q) {
		return true;
	}

	/**
	 * Should traverse and qualifier?
	 * 
	 * @return should traverse and qualifier
	 */
	protected boolean traverseAndQualifier(EOAndQualifier q) {
		return true;
	}

	/**
	 * Should traverse a key value qualifier?
	 * 
	 * @return should traverse key value qualifier
	 */
	protected boolean traverseKeyValueQualifier(EOKeyValueQualifier q) {
		return true;
	}

	/**
	 * Should traverse key comparison qualifier?
	 * 
	 * @return should traverse key comparison qualifier
	 */
	protected boolean traverseKeyComparisonQualifier(EOKeyComparisonQualifier q) {
		return true;
	}

	/**
	 * Traverses the supplied qualifier
	 * 
	 * @param q
	 * @return
	 */
	private boolean traverseQualifier(EOQualifierEvaluation q) {
		Boolean result = null;
		if (q == null)
			result = Boolean.TRUE;
		else {
			visit(q);
			if (q instanceof EOOrQualifier) {
				EOOrQualifier aq = (EOOrQualifier) q;
				result = traverseOrQualifier(aq) ? Boolean.TRUE : Boolean.FALSE;
				if(result) {
					for (Enumeration e = aq.qualifiers().objectEnumerator(); e.hasMoreElements();) {
						if (!traverseQualifier((EOQualifierEvaluation) e.nextElement())) {
							result = Boolean.FALSE;
							break;
						}
					}
				}
			}
			else if (q instanceof EOAndQualifier) {
				EOAndQualifier aq = (EOAndQualifier) q;
				result = traverseAndQualifier(aq) ? Boolean.TRUE : Boolean.FALSE;
				if(result) {
					for (Enumeration e = aq.qualifiers().objectEnumerator(); e.hasMoreElements();) {
						if (!traverseQualifier((EOQualifierEvaluation) e.nextElement())) {
							result = Boolean.FALSE;
							break;
						}
					}
				}
			}
			else if (q instanceof EONotQualifier) {
				EONotQualifier aq = (EONotQualifier) q;
				result = traverseNotQualifier(aq) ? Boolean.TRUE : Boolean.FALSE;
				if(result) {
					result = traverseQualifier((EOQualifierEvaluation) aq.qualifier()) ? Boolean.TRUE : Boolean.FALSE;
				}
			}
			else if (q instanceof EOKeyValueQualifier) {
				result = traverseKeyValueQualifier((EOKeyValueQualifier) q) ? Boolean.TRUE : Boolean.FALSE;
			}
			else if (q instanceof EOKeyComparisonQualifier) {
				result = traverseKeyComparisonQualifier((EOKeyComparisonQualifier) q) ? Boolean.TRUE : Boolean.FALSE;
			}
			else {
				result = traverseUnknownQualifier(q) ? Boolean.TRUE : Boolean.FALSE;
			}
		}
		if (result == null) {
			throw new RuntimeException("Found unknown qualifier type:" + q.getClass().getName());
		}
		return result.booleanValue();
	}

	public void traverse(EOQualifierEvaluation q) {
		boolean dummy = traverseQualifier(q);
	}
}
