/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.assignments.delayed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.eof.qualifiers.ERXPrimaryKeyListQualifier;

/**
 * Very useful when you want to restrict the things a user can see during
 * searches or in list pages. Set it up via a rule like:
 * 
 * <pre>
 * <code>
 *  entity.name = "Movie" and session.user.role &lt;&gt; "admin"
 *   =&gt;
 *  extraRestrictingQualifier = {
 *      "studio" = "session.user.studios";
 *  } [er.directtoweb.ERDDelayedExtraQualifierAssignment]
 * </code>
 * </pre>
 * 
 * then in your query page use sth like:
 * 
 * <pre>
 * <code>
 * public EODataSource queryDataSource() {
 *    EODataSource ds = super.queryDataSource();
 *    if (ds != null &amp;&amp; (ds instanceof EODatabaseDataSource)) {
 *        EOFetchSpecification fs = ((EODatabaseDataSource)ds).fetchSpecification();
 *        EOQualifier q = fs.qualifier();
 *        EOQualifier extraQualifier = (EOQualifier)d2wContext().valueForKey("extraRestrictingQualifier");
 *        if(q != null &amp;&amp; extraQualifier != null) {
 *            q = new EOAndQualifier(new NSArray(new Object[] {q, extraQualifier}));
 *        } else if(extraQualifier != null) {
 *            q = extraQualifier;
 *        }
 *        fs.setQualifier(q);
 *    }
 *    return ds;
 * }</code>
 * </pre>
 * 
 * This should guarantee that the user can only see the Movies that are made by
 * studios contained in his studio relationship. If the value is null, then this
 * qualifier will not be added. To search for NULL, return
 * NSKeyValueCoding.NullValue.<br>
 * <br>
 * To use another than the default "equals" operator, specify one of the
 * following abbreviations:
 * <ul>
 * <li>ne (not equals)
 * <li>gt (greater than)
 * <li>gte (greater than or equal)
 * <li>lt (less than)
 * <li>lte (less than or equal)
 * <li>like (case-sensitive like)
 * <li>ilike (case-insensitive like)
 * </ul>
 * 
 * The following example will limit results to objects that don't have the same
 * id as the source object (often useful for self-referencing relationships) and
 * whose startDateTime is less than the source object's startDateTime:
 * 
 * <pre>
 *  {
 *   "id" = {
 *     "ne" = "object.id"; 
 *   }; 
 *   "startDateTime" = {
 *     "lt" = "object.startDateTime"; 
 *   }; 
 * }
 * </pre>
 * 
 * @author ak
 */
public class ERDDelayedExtraQualifierAssignment extends ERDDelayedAssignment {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    public static final Logger log = LoggerFactory.getLogger(ERDDelayedExtraQualifierAssignment.class);

    /**
     * Static constructor required by the EOKeyValueUnarchiver
     * interface. If this isn't implemented then the default
     * behavior is to construct the first super class that does
     * implement this method. Very lame.
     * @param eokeyvalueunarchiver to be unarchived
     * @return decoded assignment of this class
     */
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver) {
        return new ERDDelayedExtraQualifierAssignment(eokeyvalueunarchiver);
    }

    /**
     * Public constructor
     * @param u key-value unarchiver used when unarchiving
     *		from rule files. 
     */
    public ERDDelayedExtraQualifierAssignment (EOKeyValueUnarchiver u) { super(u); }

    /**
     * Public constructor
     * @param key context key
     * @param value of the assignment
     */
    public ERDDelayedExtraQualifierAssignment (String key, Object value) { super(key,value); }

    protected EOQualifier qualifierForArray(String key, NSArray objects) {
        if (objects == null)
            return null;
        if (objects.count() == 0)
            return new EOKeyValueQualifier(key, EOQualifier.QualifierOperatorEqual, null);
        return new ERXPrimaryKeyListQualifier(key, objects);
    }

    protected EOQualifier qualifierForObject(String key, Object object) {
        return new EOKeyValueQualifier(key, EOQualifier.QualifierOperatorEqual, object);
    }

    protected EOQualifier qualifierForObject(String key, NSDictionary object) {
        return new EOKeyValueQualifier(key, EOQualifier.QualifierOperatorEqual, object);
    }

    protected EOQualifier qualifierForOperatorAndObject(String key,
                                                        String operatorKey,
                                                        Object value) {
        if ("eq".equals(operatorKey)) {
            return new EOKeyValueQualifier(key, EOQualifier.QualifierOperatorEqual, value);
        } else if ("ne".equals(operatorKey)) {
            return new EOKeyValueQualifier(key, EOQualifier.QualifierOperatorNotEqual,
                    value);
        } else if ("gt".equals(operatorKey)) {
            return new EOKeyValueQualifier(key, EOQualifier.QualifierOperatorGreaterThan,
                    value);
        } else if ("gte".equals(operatorKey)) {
            return new EOKeyValueQualifier(key,
                    EOQualifier.QualifierOperatorGreaterThanOrEqualTo, value);
        } else if ("lt".equals(operatorKey)) {
            return new EOKeyValueQualifier(key, EOQualifier.QualifierOperatorLessThan,
                    value);
        } else if ("lte".equals(operatorKey)) {
            return new EOKeyValueQualifier(key,
                    EOQualifier.QualifierOperatorLessThanOrEqualTo, value);
        } else if ("like".equals(operatorKey)) {
            return new EOKeyValueQualifier(key, EOQualifier.QualifierOperatorLike, value);
        } else if ("ilike".equals(operatorKey)) {
            return new EOKeyValueQualifier(key,
                    EOQualifier.QualifierOperatorCaseInsensitiveLike, value);
        }
        return new EOKeyValueQualifier(key, EOQualifier.QualifierOperatorNotEqual, value);
    }

    protected EOQualifier extraQualifier(D2WContext c, NSDictionary<String, Object> dict) {
        NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<>();
        EOQualifier result = null;
        for (String key : dict.allKeys()) {
            Object value = null;
            if (dict.objectForKey(key) instanceof NSDictionary) {
                // qualifier definition with operator
                NSDictionary qDict = (NSDictionary) dict.objectForKey(key);
                if (qDict.size() == 1) {
                    String operatorKey = (String) qDict.allKeys().lastObject();
                    String contextKeyPath = (String) qDict.objectForKey(operatorKey);
                    if ("NSKeyValueCoding.NullValue".equals(contextKeyPath)) {
                        value = NSKeyValueCoding.NullValue;
                    } else {
                        value = c.valueForKeyPath(contextKeyPath);
                    }
                    if (value != null) {
                        EOQualifier q = qualifierForOperatorAndObject(key, operatorKey,
                                value);
                        qualifiers.addObject(q);
                    }
                }
            } else {
                value = c.valueForKeyPath((String) dict.objectForKey(key));
                if (value != null) {
                    EOQualifier q;
                    if (value instanceof NSArray) {
                        q = qualifierForArray(key, (NSArray) value);
                    } else {
                        if (value == NSKeyValueCoding.NullValue) {
                            value = null;
                        }
                        q = qualifierForObject(key, value);
                    }
                    if (q != null) {
                        qualifiers.addObject(q);
                    }
                }
            }
        }
        if (qualifiers.count() > 0)
            result = new EOAndQualifier(qualifiers);
        if (log.isDebugEnabled()) {
            log.debug("Computed qualifier: " + result);
        }
        return result;
    }

    @Override
    public Object fireNow(D2WContext c) {
        Object result = null;
        Object value = value();
        if (value != null && value instanceof NSDictionary) {
            result = extraQualifier(c, (NSDictionary) value);
        }
        return result;
    }
}