/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;

/**
 * The primary key list qualifier is used to generate
 * a qualifier that can be used to filter a result set
 * for a given set of primary keys. Note that this uses
 * the IN expression and as such may not work with some
 * databases. <br/>
 * <br/>
 * Given a list of EOs, this generates a query looking like
 *
 *   ...  t0.ID in (< the list of primary Keys for EOs in the list>) ..
 *
 * this is useful for pre-fetching type uses.
 */
// ENHANCEME: Should support the NOT operator, also should support handing
//		off of an array of primary keys, not just eos.
public class ERXPrimaryKeyListQualifier extends EOKeyValueQualifier {

    /**
     * Public constructor inherited by EOKeyValueQualifier.
     * @param key to qualify against
     * @param s selector to use
     * @param v value object
     */
    public ERXPrimaryKeyListQualifier(String k,
                                      NSSelector s,
                                      Object v) {
        super(k,s,v);
    }    

    /**
     * Constructs a primary key list qualifer for a given
     * set of enterprise objects. For now only use this
     * qualifier if the primary key attribute of your
     * enterprise object is named 'id'.
     * @param eos array of enterprise objects
     */
    // FIXME: Given the array of eos we should be able to determine the primary key
    //		attribute name.
    public ERXPrimaryKeyListQualifier(NSArray eos) {
        super("id", EOQualifier.QualifierOperatorEqual, ERXEOToManyQualifier.primaryKeysForObjectsFromSameEntity(eos));
    }

    /**
     * Constructs a primary key list qualifer for a given
     * set of enterprise objects and the primary key
     * attribute name.
     * @param key primary key attribute name
     * @param eos array of enterprise objects
     */
    public ERXPrimaryKeyListQualifier(String key, NSArray eos) {
        super(key, EOQualifier.QualifierOperatorEqual, ERXEOToManyQualifier.primaryKeysForObjectsFromSameEntity(eos));
    }

    /**
     * Constructs a primary key list qualifer for a given
     * set of enterprise objects, the primary key
     * attribute name and a foriegn key. This type of
     * qualifier can be useful for prefetching a to-one
     * relationship off of many enterprise objects.
     * @param key primary key attribute name
     * @param foreignKey attribute name.
     * @param eos array of enterprise objects
     */    
    public ERXPrimaryKeyListQualifier(String key, String foreignKey, NSArray eos) {
        super(key, EOQualifier.QualifierOperatorEqual, ERXEOToManyQualifier.primaryKeysForObjectsFromSameEntity(foreignKey, eos));
    }

    /**
     * Constructs the sql string for the primary key
     * list qualifier. See the description of the class
     * for the format of the sql that is generated.
     * @param e a sql expression
     * @return sql string for primary key list
     */ 
    // FIXME: Need support for data primary keys 
    public String sqlStringForSQLExpression(EOSQLExpression e) {
        StringBuffer sb=new StringBuffer();
        sb.append(e.sqlStringForAttributeNamed(key()));
        sb.append(" IN ");
        sb.append(value());
        return sb.toString();
    }

    /**
     * String representation of the primary key list
     * qualifier.
     * @return string description of the qualifier
     */
    public String toString() {
        return " <primaryKey> IN '" + value() + "'";
    }

    /*
     * EOF seems to be wanting to clone qualifiers when
     * they are inside an and-or qualifier without this
     * method, ERXPrimaryKeyListQualifier is cloned into
     * an EOSQLQualifier and the generated SQL is incorrect..
     * @return cloned primary key list qualifier.
     */
    public Object clone() {
        return new ERXPrimaryKeyListQualifier(key(),selector(),value());
    }
}
