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

public class ERXQualifierInSubquery extends EOSQLQualifier {

    // generates a subquery for the qualifier given in argument
    //
    //   ...  t0.ID in (SELECT t0.ID FROM X WHERE <your qualifier here> ) ..
    //
    //
    // this class can be used to work around the EOF bug where OR
    // queries involving many-to-manies are incorrectly generated
    //
    //
    // It will also generate
    //
    //  ... t0.FOREIGN_KEY_ID in (select t1.ID from X where <your qualifier here>)
    //
    // with the 3 arg constructor
    
    private EOQualifier _qualifier;
    private EOEntity _entity;
    private EOAttribute _attribute;

    private ERXQualifierInSubquery(EOQualifier q,
                                  EOEntity entity) {        
        super(entity,null,null);
        _qualifier=q;
        _entity=entity;
    }

    public ERXQualifierInSubquery(EOEntity entity,
                                 EOQualifier q) {

        /* in theory we should be able to accept any qualifier in here
         but for KeyValueQualifiers, they HAVE to end in an attribute it seems
         coveredCounties.state=<State mass> throws */
        this (q, entity);
        _attribute=(EOAttribute)_entity.primaryKeyAttributes().objectAtIndex(0);
    }

    public ERXQualifierInSubquery(EOEntity entity,
                                 EOAttribute att,
                                 EOQualifier q) {
        this(q, entity);
        _attribute=att;
    }

    public String sqlStringForSQLExpression(EOSQLExpression e) {
        StringBuffer sb=new StringBuffer();
        sb.append(e.sqlStringForAttribute(_attribute));
        sb.append(" IN ( ");
        EOFetchSpecification fs=new EOFetchSpecification(_entity.name(),
                                                         _qualifier,
                                                         null,
                                                         false,
                                                         true,
                                                         null);
        // FIXME: This will create a new EOAdaptor everytime it is called.
        EOSQLExpressionFactory factory=EOAdaptor.adaptorWithModel(_entity.model()).expressionFactory();
        EOSQLExpression expression=factory.selectStatementForAttributes( _entity.primaryKeyAttributes(),
                                           false,
                                           fs,
                                           _entity);

        sb.append(expression.statement());

        sb.append(" ) ");
        return sb.toString();
    }

    public String toString() { return " <subquery> '"+_qualifier.toString()+"'"; }

    /*
     EOF seems to be wanting to clone qualifiers when the are inside an and-or qualifier
     without this method, EOToManyQualifier is cloned into an EOSQLQualifier and the generated SQL is incorrect..
        */
    public Object clone() {
        return new ERXQualifierInSubquery(_entity, _qualifier);
    }    
}
