/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* ERKeyValueQualifierInSubquery.java created by bposokhow on Tue 25-Jul-2000 */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import java.util.*;
import org.apache.log4j.Category;

public class ERXPrimaryKeyListQualifier extends EOKeyValueQualifier {

    ////////////////////////////////////////////  log4j category  ////////////////////////////////////////////
    public static final Category cat = Category.getInstance(ERXPrimaryKeyListQualifier.class);

    // given a list of EOs, generate a query looking like
    //
    //   ...  t0.ID in (< the list of primary Keys for EOs in the list>) ..
    //
    // this is useful for pre-fetching type uses.

    public ERXPrimaryKeyListQualifier(NSArray eos) {
        super("id", EOQualifier.QualifierOperatorEqual, ERXEOToManyQualifier.primaryKeysForObjectsFromSameEntity(eos));
    }
    public ERXPrimaryKeyListQualifier(String key, NSArray eos) {
        super(key, EOQualifier.QualifierOperatorEqual, ERXEOToManyQualifier.primaryKeysForObjectsFromSameEntity(eos));
    }
    public ERXPrimaryKeyListQualifier(String key, String foreignKey, NSArray eos) {
        super(key, EOQualifier.QualifierOperatorEqual, ERXEOToManyQualifier.primaryKeysForObjectsFromSameEntity(foreignKey, eos));
    }
    
    public ERXPrimaryKeyListQualifier(String k,
                                     NSSelector s,
                                     Object v) {
        super(k,s,v);
    }

    public String sqlStringForSQLExpression(EOSQLExpression e) {
        StringBuffer sb=new StringBuffer();
        sb.append(e.sqlStringForAttributeNamed(key()));
        sb.append(" IN  ");
        sb.append(value());
        return sb.toString();
    }

    public String description() {
        return " <primaryKey> IN '"+value()+"'";
    }
    public String toString() {
        return description();
    }

    /*
     EOF seems to be wanting to clone qualifiers when they are inside an and-or qualifier
     without this method, EOToManyQualifier is cloned into an EOSQLQualifier and the generated SQL is incorrect..
        */
    public Object clone() {
        return new ERXPrimaryKeyListQualifier(key(),selector(),value());
    }

     
}
